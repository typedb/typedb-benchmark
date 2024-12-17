# -*- coding: utf-8 -*-
# TODO:
# - Create faster IDs (no traversal):
#   - D = W * 10 + D
#   - C = D * 3000 + C
# - Hardcode DS_INFO
# - Customer of distict
# - Order of customer
# - Orderline of order
# -----------------------------------------------------------------------

from __future__ import with_statement

import os
import logging
from pprint import pformat
import time
from typedb.driver import *

import sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
import constants
from drivers.abstractdriver import AbstractDriver
from enum import Enum
from multiprocessing import Event

ITEMS_COMPLETE = Event()

class EDITION(Enum):
    Cloud = 1
    Core = 2

DPW = constants.DISTRICTS_PER_WAREHOUSE
CPD = constants.CUSTOMERS_PER_DISTRICT
DATA_COUNT = { }

## ==============================================
## TypeDB3Driver
## ==============================================
class Typedb3Driver(AbstractDriver):
    DEFAULT_CONFIG = {
        "database": ("Name of DB", "tpcc" ),
        "addr": ("Address of server", "127.0.0.1:1729" ),
        "edition": ("TypeDB Edition (Core or Cloud)", "Core" ),
        "user": ("DB User", "admin" ),
        "password": ("DB Password", "password"),
        "schema": ("Script-relative path to schema file", "tql3/tpcc-schema.tql"),
        "debug": ("Enable debug-level logging", "0"),
    }
   
    def __init__(self, ddl, shared_event=None):
        super(Typedb3Driver, self).__init__("typedb", ddl)
        self.database = None
        self.addr = None
        self.edition = None
        self.user = None
        self.password = None
        self.driver = None
        self.tx = None
        self.execution_timer = None
        self.items_complete_event = shared_event
        self.debug = None

        # Set up TypeDB specific logger
        self.typedb_logger = logging.getLogger('typedb_logger')
        handler = logging.FileHandler('typedb.log')
        handler.setFormatter(logging.Formatter('%(asctime)s - %(message)s'))
        self.typedb_logger.addHandler(handler)
    
    ## ----------------------------------------------
    ## makeDefaultConfig
    ## ----------------------------------------------
    def makeDefaultConfig(self):
        return Typedb3Driver.DEFAULT_CONFIG
    
    ## ----------------------------------------------
    ## loadConfig
    ## ----------------------------------------------
    def loadConfig(self, config):
        # Config passed here contains some extra parameters (see `driver.loadConfig` in tpcc.py)
        for key in Typedb3Driver.DEFAULT_CONFIG.keys():
            assert key in config, "Missing parameter '%s' in %s configuration" % (key, self.name)
        
        self.database = str(config["database"])
        self.addr = str(config["addr"])
        self.user = str(config["user"])
        self.password = str(config["password"])
        edition = config["edition"]
        if edition == "Core":
            self.edition = EDITION.Core
        elif edition == "Cloud":
            self.edition = EDITION.Cloud
        else:
            raise Exception(f"Did not open a driver for edition: {edition}")

        self.schema = str(config["schema"])

        self.debug = bool(config["debug"])
        if self.debug:
            self.typedb_logger.setLevel(logging.DEBUG)
            self.execution_timer = time.time()
        else:
            self.typedb_logger.setLevel(logging.INFO)

        credentials = Credentials(self.user, self.password)

        if self.edition is EDITION.Core:
            self.driver = TypeDB.core_driver(address=f"{self.addr}", credentials=credentials, driver_options=DriverOptions())
        if self.edition is EDITION.Cloud:
            raise "Unimplemented"

        if config["reset"] and self.driver.databases.contains(self.database):
            logging.debug("Deleting database '%s'" % self.database)
            self.driver.databases.get(self.database).delete()
        
        if not self.driver.databases.contains(self.database):
            logging.debug("Creating database'%s'" % (self.database))
            self.driver.databases.create(self.database)
            logging.debug("Loading schema file'%s'" % (self.schema))
            script_dir = os.path.dirname(os.path.abspath(__file__))
            full_path = os.path.join(script_dir, self.schema)
            with open(full_path, 'r') as data:
                define_query = data.read()
            logging.debug("Writing schema")
            with self.driver.transaction(self.database, TransactionType.SCHEMA) as tx:
                tx.query(define_query)
                tx.commit()
            logging.debug("Committed schema")
        ## IF

    ## ----------------------------------------------
    ## Simple execution timer
    ## ----------------------------------------------
    def start_checkpoint(self, q):
        if self.debug:
            self.typedb_logger.debug(f"\n EXECUTING QUERY:\n{q}")
        return

    def end_checkpoint(self):
        if self.debug:
            self.typedb_logger.debug(f"--- Time taken: {(time.time() - self.execution_timer)} ---\n\n")
            self.execution_timer = time.time()
        return
    
    ## ----------------------------------------------
    ## loadStart
    ## ----------------------------------------------
    def loadStart(self):
        with open('typedb_log.log', 'w') as f:
            f.write('')
        return None

    ## ----------------------------------------------
    ## loadTuples
    ## ----------------------------------------------
    def loadTuples(self, tableName, tuples):
        if len(tuples) == 0: return

        if tableName != "ITEM" and self.items_complete_event and not self.items_complete_event.is_set():
            logging.info("Waiting for ITEM loading to be complete ...")
            self.items_complete_event.wait()  # We wait until item loading is complete
            logging.info("ITEM loading complete! Proceeding...")

        with self.driver.transaction(self.database, TransactionType.WRITE) as tx:
            write_query = [ ]

            if tableName == "WAREHOUSE":
                for tuple in tuples:
                    w_id = tuple[0]
                    w_name = tuple[1]
                    w_street_1 = tuple[2]
                    w_street_2 = tuple[3]
                    w_city = tuple[4]
                    w_state = tuple[5]
                    w_zip = tuple[6]
                    w_tax = tuple[7]
                    w_ytd = tuple[8]

                    q = f"""
    insert 
    $warehouse isa WAREHOUSE, 
    has W_ID {w_id}, has W_NAME "{w_name}", has W_STREET_1 "{w_street_1}", 
    has W_STREET_2 "{w_street_2}", has W_CITY "{w_city}", has W_STATE "{w_state}", 
    has W_ZIP "{w_zip}", has W_TAX {w_tax}, has W_YTD {w_ytd};
    reduce $count = count;"""
                    write_query.append(q)

            if tableName == "DISTRICT":
                for tuple in tuples:
                    d_id = tuple[0]
                    d_w_id = tuple[1]
                    d_name = tuple[2]
                    d_street_1 = tuple[3]
                    d_street_2 = tuple[4]
                    d_city = tuple[5]
                    d_state = tuple[6]
                    d_zip = tuple[7]
                    d_tax = tuple[8]
                    d_ytd = tuple[9]
                    d_next_o_id = tuple[10]

                    q = f"""
    match 
    $w isa WAREHOUSE, has W_ID {d_w_id};
    insert 
    $district links (warehouse: $w), isa DISTRICT,
    has D_ID {d_w_id * DPW + d_id}, has D_NAME "{d_name}",
    has D_STREET_1 "{d_street_1}", has D_STREET_2 "{d_street_2}",
    has D_CITY "{d_city}", has D_STATE "{d_state}", has D_ZIP "{d_zip}",
    has D_TAX {d_tax}, has D_YTD {d_ytd}, has D_NEXT_O_ID {d_next_o_id};
    reduce $count = count;"""
                    write_query.append(q)

            if tableName == "ITEM":
                for tuple in tuples:
                    i_id = tuple[0]
                    i_im_id = tuple[1]
                    i_name = tuple[2]
                    i_price = tuple[3]
                    i_data = tuple[4]

                    q = f"""
    insert 
    $item isa ITEM,
    has I_ID {i_id}, has I_IM_ID {i_im_id}, has I_NAME "{i_name}",
    has I_PRICE {i_price}, has I_DATA "{i_data}";
    reduce $count = count;"""
                    write_query.append(q)

            if tableName == "CUSTOMER":
                for tuple in tuples:
                    c_id = tuple[0]
                    c_d_id = tuple[1]
                    c_w_id = tuple[2]
                    c_first = tuple[3]
                    c_middle = tuple[4]
                    c_last = tuple[5]
                    c_street_1 = tuple[6]
                    c_street_2 = tuple[7]
                    c_city = tuple[8]
                    c_state = tuple[9]
                    c_zip = tuple[10]
                    c_phone = tuple[11]
                    c_since = tuple[12].isoformat()[:-3]
                    c_credit = tuple[13]
                    c_credit_lim = tuple[14]
                    c_discount = tuple[15]
                    c_balance = tuple[16]
                    c_ytd_payment = tuple[17]
                    c_payment_cnt = tuple[18]
                    c_delivery_cnt = tuple[19]
                    c_data = tuple[20]

                    q = f"""
    match
    $d isa DISTRICT, has D_ID {c_w_id * DPW + c_d_id};
    insert 
    $customer links (district: $d), isa CUSTOMER,
    has C_ID {c_w_id * DPW * CPD + c_d_id * CPD + c_id}, 
    has C_FIRST "{c_first}", has C_MIDDLE "{c_middle}", has C_LAST "{c_last}",
    has C_STREET_1 "{c_street_1}", has C_STREET_2 "{c_street_2}",
    has C_CITY "{c_city}", has C_STATE "{c_state}", has C_ZIP "{c_zip}",
    has C_PHONE "{c_phone}", has C_SINCE {c_since}, has C_CREDIT "{c_credit}",
    has C_CREDIT_LIM {c_credit_lim}, has C_DISCOUNT {c_discount},
    has C_BALANCE {c_balance}, has C_YTD_PAYMENT {c_ytd_payment},
    has C_PAYMENT_CNT {c_payment_cnt}, has C_DELIVERY_CNT {c_delivery_cnt},
    has C_DATA "{c_data}";
"""
                    write_query.append(q)

            if tableName == "ORDERS":
                for tuple in tuples:
                    o_id = tuple[0]
                    o_c_id = tuple[1]
                    o_d_id = tuple[2]
                    o_w_id = tuple[3]
                    o_entry_d = tuple[4].isoformat()[:-3]
                    o_carrier_id = tuple[5]
                    o_ol_cnt = tuple[6]
                    o_all_local = tuple[7]

                    q = f"""
    match 
    $d isa DISTRICT, has D_ID {o_w_id * DPW + o_d_id};
    $c isa CUSTOMER, has C_ID {o_w_id * DPW * CPD + o_d_id * CPD + o_c_id};
    insert 
    $o links (customer: $c, district: $d), isa ORDER,
    has O_ID {o_id},
    has O_ENTRY_D {o_entry_d}, has O_CARRIER_ID {o_carrier_id},
    has O_OL_CNT {o_ol_cnt}, has O_ALL_LOCAL {o_all_local}, has O_NEW_ORDER false;
"""
                    write_query.append(q)

            if tableName == "NEW_ORDER":
                for tuple in tuples:
                    no_o_id = tuple[0]
                    no_d_id = tuple[1]
                    no_w_id = tuple[2]

                    q = f"""
    match 
    $d isa DISTRICT, has D_ID {no_w_id * DPW + no_d_id};
    $o links (district: $d), isa ORDER, has O_ID {no_o_id}, has O_NEW_ORDER $status;
    delete $status of $o;
    insert $o has O_NEW_ORDER true;
"""
                    write_query.append(q)

            if tableName == "ORDER_LINE":
                for tuple in tuples:
                    ol_o_id = tuple[0]
                    ol_d_id = tuple[1]
                    ol_w_id = tuple[2]
                    ol_number = tuple[3]
                    ol_i_id = tuple[4]
                    ol_supply_w_id = tuple[5]
                    # See TPCC Spec: delivery date may be null
                    if tuple[6] is not None:
                        has_ol_delivery_d = f"has OL_DELIVERY_D {tuple[6].isoformat()[:-3]},"
                    else:
                        has_ol_delivery_d = ""
                    ol_quantity = tuple[7]
                    ol_amount = tuple[8]
                    ol_dist_info = tuple[9]

                    q = f"""
    match 
    $w isa WAREHOUSE, has W_ID {ol_w_id};
    $d isa DISTRICT, has D_ID {ol_w_id * DPW + ol_d_id};
    $order links (district: $d), isa ORDER, has O_ID {ol_o_id};
    $item has I_ID {ol_i_id};
    insert 
    $order_line links (order: $order, item: $item), isa ORDER_LINE,
    has OL_NUMBER {ol_number}, has OL_SUPPLY_W_ID {ol_supply_w_id},
    """ + has_ol_delivery_d + f"""
    has OL_QUANTITY {ol_quantity}, has OL_AMOUNT {ol_amount},
    has OL_DIST_INFO "{ol_dist_info}";
"""
                    write_query.append(q)
    
            if tableName == "STOCK":
                for tuple in tuples:
                    s_i_id = tuple[0]
                    s_w_id = tuple[1]
                    s_quantity = tuple[2]
                    s_ytd = tuple[13]
                    s_order_cnt = tuple[14]
                    s_remote_cnt = tuple[15]
                    s_data = tuple[16]

                    q_stock = f"""
    match 
    $i isa ITEM, has I_ID {s_i_id};   
    $w isa WAREHOUSE, has W_ID {s_w_id};
    insert 
    $stock links (item: $i, warehouse: $w), isa STOCKING, 
    has S_QUANTITY {s_quantity}, has S_YTD {s_ytd}, has S_ORDER_CNT {s_order_cnt},
    has S_REMOTE_CNT {s_remote_cnt}, has S_DATA "{s_data}";
"""
                    write_query.append(q_stock)
    
                    for i in range(1, 11):

                        q_stock_info = f"""
    match 
    $i isa ITEM, has I_ID {s_i_id};
    $w isa WAREHOUSE, has W_ID {s_w_id};   
    $stock links (item: $i, warehouse: $w), isa STOCKING;
    insert
    $stock has S_DIST_{i} "{tuple[2+i]}";
"""
                        write_query.append(q_stock_info)

    
            if tableName == "HISTORY":
                for tuple in tuples:
                    h_c_id = tuple[0]
                    h_d_id = tuple[3]
                    h_w_id = tuple[4]
                    h_date = tuple[5].isoformat()[:-3]
                    h_amount = tuple[6]
                    h_data = tuple[7]

                    # TODO: consider keeping track of warehouse w_id as well 
                    q = f"""
    match 
    $c isa CUSTOMER, has C_ID {h_w_id * DPW * CPD + h_d_id * CPD + h_c_id};
    insert 
    $history links (customer: $c), isa CUSTOMER_HISTORY,
    has H_DATE {h_date}, has H_AMOUNT {h_amount}, has H_DATA "{h_data}";
"""
                    write_query.append(q)

            if tableName not in DATA_COUNT:
                DATA_COUNT[tableName] = 0;
            DATA_COUNT[tableName] += len(tuples);

            start_time = time.time()
            promises = [ ]
            for q in write_query:
                promises.append(tx.query(q))

            for p in promises:
                p.resolve()
            tx.commit()
            logging.info(f"Wrote {len(tuples)} instances of {tableName} with TPQ: {(time.time() - start_time) / len(tuples)}")
        return

    ## ----------------------------------------------
    ## loadFinish
    ## ----------------------------------------------
    def loadFinish(self):
        logging.info("-- COMPLETE! Data loaded by this worker thread:\n%s --" % pformat(DATA_COUNT))
        return None
    
    ## ----------------------------------------------
    ## loadFinishItem
    ## ----------------------------------------------
    def loadFinishItem(self):
        if self.items_complete_event:
            self.items_complete_event.set()
        return None
    
    ## ----------------------------------------------
    ## loadFinishItem
    ## ----------------------------------------------
    def loadFinishDistrict(self, w_id, d_id, d_total):
        logging.info(f"-- Completed {int((d_id/d_total) * 100)}% of warehouse {w_id} --")
        return None

    ## ----------------------------------------------
    ## Post-load verification
    ## ----------------------------------------------
    def loadVerify(self):
        logging.info("TypeDB3:")
        logging.info(self.get_counts())

    ## ----------------------------------------------
    ## T1: doNewOrder
    ## ----------------------------------------------
    def doNewOrder(self, params):

        if self.debug:
            self.typedb_logger.debug("--- START doNewOrder ---")

        w_id = params["w_id"]
        d_id = params["d_id"]
        c_id = params["c_id"]
        o_entry_d = params["o_entry_d"].isoformat()[:-3]
        i_ids = params["i_ids"]
        i_w_ids = params["i_w_ids"]
        i_qtys = params["i_qtys"]
            
        assert len(i_ids) > 0
        assert len(i_ids) == len(i_w_ids)
        assert len(i_ids) == len(i_qtys)

        all_local = True
        total = 0
        items = [ ]
        item_data = [ ]
        ## Determine if this is an all local order or not
        for i in range(len(i_ids)):
            all_local = all_local and i_w_ids[i] == w_id

        with self.driver.transaction(self.database, TransactionType.WRITE) as tx:
            for i in range(len(i_ids)):
                q = f"""
match 
$i isa ITEM, has I_ID {i_ids[i]}, 
has I_NAME $i_name, has I_PRICE $i_price, 
has I_DATA $i_data; 
select $i_name, $i_price, $i_data;"""
                self.start_checkpoint(q)
                item = list(tx.query(q).resolve().as_concept_rows())
                self.end_checkpoint()
                if len(item) == 0:
                    return (None, 0)
                items.append({ 'name': item[0].get('i_name').as_attribute().get_value(), 
                                'price': item[0].get('i_price').as_attribute().get_value(), 
                                'data': item[0].get('i_data').as_attribute().get_value()})

            # Query: get warhouse, district, and customer info, then insert new order
            all_local_int = 1 if all_local else 0
            ol_cnt = len(i_ids)
            o_carrier_id = constants.NULL_CARRIER_ID
            q = f"""
match 
$w isa WAREHOUSE, has W_ID {w_id}, has W_TAX $w_tax;
$d isa DISTRICT, has D_ID {w_id * DPW + d_id}, has D_TAX $d_tax, has D_NEXT_O_ID $d_next_o_id;
$c isa CUSTOMER, has C_ID {w_id * DPW * CPD + d_id * CPD + c_id}, 
has C_DISCOUNT $c_discount, has C_LAST $c_last, has C_CREDIT $c_credit;
$d_next_o_id_old = $d_next_o_id;
$d_next_o_id_new = $d_next_o_id_old + 1;
delete 
$d_next_o_id of $d;
insert 
$d has D_NEXT_O_ID == $d_next_o_id_new;
$order links (district: $d, customer: $c), isa ORDER,
has O_ID == $d_next_o_id_old,
has O_ENTRY_D {o_entry_d}, has O_CARRIER_ID {o_carrier_id},
has O_OL_CNT {ol_cnt}, has O_ALL_LOCAL {all_local_int}, has O_NEW_ORDER true;
select $w_tax, $d_tax, $d_next_o_id_old, $c_discount, $c_last, $c_credit;"""
            self.start_checkpoint(q)
            general_info = list(tx.query(q).resolve().as_concept_rows())
            self.end_checkpoint()
            
            if len(general_info) == 0:
                logging.warning("No general info for warehouse %d" % w_id)
                self.typedb_logger.debug("--- FAILED ---")
                return (None, 0)
            w_tax = general_info[0].get('w_tax').as_attribute().get_value()
            d_tax = general_info[0].get('d_tax').as_attribute().get_value()
            d_next_o_id = general_info[0].get('d_next_o_id_old').get_long()
            c_discount = general_info[0].get('c_discount').as_attribute().get_value()
            c_last = general_info[0].get('c_last').as_attribute().get_value()
            c_credit = general_info[0].get('c_credit').as_attribute().get_value()

            for i in range(len(i_ids)):
                ol_number = i + 1
                ol_supply_w_id = i_w_ids[i]
                ol_i_id = i_ids[i]
                ol_quantity = i_qtys[i]

                i_name = items[i]['name']
                i_data = items[i]['data']
                i_price = items[i]['price']

                # Query: get stock info of item i
                q = f"""
match
$i isa ITEM, has I_ID {ol_i_id}; 
$w isa WAREHOUSE, has W_ID {ol_supply_w_id};
$s links (item: $i, warehouse: $w), isa STOCKING, 
has S_QUANTITY $s_quantity, has S_DATA $s_data, has S_YTD $s_ytd, 
has S_ORDER_CNT $s_order_cnt, has S_REMOTE_CNT $s_remote_cnt, 
has S_DIST_{d_id} $s_dist_xx;
select $s_quantity, $s_data, $s_ytd, $s_order_cnt, $s_remote_cnt, $s_dist_xx;"""
                self.start_checkpoint(q)
                stock_info = list(tx.query(q).resolve().as_concept_rows())
                self.end_checkpoint()

                if len(stock_info) == 0:
                    logging.warning("No STOCK record for (ol_i_id=%d, ol_supply_w_id=%d)" % (ol_i_id, ol_supply_w_id))
                    continue
                s_quantity = stock_info[0].get('s_quantity').as_attribute().get_value()
                s_data = stock_info[0].get('s_data').as_attribute().get_value()
                s_ytd = stock_info[0].get('s_ytd').as_attribute().get_value()
                s_order_cnt = stock_info[0].get('s_order_cnt').as_attribute().get_value()
                s_remote_cnt = stock_info[0].get('s_remote_cnt').as_attribute().get_value()
                s_dist_xx = stock_info[0].get('s_dist_xx').as_attribute().get_value()
                
                # Compute auxilliary values
                s_ytd += ol_quantity
                if s_quantity >= ol_quantity + 10:
                    s_quantity = s_quantity - ol_quantity
                else:
                    s_quantity = s_quantity + 91 - ol_quantity
                s_order_cnt += 1
                
                if ol_supply_w_id != w_id: 
                    s_remote_cnt += 1

                if i_data.find(constants.ORIGINAL_STRING) != -1 and s_data.find(constants.ORIGINAL_STRING) != -1:
                    brand_generic = 'B'
                else:
                    brand_generic = 'G'

                ol_amount = ol_quantity * i_price
                # Query: update stock info of item i
                q = f"""
match
$i isa ITEM, has I_ID {ol_i_id};
$w isa WAREHOUSE, has W_ID {ol_supply_w_id};
$d isa DISTRICT, has D_ID {w_id * DPW + d_id};
$o links (district: $d), isa ORDER, has O_ID {d_next_o_id};
$s links (item: $i, warehouse: $w), isa STOCKING, 
has S_QUANTITY $s_quantity, has S_YTD $s_ytd, 
has S_ORDER_CNT $s_order_cnt, has S_REMOTE_CNT $s_remote_cnt;
delete 
$s_quantity of $s;
$s_ytd of $s;
$s_order_cnt of $s;
$s_remote_cnt of $s;
insert 
$s has S_QUANTITY {s_quantity}, has S_YTD {s_ytd}, 
has S_ORDER_CNT {s_order_cnt}, has S_REMOTE_CNT {s_remote_cnt};
(item: $i, order: $o) isa ORDER_LINE, 
has OL_NUMBER {ol_number}, has OL_SUPPLY_W_ID {ol_supply_w_id}, 
has OL_QUANTITY {ol_quantity}, has OL_AMOUNT {ol_amount}, has OL_DIST_INFO "{s_dist_xx}";
reduce $count = count;"""
                self.start_checkpoint(q)
                count = list(tx.query(q).resolve().as_concept_rows())[0].get('count').as_value().get_long()
                self.end_checkpoint()
                assert count == 1, "Expected 1 ORDER_LINE to be inserted"

                ## Transaction profile states to use "ol_quantity * i_price"
                total += ol_amount
    
                ## Add the info to be returned
                item_data.append( (i_name, s_quantity, brand_generic, i_price, ol_amount) )
            ## FOR

            tx.commit()
            total *= (1 - c_discount) * (1 + w_tax + d_tax)

            ## Pack up values the client is missing (see TPC-C 2.4.3.5)
            misc = [ (w_tax, d_tax, d_next_o_id, total) ]
            return ([ [c_discount, c_last, c_credit], misc, item_data ], 0)
        ## WITH

    ## ----------------------------------------------
    ## T2: doDelivery
    ## ----------------------------------------------
    def doDelivery(self, params):

        if self.debug:
            self.typedb_logger.debug("--- START doDelivery ---")
        
        w_id = params["w_id"]
        o_carrier_id = params["o_carrier_id"]
        ol_delivery_d = params["ol_delivery_d"].isoformat()[:-3]

        with self.driver.transaction(self.database, TransactionType.WRITE) as tx:
            result = [ ]
            for d_id in range(1, constants.DISTRICTS_PER_WAREHOUSE+1):
                q = f"""
match
$d isa DISTRICT, has D_ID {w_id * DPW + d_id};
$o links (customer: $c, district: $d), isa ORDER, has O_ID $o_id, has O_NEW_ORDER true;
$c isa CUSTOMER, has C_ID $c_id;
select $o_id, $c_id;
"""
                self.start_checkpoint(q)
                new_order_info = list(tx.query(q).resolve().as_concept_rows())
                self.end_checkpoint()
                if len(new_order_info) == 0:
                    ## No orders for this district: skip it. Note: This must be reported if > 1%
                    continue
                assert new_order_info is not None
                no_o_id = new_order_info[0].get('o_id').as_attribute().get_value()
                c_id = new_order_info[0].get('c_id').as_attribute().get_value() % CPD

                q = f"""
match
$d isa DISTRICT, has D_ID {w_id * DPW + d_id};
$o links (district: $d), isa ORDER, has O_ID {no_o_id};
$ol links (order: $o, item: $i),  isa ORDER_LINE, has OL_AMOUNT $ol_amount;
select $ol_amount;
reduce $sum = sum($ol_amount);
"""
                self.start_checkpoint(q)
                response = list(tx.query(q).resolve().as_concept_rows())[0]
                self.end_checkpoint()
                ol_total = response.get("sum").as_value().get_double()
                # If there are no order lines, SUM returns null. There should always be order lines.
                assert ol_total != None, "ol_total is NULL: there are no order lines. This should not happen"
                assert ol_total > 0.0

                
                q = f"""
match
$c isa CUSTOMER, has C_ID {w_id * DPW * CPD + d_id * CPD + c_id}, has C_BALANCE $c_balance;
$c_balance_new = $c_balance + {ol_total};
$o links (customer: $c), isa ORDER, has O_ID {no_o_id}, has O_NEW_ORDER $o_new_order, has O_CARRIER_ID $o_carrier_id;
delete 
$o_new_order of $o;
$o_carrier_id of $o;
$c_balance of $c;
insert 
$o has O_NEW_ORDER false, has O_CARRIER_ID {o_carrier_id};
$c has C_BALANCE == $c_balance_new;
select $o;
match 
$ol links  (order: $o), isa ORDER_LINE;
insert
$ol has OL_DELIVERY_D {ol_delivery_d};
"""
                self.start_checkpoint(q)
                tx.query(q).resolve()
                self.end_checkpoint()

#                 q = f"""
# match
# $d isa DISTRICT, has D_ID {w_id * DPW + d_id};
# $o links (district: $d), isa ORDER, has O_ID {no_o_id};
# $ol links  (order: $o, item: $i), isa ORDER_LINE;
# insert
# $ol has OL_DELIVERY_D {ol_delivery_d};
# """
#                 self.start_checkpoint(q)
#                 tx.query(q).resolve()
#                 self.end_checkpoint()



                # These must be logged in the "result file" according to TPC-C 2.7.2.2 (page 39)
                # We remove the queued time, completed time, w_id, and o_carrier_id: the client can figure
                # them out
                result.append((d_id, no_o_id))
            ## FOR

            tx.commit()
        return (result,0)

    ## ----------------------------------------------
    ## T3: doOrderStatus
    ## ----------------------------------------------
    def doOrderStatus(self, params):

        if self.debug:
            self.typedb_logger.debug("--- START doOrderStatus ---")

        w_id = params["w_id"]
        d_id = params["d_id"]
        c_id = params["c_id"]
        c_last = params["c_last"]
        
        assert w_id, pformat(params)
        assert d_id, pformat(params)
        with self.driver.transaction(self.database, TransactionType.WRITE) as tx:
            result = [ ]
            if c_id != None:
                q = f"""
match
$c isa CUSTOMER, has C_ID {w_id * DPW * CPD + d_id * CPD + c_id},
has C_FIRST $c_first, has C_MIDDLE $c_middle, has C_LAST $c_last,
has C_BALANCE $c_balance;
select $c_first, $c_middle, $c_last, $c_balance;
"""
                customer = list(tx.query(q).resolve().as_concept_rows())
                assert len(customer) == 1, f"doOrderStatus: no customer found for w_id {w_id}, d_id {d_id}, c_id {c_id}"
                customer = customer[0]
            else:
                # TODO: check whether it's faster to constrain customer through C_ID range
                q = f"""
match
$d isa DISTRICT, has D_ID {w_id * DPW + d_id};
$c links (district: $d), isa CUSTOMER, has C_ID $c_id,
has C_FIRST $c_first, has C_MIDDLE $c_middle, has C_LAST $c_last,
has C_BALANCE $c_balance;
$c_last == "{c_last}";
select $c_id, $c_first, $c_middle, $c_last,$c_balance;
sort $c_first asc;
"""
                self.start_checkpoint(q)
                # Get the midpoint customer's id
                all_customers = list(tx.query(q).resolve().as_concept_rows())
                self.end_checkpoint()
                assert len(all_customers) > 0
                index = (len(all_customers) - 1) // 2
                customer = all_customers[index]
                c_id = customer.get('c_id').as_attribute().get_value() % CPD

            assert customer is not None
            assert c_id != None
            customer_data = [
                c_id,
                customer.get('c_first').as_attribute().get_value(),
                customer.get('c_middle').as_attribute().get_value(),
                customer.get('c_last').as_attribute().get_value(),
                customer.get('c_balance').as_attribute().get_value(),
            ]


            q = f"""
match
$c isa CUSTOMER, has C_ID {w_id * DPW * CPD + d_id * CPD + c_id};
$o links (customer: $c), isa ORDER, has O_ID $o_id;
select $o_id;
sort $o_id desc;
limit 1;"""
            self.start_checkpoint(q)
            order = list(tx.query(q).resolve().as_concept_rows())
            self.end_checkpoint()
            orderLines_data = [ ]

            if len(order) > 0:
                o_id = order[0].get('o_id').as_attribute().get_value()
                # OL_I_ID, OL_SUPPLY_W_ID, OL_QUANTITY, OL_AMOUNT, OL_DIST_INFO 
                q = f"""
match
$c isa CUSTOMER, has C_ID {w_id * DPW * CPD + d_id * CPD + c_id};
$o links (customer: $c), isa ORDER, has O_ID {o_id};
$i isa ITEM, has I_ID $i_id;
$ol links (order: $o, item: $i), isa ORDER_LINE, 
has OL_SUPPLY_W_ID $ol_supply_w_id, has OL_QUANTITY $ol_quantity, 
has OL_AMOUNT $ol_amount, has OL_DIST_INFO $ol_dist_info;
select $i_id, $ol_supply_w_id, $ol_quantity, $ol_amount, $ol_dist_info;"""
                self.start_checkpoint(q)
                orderLines = list(tx.query(q).resolve().as_concept_rows())
                self.end_checkpoint()
                for orderLine in orderLines:
                    orderLines_data.append([
                        orderLine.get('i_id').as_attribute().get_value(),
                        orderLine.get('ol_supply_w_id').as_attribute().get_value(),
                        orderLine.get('ol_quantity').as_attribute().get_value(),
                        orderLine.get('ol_amount').as_attribute().get_value(),
                        orderLine.get('ol_dist_info').as_attribute().get_value(),
                    ])
            else:
                o_id = None

            tx.commit()
            return ([ customer_data, [o_id] if o_id else [], orderLines_data ],0)

    ## ----------------------------------------------
    ## T4: doPayment
    ## ----------------------------------------------    
    def doPayment(self, params):

        if self.debug:
            self.typedb_logger.debug("--- START doPayment ---")

        w_id = params["w_id"]
        d_id = params["d_id"]
        h_amount = params["h_amount"]
        c_w_id = params["c_w_id"]
        c_d_id = params["c_d_id"]
        c_id = params["c_id"]
        c_last = params["c_last"]
        h_date = params["h_date"].isoformat()[:-3]

        with self.driver.transaction(self.database, TransactionType.WRITE) as tx:
            if c_id != None:
                q = f"""
match
$c isa CUSTOMER, has C_ID $c_id,
has C_FIRST $c_first, has C_MIDDLE $c_middle, has C_LAST $c_last,
has C_STREET_1 $c_street_1, has C_STREET_2 $c_street_2, has C_CITY $c_city,
has C_STATE $c_state, has C_ZIP $c_zip, has C_PHONE $c_phone,
has C_SINCE $c_since, has C_CREDIT $c_credit, has C_CREDIT_LIM $c_credit_lim,
has C_DISCOUNT $c_discount, has C_BALANCE $c_balance, has C_YTD_PAYMENT $c_ytd_payment, 
has C_PAYMENT_CNT $c_payment_cnt, has C_DATA $c_data;
$c_id == {w_id * DPW * CPD + d_id * CPD + c_id};
select $c_id, $c_first, $c_middle, $c_last, $c_street_1, $c_street_2, $c_city,
$c_state, $c_zip, $c_phone, $c_since, $c_credit, $c_credit_lim, $c_discount,
$c_balance, $c_ytd_payment, $c_payment_cnt, $c_data;
"""
                self.start_checkpoint(q)
                customer = list(tx.query(q).resolve().as_concept_rows())
                self.end_checkpoint()
                assert len(customer) == 1, f"doPayment: no customer found for w_id {w_id}, d_id {d_id}, c_id {c_id}"
                customer = customer[0]
            else:
                # TODO: check whether it's faster to constrain customer through C_ID range
                q = f"""
match
$d isa DISTRICT, has D_ID {w_id * DPW + d_id};
$c links (district: $d), isa CUSTOMER, has C_ID $c_id,
has C_FIRST $c_first, has C_MIDDLE $c_middle, has C_LAST $c_last,
has C_STREET_1 $c_street_1, has C_STREET_2 $c_street_2, has C_CITY $c_city,
has C_STATE $c_state, has C_ZIP $c_zip, has C_PHONE $c_phone,
has C_SINCE $c_since, has C_CREDIT $c_credit, has C_CREDIT_LIM $c_credit_lim,
has C_DISCOUNT $c_discount, has C_BALANCE $c_balance, has C_YTD_PAYMENT $c_ytd_payment, 
has C_PAYMENT_CNT $c_payment_cnt, has C_DATA $c_data;
$c_last == "{c_last}";
select $c_id, $c_first, $c_middle, $c_last, $c_street_1, $c_street_2, $c_city,
$c_state, $c_zip, $c_phone, $c_since, $c_credit, $c_credit_lim, $c_discount,
$c_balance, $c_ytd_payment, $c_payment_cnt, $c_data;
sort $c_first asc;
"""
                self.start_checkpoint(q)
                # Get the midpoint customer's id
                all_customers = list(tx.query(q).resolve().as_concept_rows())
                self.end_checkpoint()
                assert len(all_customers) > 0, f"doPayment: no customer found for w_id {w_id}, d_id {d_id}, c_last {c_last}"
                namecnt = len(all_customers)
                index = (namecnt-1) // 2
                customer = all_customers[index]
                c_id = customer.get('c_id').as_attribute().get_value() % CPD
            assert customer is not None
            assert c_id != None
            customer_data = [
                c_id,
                customer.get('c_first').as_attribute().get_value(),
                customer.get('c_middle').as_attribute().get_value(),
                customer.get('c_last').as_attribute().get_value(),
                customer.get('c_street_1').as_attribute().get_value(),
                customer.get('c_street_2').as_attribute().get_value(),
                customer.get('c_city').as_attribute().get_value(),
                customer.get('c_state').as_attribute().get_value(),
                customer.get('c_zip').as_attribute().get_value(),
                customer.get('c_phone').as_attribute().get_value(),
                customer.get('c_since').as_attribute().get_value(),
                customer.get('c_credit').as_attribute().get_value(),
                customer.get('c_credit_lim').as_attribute().get_value(),
                customer.get('c_discount').as_attribute().get_value(),
                customer.get('c_balance').as_attribute().get_value(),
                customer.get('c_ytd_payment').as_attribute().get_value(),
                customer.get('c_payment_cnt').as_attribute().get_value(),
                customer.get('c_data').as_attribute().get_value(),
            ]
            c_balance = customer_data[14] - h_amount
            c_ytd_payment = customer_data[15] + h_amount
            c_payment_cnt = customer_data[16] + 1
            c_data = customer_data[17]
            # W_NAME, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP
            q = f"""
match
$w isa WAREHOUSE, has W_ID {w_id}, has W_NAME $w_name, 
has W_STREET_1 $w_street_1, has W_STREET_2 $w_street_2, 
has W_CITY $w_city, has W_STATE $w_state, has W_ZIP $w_zip, has W_YTD $w_ytd;
$w_ytd_new = $w_ytd + {h_amount};
delete $w_ytd of $w;
insert $w has W_YTD == $w_ytd_new;
select $w_name, $w_street_1, $w_street_2, $w_city, $w_state, $w_zip;
"""
            self.start_checkpoint(q)
            warehouse = list(tx.query(q).resolve().as_concept_rows())
            self.end_checkpoint()
            assert len(warehouse) == 1
            warehouse_data = [
                warehouse[0].get('w_name').as_attribute().get_value(),
                warehouse[0].get('w_street_1').as_attribute().get_value(),
                warehouse[0].get('w_street_2').as_attribute().get_value(),
                warehouse[0].get('w_city').as_attribute().get_value(),
                warehouse[0].get('w_state').as_attribute().get_value(),
                warehouse[0].get('w_zip').as_attribute().get_value(),
            ]

            # D_NAME, D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP
            q = f"""
match
$d isa DISTRICT, has D_ID {w_id * DPW + d_id}, 
has D_NAME $d_name, has D_STREET_1 $d_street_1, 
has D_STREET_2 $d_street_2, has D_CITY $d_city, 
has D_STATE $d_state, has D_ZIP $d_zip, has D_YTD $d_ytd;
$d_ytd_new = $d_ytd + {h_amount};
delete $d_ytd of $d;
insert $d has D_YTD == $d_ytd_new;
select $d_name, $d_street_1, $d_street_2, $d_city, $d_state, $d_zip;
"""
            self.start_checkpoint(q)
            district = list(tx.query(q).resolve().as_concept_rows())
            self.end_checkpoint()
            assert len(district) == 1
            district_data = [
                district[0].get('d_name').as_attribute().get_value(),
                district[0].get('d_street_1').as_attribute().get_value(),
                district[0].get('d_street_2').as_attribute().get_value(),
                district[0].get('d_city').as_attribute().get_value(),
                district[0].get('d_state').as_attribute().get_value(),
                district[0].get('d_zip').as_attribute().get_value(),
            ]
            # UPDATE DISTRICT SET D_YTD = D_YTD + ? WHERE D_W_ID  = ? AND D_ID = ?
# 
            # q = f"""
# match
# $w isa WAREHOUSE, has W_ID {w_id}, has W_YTD $w_ytd;
# $w_ytd_new = $w_ytd + {h_amount};
# delete $w_ytd of $w;
# insert $w has W_YTD == $w_ytd_new;
# """
            # self.start_checkpoint(q)
            # tx.query(q).resolve()
            # self.end_checkpoint()
# 
            # q = f"""
# match
# $d isa DISTRICT, has D_ID {w_id * DPW + d_id}, has D_YTD $d_ytd;
# $d_ytd_new = $d_ytd + {h_amount};
# delete $d_ytd of $d;
# insert $d has D_YTD == $d_ytd_new;
# """
            # self.start_checkpoint(q)
            # tx.query(q).resolve()
            # self.end_checkpoint()
# 
            h_data = "%s    %s" % (warehouse_data[0], district_data[0])

            # Update customers and history
            if customer_data[11] == constants.BAD_CREDIT:
                newData = " ".join(map(str, [c_id, c_d_id, c_w_id, d_id, w_id, h_amount]))
                c_data = (newData + "|" + c_data)
                if len(c_data) > constants.MAX_C_DATA: c_data = c_data[:constants.MAX_C_DATA]
                # "updateBCCustomer": "UPDATE CUSTOMER SET C_BALANCE = ?, C_YTD_PAYMENT = ?, C_PAYMENT_CNT = ?, C_DATA = ? WHERE C_W_ID = ? AND C_D_ID = ? AND C_ID = ?", # c_balance, c_ytd_payment, c_payment_cnt, c_data, c_w_id, c_d_id, c_id
                q = f"""
match
$c isa CUSTOMER, has C_ID {c_w_id * DPW * CPD + c_d_id * CPD + c_id}, 
has C_BALANCE $c_balance, has C_YTD_PAYMENT $c_ytd_payment, 
has C_PAYMENT_CNT $c_payment_cnt, has C_DATA $c_data;
delete 
$c_balance of $c; 
$c_ytd_payment of $c;
$c_payment_cnt of $c; 
$c_data of $c;
insert $c has C_BALANCE {c_balance}, has C_YTD_PAYMENT {c_ytd_payment}, 
has C_PAYMENT_CNT {c_payment_cnt}, has C_DATA "{c_data}";
$h links (customer: $c), isa CUSTOMER_HISTORY, has H_DATE {h_date}, has H_AMOUNT {h_amount}, has H_DATA "{h_data}";
"""            # TODO: if histories keep track of w_id's this needs to be changed as well
                self.start_checkpoint(q)
                tx.query(q).resolve()
                self.end_checkpoint()
            else:
                q = f"""
match
$c isa CUSTOMER, has C_ID {c_w_id * DPW * CPD + c_d_id * CPD + c_id}, 
has C_BALANCE $c_balance, has C_YTD_PAYMENT $c_ytd_payment, 
has C_PAYMENT_CNT $c_payment_cnt;
delete 
$c_balance of $c; 
$c_ytd_payment of $c;
$c_payment_cnt of $c;
insert 
$c has C_BALANCE {c_balance}, has C_YTD_PAYMENT {c_ytd_payment}, 
has C_PAYMENT_CNT {c_payment_cnt};
$h links (customer: $c), isa CUSTOMER_HISTORY, has H_DATE {h_date}, has H_AMOUNT {h_amount}, has H_DATA "{h_data}";
"""             # TODO: if histories keep track of w_id's this needs to be changed as well
                self.start_checkpoint(q)
                tx.query(q).resolve()
                self.end_checkpoint()

            # TPC-C 2.5.3.3: Must display the following fields:
            # W_ID, D_ID, C_ID, C_D_ID, C_W_ID, W_STREET_1, W_STREET_2, W_CITY, W_STATE, W_ZIP,
            # D_STREET_1, D_STREET_2, D_CITY, D_STATE, D_ZIP, C_FIRST, C_MIDDLE, C_LAST, C_STREET_1,
            # C_STREET_2, C_CITY, C_STATE, C_ZIP, C_PHONE, C_SINCE, C_CREDIT, C_CREDIT_LIM,
            # C_DISCOUNT, C_BALANCE, the first 200 characters of C_DATA (only if C_CREDIT = "BC"),
            # H_AMOUNT, and H_DATE.
            
            tx.commit()
            return ([ warehouse_data, district_data, customer_data ],0)


        
    ## ----------------------------------------------
    ## T5: doStockLevel
    ## ----------------------------------------------    
    def doStockLevel(self, params):

        if self.debug:
            self.typedb_logger.debug("--- START doStockLevel ---")

        w_id = params["w_id"]
        d_id = params["d_id"]
        threshold = params["threshold"]
        
        with self.driver.transaction(self.database, TransactionType.WRITE) as tx:
            q = f"""
match
$d isa DISTRICT, has D_ID {w_id * DPW + d_id}, has D_NEXT_O_ID $d_next_o_id;
select $d_next_o_id;
"""
            self.start_checkpoint(q)
            result = list(tx.query(q).resolve().as_concept_rows())
            self.end_checkpoint()
            assert len(result) == 1, f"doStockLevel: no district found for w_id {w_id}, d_id {d_id}"
            o_id = result[0].get('d_next_o_id').as_attribute().get_value()

            q = f"""
match
$w isa WAREHOUSE, has W_ID {w_id};
$d isa DISTRICT, has D_ID {w_id * DPW + d_id};
$s links (item: $i, warehouse: $w), isa STOCKING, has S_QUANTITY < {threshold};
$ol links  (item: $i, order: $o), isa ORDER_LINE;
$o links (district: $d), isa ORDER, has O_ID $o_id;
$o_id < {o_id};
$o_id >= {o_id - 20};
select $i;
reduce $count = count;"""
            self.start_checkpoint(q)
            # Todo
            first_response = list(tx.query(q).resolve().as_concept_rows())[0]
            self.end_checkpoint()
            result = first_response.get('count').get_long()
            
            tx.commit()
            
            return (int(result),0)
           
    ## ----------------------------------------------
    ## Post-execution verification
    ## ----------------------------------------------
    def executeVerify(self):
        logging.info("TypeDB3:")
        logging.info(self.get_counts())

    def get_counts(self):      
        tables = ["ITEM", "WAREHOUSE", "DISTRICT", "CUSTOMER", "STOCK", "ORDERS", "NEW_ORDER", "ORDER_LINE", "CUSTOMER_HISTORY"]
        with self.driver.transaction(self.database, TransactionType.READ) as txn:
            verification = "\n{\n"
            for table in tables:
                if table == "ORDERS":
                    q = f"match $t isa ORDER; reduce $count = count;"
                elif table == "NEW_ORDER":
                    q = f"match $t isa ORDER, has O_NEW_ORDER true; reduce $count = count;"
                elif table == "STOCK":
                    q = f"match $t isa STOCKING; reduce $count = count;"
                else:
                    q = f"match $t isa {table}; reduce $count = count;"
                result = list(txn.query(q).resolve().as_concept_rows())
                count = result[0].get('count').get_long()
                verification += f"    \"{table}\": {count}\n"
            verification += "}"
            return verification

## CLASS
