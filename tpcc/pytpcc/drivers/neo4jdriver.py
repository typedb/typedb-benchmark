from __future__ import with_statement

import os
from neo4j import GraphDatabase
import getpass
import logging
from pprint import pformat

import sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
import constants
from drivers.abstractdriver import AbstractDriver


## ==============================================
## Neo4JDriver
## ==============================================
class Neo4JDriver(AbstractDriver):
    DEFAULT_CONFIG = {
        "uri": ("The URI for the Neo4j database", "bolt://localhost:7687"),
        "database": ("The name of the Neo4j database", "neo4j"),
        "user": ("The username to connect to the Neo4j database", "neo4j"),
        "password": ("The password to connect to the Neo4j database", "password"), ## Neo4j requires setting this
    }
    
    def __init__(self, ddl, shared_event=None):
        super(Neo4JDriver, self).__init__("neo4j", ddl)
        self.driver = None
        self.session = None
        self.uri = None
        self.user = None
        self.password = None
        self.items_complete_event = shared_event
    
    ## ----------------------------------------------
    ## makeDefaultConfig
    ## ----------------------------------------------
    def makeDefaultConfig(self):
        return Neo4JDriver.DEFAULT_CONFIG
    
    ## ----------------------------------------------
    ## loadConfig
    ## ----------------------------------------------
    def loadConfig(self, config):
        for key in Neo4JDriver.DEFAULT_CONFIG.keys():
            assert key in config, f"Missing parameter '{key}' in Neo4j configuration"
        
        self.database = str(config["database"])
        self.uri = str(config["uri"])
        self.user = str(config["user"])
        self.password = str(config["password"])

        try:
            self.driver = GraphDatabase.driver(self.uri, auth=(self.user, self.password))
            logging.info(f"Connected to Neo4j database: {self.uri}")
        except Exception as e:
            logging.error(f"Failed to connect to Neo4j database: {e}")
            raise

        if config["reset"]:
            with self.driver.session(database=self.database) as session:
                # Clear the entire database
                session.run("MATCH (n) DETACH DELETE n")
                logging.info("Cleared all nodes and relationships from the database")

    ## ----------------------------------------------
    ## loadStart
    ## ----------------------------------------------
    def loadStart(self):
        pass  # Neo4j doesn't require special handling for bulk loading

    ## ----------------------------------------------
    ## loadTuples
    ## ----------------------------------------------
    def loadTuples(self, tableName, tuples):
        if len(tuples) == 0: return

        with self.driver.session(database=self.database) as session:

            if tableName == "ITEM":
                pass
            elif self.items_complete_event and not self.items_complete_event.is_set():
                logging.info("Waiting for ITEM loading to be complete ...")
                self.items_complete_event.wait()  # We wait until item loading is complete
                logging.info("ITEM loading complete! Proceeding...")

            if tableName == "WAREHOUSE":
                for tuple in tuples:
                    session.run("""
                    CREATE (w:WAREHOUSE {
                        W_ID: $w_id, W_NAME: $w_name, W_STREET_1: $w_street_1,
                        W_STREET_2: $w_street_2, W_CITY: $w_city, W_STATE: $w_state,
                        W_ZIP: $w_zip, W_TAX: $w_tax, W_YTD: $w_ytd
                    })
                    """, w_id=tuple[0], w_name=tuple[1], w_street_1=tuple[2],
                    w_street_2=tuple[3], w_city=tuple[4], w_state=tuple[5],
                    w_zip=tuple[6], w_tax=tuple[7], w_ytd=tuple[8])

            elif tableName == "DISTRICT":
                for tuple in tuples:
                    session.run("""
                    MATCH (w:WAREHOUSE {W_ID: $w_id})
                    CREATE (d:DISTRICT {
                        D_ID: $d_id, D_NAME: $d_name, D_STREET_1: $d_street_1,
                        D_STREET_2: $d_street_2, D_CITY: $d_city, D_STATE: $d_state,
                        D_ZIP: $d_zip, D_TAX: $d_tax, D_YTD: $d_ytd, D_NEXT_O_ID: $d_next_o_id
                    }) -[:BELONGS_TO]-> (w)
                    """, w_id=tuple[1], d_id=tuple[0], d_name=tuple[2], d_street_1=tuple[3],
                    d_street_2=tuple[4], d_city=tuple[5], d_state=tuple[6], d_zip=tuple[7],
                    d_tax=tuple[8], d_ytd=tuple[9], d_next_o_id=tuple[10])

            elif tableName == "CUSTOMER":
                for tuple in tuples:
                    session.run("""
                    MATCH (d:DISTRICT {D_ID: $d_id}) 
                           -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                    CREATE (c:CUSTOMER {
                        C_ID: $c_id, C_FIRST: $c_first, C_MIDDLE: $c_middle, C_LAST: $c_last,
                        C_STREET_1: $c_street_1, C_STREET_2: $c_street_2, C_CITY: $c_city,
                        C_STATE: $c_state, C_ZIP: $c_zip, C_PHONE: $c_phone, C_SINCE: $c_since,
                        C_CREDIT: $c_credit, C_CREDIT_LIM: $c_credit_lim, C_DISCOUNT: $c_discount,
                        C_BALANCE: $c_balance, C_YTD_PAYMENT: $c_ytd_payment,
                        C_PAYMENT_CNT: $c_payment_cnt, C_DELIVERY_CNT: $c_delivery_cnt,
                        C_DATA: $c_data
                    }) -[:BELONGS_TO]-> (d)
                    """, w_id=tuple[2], d_id=tuple[1], c_id=tuple[0], c_first=tuple[3],
                    c_middle=tuple[4], c_last=tuple[5], c_street_1=tuple[6], c_street_2=tuple[7],
                    c_city=tuple[8], c_state=tuple[9], c_zip=tuple[10], c_phone=tuple[11],
                    c_since=tuple[12], c_credit=tuple[13], c_credit_lim=tuple[14],
                    c_discount=tuple[15], c_balance=tuple[16], c_ytd_payment=tuple[17],
                    c_payment_cnt=tuple[18], c_delivery_cnt=tuple[19], c_data=tuple[20])

            elif tableName == "ITEM":
                for tuple in tuples:
                    session.run("""
                    CREATE (i:ITEM {
                        I_ID: $i_id, I_IM_ID: $i_im_id, I_NAME: $i_name,
                        I_PRICE: $i_price, I_DATA: $i_data
                    })
                    """, i_id=tuple[0], i_im_id=tuple[1], i_name=tuple[2],
                    i_price=tuple[3], i_data=tuple[4])

            elif tableName == "ORDERS":
                for tuple in tuples:
                    session.run("""
                    MATCH (c:CUSTOMER {C_ID: $c_id}) 
                           -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id}) 
                           -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                    CREATE (o:ORDER {
                        O_ID: $o_id, O_ENTRY_D: $o_entry_d, O_CARRIER_ID: $o_carrier_id,
                        O_OL_CNT: $o_ol_cnt, O_ALL_LOCAL: $o_all_local
                    }) -[:PLACED_BY]-> (c)
                    """, o_id=tuple[0], c_id=tuple[1], d_id=tuple[2], w_id=tuple[3],
                    o_entry_d=tuple[4], o_carrier_id=tuple[5], o_ol_cnt=tuple[6],
                    o_all_local=tuple[7])

            elif tableName == "NEW_ORDER":
                for tuple in tuples:
                    session.run("""
                    MATCH (o:ORDER {O_ID: $o_id}) 
                           -[:PLACED_BY]-> (:CUSTOMER) 
                           -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id}) 
                           -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                    SET o.O_NEW_ORDER = true
                    """, o_id=tuple[0], d_id=tuple[1], w_id=tuple[2])

            elif tableName == "ORDER_LINE":
                for tuple in tuples:
                    session.run("""
                    MATCH (o:ORDER {O_ID: $o_id}) 
                           -[:PLACED_BY]-> (:CUSTOMER) 
                           -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id}) 
                           -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                    MATCH (i:ITEM {I_ID: $i_id})
                    CREATE (ol:ORDER_LINE {
                        OL_NUMBER: $ol_number, OL_SUPPLY_W_ID: $ol_supply_w_id,
                        OL_DELIVERY_D: $ol_delivery_d, OL_QUANTITY: $ol_quantity,
                        OL_AMOUNT: $ol_amount, OL_DIST_INFO: $ol_dist_info
                    }) -[:PART_OF]-> (o),
                    (ol) -[:CONTAINS]-> (i)
                    """, o_id=tuple[0], d_id=tuple[1], w_id=tuple[2], ol_number=tuple[3],
                    i_id=tuple[4], ol_supply_w_id=tuple[5], ol_delivery_d=tuple[6],
                    ol_quantity=tuple[7], ol_amount=tuple[8], ol_dist_info=tuple[9])

            elif tableName == "HISTORY":
                for tuple in tuples:
                    session.run("""
                    MATCH (c:CUSTOMER {C_ID: $c_id}) 
                           -[:BELONGS_TO]-> (dc:DISTRICT {D_ID: $d_c_id}) 
                           -[:BELONGS_TO]-> (wc:WAREHOUSE {W_ID: $w_c_id})
                    MATCH (d:DISTRICT {D_ID: $d_id}) -[:BELONGS_TO]-> (w:WAREHOUSE {W_ID: $w_id})
                    CREATE (h:HISTORY {
                        H_DATE: $h_date, H_AMOUNT: $h_amount, H_DATA: $h_data
                    }) -[:CUSTOMER_HISTORY]-> (c),
                    (h) -[:DISTRICT_HISTORY]-> (d),
                    (h) -[:WAREHOUSE_HISTORY]-> (w)
                    """, c_id=tuple[0], d_c_id=tuple[1], w_c_id=tuple[2],
                    d_id=tuple[3], w_id=tuple[4], h_date=tuple[5], 
                    h_amount=tuple[6], h_data=tuple[7])

            elif tableName == "STOCK":
                for tuple in tuples:
                    session.run("""
                    MATCH (i:ITEM {I_ID: $i_id})
                    MATCH (w:WAREHOUSE {W_ID: $w_id})
                    CREATE (s:STOCK {
                        S_QUANTITY: $s_quantity, S_DIST_01: $s_dist_01, S_DIST_02: $s_dist_02,
                        S_DIST_03: $s_dist_03, S_DIST_04: $s_dist_04, S_DIST_05: $s_dist_05,
                        S_DIST_06: $s_dist_06, S_DIST_07: $s_dist_07, S_DIST_08: $s_dist_08,
                        S_DIST_09: $s_dist_09, S_DIST_10: $s_dist_10, S_YTD: $s_ytd,
                        S_ORDER_CNT: $s_order_cnt, S_REMOTE_CNT: $s_remote_cnt, S_DATA: $s_data
                    }) -[:STOCKED_BY]-> (w),
                    (s) -[:STOCK_OF]-> (i)
                    """, i_id=tuple[0], w_id=tuple[1], s_quantity=tuple[2],
                    s_dist_01=tuple[3], s_dist_02=tuple[4], s_dist_03=tuple[5],
                    s_dist_04=tuple[6], s_dist_05=tuple[7], s_dist_06=tuple[8],
                    s_dist_07=tuple[9], s_dist_08=tuple[10], s_dist_09=tuple[11],
                    s_dist_10=tuple[12], s_ytd=tuple[13], s_order_cnt=tuple[14],
                    s_remote_cnt=tuple[15], s_data=tuple[16])

            elif tableName == "HISTORY":
                for tuple in tuples:
                    session.run("""
                    MATCH (c:CUSTOMER {C_ID: $c_id}) 
                           -[:BELONGS_TO]-> (d:DISTRICT {D_ID: $d_id}) 
                           -[:BELONGS_TO]-> (w:WAREHOUSE {W_ID: $w_id})
                    CREATE (h:HISTORY {
                        H_DATE: $h_date,
                        H_AMOUNT: $h_amount,
                        H_DATA: $h_data
                    })
                    CREATE (h) -[:CUSTOMER_HISTORY]-> (c),
                    (h) -[:DISTRICT_HISTORY]-> (d),
                    (h) -[:WAREHOUSE_HISTORY]-> (w)
                    """, 
                    c_id=tuple[0], d_id=tuple[3], w_id=tuple[4],
                    h_date=tuple[5].isoformat()[:-3], h_amount=tuple[6], h_data=tuple[7])

            logging.info("Committing %d queries for type %s" % (len(tuples), tableName))

    ## ----------------------------------------------
    ## loadFinish
    ## ----------------------------------------------
    def loadFinish(self):
        pass  # Neo4j doesn't require special handling for finishing bulk loading
    
    ## ----------------------------------------------
    ## loadFinishItem
    ## ----------------------------------------------
    def loadFinishItem(self):
        if self.items_complete_event:
            self.items_complete_event.set()
        return None
    
    ## ----------------------------------------------
    ## loadVerify
    ## ----------------------------------------------
    def loadVerify(self):
        logging.info("Neo4j:")
        logging.info(self.get_counts())

    ## ----------------------------------------------
    ## T1: doNewOrder
    ## ----------------------------------------------
    def doNewOrder(self, params):
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

        all_local = all(i_w_id == w_id for i_w_id in i_w_ids)
        total = 0
        items = []
        item_data = []

        with self.driver.session(database=self.database) as session:
            with session.begin_transaction() as tx:
                # Get item info
                for i in range(len(i_ids)):
                    result = tx.run("""
                    MATCH (i:ITEM {I_ID: $i_id})
                    RETURN i.I_PRICE as price, i.I_NAME as name, i.I_DATA as data
                    """, i_id=i_ids[i])
                    item = result.single()
                    if not item:
                        return (None, 0)
                    items.append({'name': item['name'], 'price': item['price'], 'data': item['data']})

                # Get warehouse, district, and customer info
                result = tx.run("""
                MATCH (c:CUSTOMER {C_ID: $c_id}) 
                       -[:BELONGS_TO]-> (d:DISTRICT {D_ID: $d_id}) 
                       -[:BELONGS_TO]-> (w:WAREHOUSE {W_ID: $w_id})  
                RETURN w.W_TAX as w_tax, d.D_TAX as d_tax, d.D_NEXT_O_ID as d_next_o_id,
                       c.C_DISCOUNT as c_discount, c.C_LAST as c_last, c.C_CREDIT as c_credit
                """, w_id=w_id, d_id=d_id, c_id=c_id)
                general_info = result.single()
                
                if not general_info:
                    logging.warning(f"No general info for warehouse {w_id}")
                    return (None, 0)

                w_tax = general_info['w_tax']
                d_tax = general_info['d_tax']
                d_next_o_id = general_info['d_next_o_id']
                c_discount = general_info['c_discount']
                c_last = general_info['c_last']
                c_credit = general_info['c_credit']

                ol_cnt = len(i_ids)
                o_carrier_id = constants.NULL_CARRIER_ID

                # Update district's next order id and create new order
                tx.run("""
                MATCH (c:CUSTOMER {C_ID: $c_id}) 
                       -[:BELONGS_TO]-> (d:DISTRICT {D_ID: $d_id}) 
                       -[:BELONGS_TO]-> (w:WAREHOUSE {W_ID: $w_id})
                SET d.D_NEXT_O_ID = d.D_NEXT_O_ID + 1
                CREATE (o:ORDER {
                    O_ID: $d_next_o_id,
                    O_D_ID: $d_id,
                    O_W_ID: $w_id,
                    O_C_ID: $c_id,
                    O_ENTRY_D: $o_entry_d,
                    O_CARRIER_ID: $o_carrier_id,
                    O_OL_CNT: $ol_cnt,
                    O_ALL_LOCAL: $all_local
                }) -[:PLACED_BY]-> (c)
                CREATE (no:NEW_ORDER {NO_O_ID: $d_next_o_id}) 
                       -[:PLACED_BY]-> (:CUSTOMER {C_ID: $c_id})
                """, w_id=w_id, d_id=d_id, c_id=c_id, d_next_o_id=d_next_o_id, o_entry_d=o_entry_d,
                     o_carrier_id=o_carrier_id, ol_cnt=ol_cnt, all_local=int(all_local))

                for i in range(len(i_ids)):
                    ol_number = i + 1
                    ol_supply_w_id = i_w_ids[i]
                    ol_i_id = i_ids[i]
                    ol_quantity = i_qtys[i]

                    i_name = items[i]['name']
                    i_data = items[i]['data']
                    i_price = items[i]['price']

                    # First query: Retrieve the current stock information
                    result = tx.run("""
                    MATCH (s:STOCK) -[:STOCK_OF]-> (:ITEM {I_ID: $i_id}) 
                    MATCH (s:STOCK) -[:STOCKED_BY]-> (:WAREHOUSE {W_ID: $w_id})
                    RETURN s.S_QUANTITY as s_quantity, s.S_DATA as s_data, s.S_DIST_01 as s_dist,
                           s.S_YTD as s_ytd, s.S_ORDER_CNT as s_order_cnt, s.S_REMOTE_CNT as s_remote_cnt
                    """, i_id=ol_i_id, w_id=ol_supply_w_id)

                    stock_info = result.single()
                    assert stock_info is not None, f"No stock found for item {ol_i_id} in warehouse {ol_supply_w_id}"

                    s_quantity = stock_info['s_quantity']
                    s_data = stock_info['s_data']
                    s_dist = stock_info['s_dist']
                    s_ytd = stock_info['s_ytd']
                    s_order_cnt = stock_info['s_order_cnt']
                    s_remote_cnt = stock_info['s_remote_cnt']

                    # Calculate auxilliary values
                    new_s_ytd = s_ytd + ol_quantity
                    new_s_order_cnt = s_order_cnt + 1
                    new_s_remote_cnt = s_remote_cnt + (1 if ol_supply_w_id != w_id else 0)
                    new_s_quantity = s_quantity - ol_quantity if s_quantity >= ol_quantity + 10 else s_quantity + 91 - ol_quantity
                    brand_generic = 'B' if constants.ORIGINAL_STRING in i_data and constants.ORIGINAL_STRING in s_data else 'G'
                    ol_amount = ol_quantity * i_price

                    # Second query: Update the stock information
                    tx.run("""
                    MATCH (s:STOCK) -[:STOCK_OF]-> (:ITEM {I_ID: $i_id}) 
                    MATCH (s:STOCK) -[:STOCKED_BY]-> (:WAREHOUSE {W_ID: $w_id})
                    SET s.S_YTD = $new_s_ytd,
                        s.S_ORDER_CNT = $new_s_order_cnt,
                        s.S_REMOTE_CNT = $new_s_remote_cnt,
                        s.S_QUANTITY = $new_s_quantity
                    """, i_id=ol_i_id, w_id=ol_supply_w_id, 
                         new_s_ytd=new_s_ytd, new_s_order_cnt=new_s_order_cnt, 
                         new_s_remote_cnt=new_s_remote_cnt, new_s_quantity=new_s_quantity)

                    # Create order line
                    tx.run("""
                    MATCH (o:ORDER {O_ID: $o_id}) 
                           -[:PLACED_BY]-> (:CUSTOMER) 
                           -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id}) 
                           -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                    MATCH (i:ITEM {I_ID: $i_id})
                    CREATE (ol:ORDER_LINE {
                        OL_O_ID: $o_id,
                        OL_D_ID: $d_id,
                        OL_W_ID: $w_id,
                        OL_NUMBER: $ol_number,
                        OL_I_ID: $i_id,
                        OL_SUPPLY_W_ID: $ol_supply_w_id,
                        OL_QUANTITY: $ol_quantity,
                        OL_AMOUNT: $ol_amount,
                        OL_DIST_INFO: $ol_dist_info
                    }) -[:PART_OF]-> (o)
                    CREATE (ol) -[:CONTAINS]-> (i)
                    """, o_id=d_next_o_id, d_id=d_id, w_id=w_id, ol_number=ol_number, i_id=ol_i_id,
                         ol_supply_w_id=ol_supply_w_id, ol_quantity=ol_quantity, ol_amount=ol_amount,
                         ol_dist_info=s_dist)

                    total += ol_amount
                    item_data.append((i_name, s_quantity, brand_generic, i_price, ol_amount))

                tx.commit()
                total *= (1 - c_discount) * (1 + w_tax + d_tax)

                misc = [(w_tax, d_tax, d_next_o_id, total)]
                return ([[c_discount, c_last, c_credit], misc, item_data], 0)
            ## WITH
        ## WITH
    
    ## ----------------------------------------------
    ## T2: doDelivery
    ## ----------------------------------------------
    def doDelivery(self, params):
        w_id = params["w_id"]
        o_carrier_id = params["o_carrier_id"]
        ol_delivery_d = params["ol_delivery_d"].isoformat()[:-3]

        with self.driver.session(database=self.database) as session:
            with session.begin_transaction() as tx:
                result = []
                for d_id in range(1, constants.DISTRICTS_PER_WAREHOUSE + 1):
                    # Get the oldest NEW_ORDER for this district
                    new_order_query = """
                    MATCH (no:ORDER {O_NEW_ORDER: true}) 
                      -[:PLACED_BY]-> (c:CUSTOMER) 
                      -[:BELONGS_TO]-> (d:DISTRICT {D_ID: $d_id}) 
                      -[:BELONGS_TO]-> (w:WAREHOUSE {W_ID: $w_id})
                    WITH no
                    ORDER BY no.O_ID
                    LIMIT 1
                    RETURN no.O_ID as o_id
                    """
                    new_order = tx.run(new_order_query, w_id=w_id, d_id=d_id).single()
                    
                    if not new_order:
                        # No orders for this district: skip it. Note: This must be reported if > 1%
                        continue

                    no_o_id = new_order['o_id']

                    # Get the total amount from ORDER_LINE
                    ol_total_query = """
                    MATCH (o:ORDER {O_ID: $o_id}) 
                      -[:PLACED_BY]-> (c:CUSTOMER)
                      -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id})
                      -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                    MATCH (o) <-[:PART_OF]- (ol:ORDER_LINE)
                    RETURN sum(ol.OL_AMOUNT) as ol_total
                    """
                    ol_total_result = tx.run(ol_total_query, o_id=no_o_id, w_id=w_id, d_id=d_id).single()
                    ol_total = ol_total_result['ol_total']

                    # Update ORDER, delete NEW_ORDER, update CUSTOMER
                    update_query = """
                    MATCH (o:ORDER {O_ID: $o_id}) 
                      -[:PLACED_BY]-> (c:CUSTOMER)
                      -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id})
                      -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                    SET o.O_CARRIER_ID = $o_carrier_id,
                        o.O_NEW_ORDER = false,
                        c.C_BALANCE = c.C_BALANCE + $ol_total,
                        c.C_DELIVERY_CNT = coalesce(c.C_DELIVERY_CNT, 0) + 1
                    """
                    tx.run(update_query, o_id=no_o_id, w_id=w_id, d_id=d_id, o_carrier_id=o_carrier_id, ol_total=ol_total)

                    # Update ORDER_LINE delivery date
                    update_ol_query = """
                    MATCH (o:ORDER {O_ID: $o_id})<-[:PART_OF]-(ol:ORDER_LINE)
                    SET ol.OL_DELIVERY_D = $ol_delivery_d
                    """
                    tx.run(update_ol_query, o_id=no_o_id, ol_delivery_d=ol_delivery_d)

                    # If there are no order lines, SUM returns null. There should always be order lines.
                    assert ol_total is not None, "ol_total is NULL: there are no order lines. This should not happen"
                    assert ol_total > 0.0

                    # These must be logged in the "result file" according to TPC-C 2.7.2.2 (page 39)
                    # We remove the queued time, completed time, w_id, and o_carrier_id: the client can figure them out
                    result.append((d_id, no_o_id))

                tx.commit()
                return (result, 0)
            ## WITH
        ## WITH

    ## ----------------------------------------------
    ## T3:doOrderStatus
    ## ----------------------------------------------    
    def doOrderStatus(self, params):
        w_id = params["w_id"]
        d_id = params["d_id"]
        c_id = params["c_id"]
        c_last = params["c_last"]
        
        assert w_id, pformat(params)
        assert d_id, pformat(params)

        with self.driver.session(database=self.database) as session:
            with session.begin_transaction() as tx:
                result = []
                if c_id is not None:
                    customer_query = """
                    MATCH (c:CUSTOMER {C_ID: $c_id}) 
                      -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id})
                      -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                    RETURN c.C_ID as c_id, c.C_FIRST as c_first, c.C_MIDDLE as c_middle, 
                           c.C_LAST as c_last, c.C_BALANCE as c_balance
                    """
                    customer = tx.run(customer_query, c_id=c_id, w_id=w_id, d_id=d_id).single()
                    assert customer, f"doOrderStatus: no customer found for w_id {w_id}, d_id {d_id}, c_id {c_id}"
                else:
                    customers_query = """
                    MATCH (c:CUSTOMER {C_LAST: $c_last}) 
                      -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id})
                      -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                    RETURN c.C_ID as c_id, c.C_FIRST as c_first, c.C_MIDDLE as c_middle, 
                           c.C_LAST as c_last, c.C_BALANCE as c_balance
                    ORDER BY c.C_FIRST
                    """
                    customers = list(tx.run(customers_query, c_last=c_last, w_id=w_id, d_id=d_id))
                    assert len(customers) > 0, f"doOrderStatus: no customers found for w_id {w_id}, d_id {d_id}, c_last {c_last}"
                    
                    # Get the midpoint customer
                    index = (len(customers) - 1) // 2
                    customer = customers[index]
                    c_id = customer['c_id']

                customer_data = [
                    c_id,
                    customer['c_first'],
                    customer['c_middle'],
                    customer['c_last'],
                    customer['c_balance']
                ]

                # Get the latest order for this customer
                order_query = """
                MATCH (c:CUSTOMER {C_ID: $c_id}) 
                  -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id})
                  -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id}) 
                MATCH (c) <-[:PLACED_BY]- (o:ORDER)
                RETURN o.O_ID as o_id
                ORDER BY o.O_ID DESC
                LIMIT 1
                """
                order = tx.run(order_query, c_id=c_id, w_id=w_id, d_id=d_id).single()

                orderLines_data = []
                if order:
                    o_id = order['o_id']
                    
                    # Get order lines
                    orderlines_query = """
                    MATCH (c:CUSTOMER {C_ID: $c_id}) 
                      -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id})
                      -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id}) 
                    MATCH (c) <-[:PLACED_BY]- (o:ORDER {O_ID: $o_id}) <-[:PART_OF]- (ol:ORDER_LINE) -[:CONTAINS]-> (i:ITEM)
                    RETURN i.I_ID as i_id, ol.OL_SUPPLY_W_ID as ol_supply_w_id, 
                           ol.OL_QUANTITY as ol_quantity, ol.OL_AMOUNT as ol_amount, 
                           ol.OL_DIST_INFO as ol_dist_info
                    """
                    orderLines = tx.run(orderlines_query, o_id=o_id, w_id=w_id, d_id=d_id, c_id=c_id)
                    for orderLine in orderLines:
                        orderLines_data.append([
                            orderLine['i_id'],
                            orderLine['ol_supply_w_id'],
                            orderLine['ol_quantity'],
                            orderLine['ol_amount'],
                            orderLine['ol_dist_info']
                        ])
                else:
                    o_id = None

                tx.commit()
                return ([customer_data, [o_id] if o_id else [], orderLines_data], 0)
            ## WITH
        ## WITH

    ## ----------------------------------------------
    ## T4:doPayment
    ## ----------------------------------------------    
    def doPayment(self, params):
        w_id = params["w_id"]
        d_id = params["d_id"]
        h_amount = params["h_amount"]
        c_w_id = params["c_w_id"]
        c_d_id = params["c_d_id"]
        c_id = params["c_id"]
        c_last = params["c_last"]
        h_date = params["h_date"].isoformat()[:-3]

        with self.driver.session(database=self.database) as session:
            with session.begin_transaction() as tx:
                if c_id is not None:
                    customer_query = """
                    MATCH (c:CUSTOMER {C_ID: $c_id}) 
                      -[:BELONGS_TO]-> (:DISTRICT {D_ID: $c_d_id}) 
                      -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $c_w_id})
                    RETURN c
                    """
                    customer = tx.run(customer_query, c_id=c_id, c_w_id=c_w_id, c_d_id=c_d_id).single()
                    assert customer, f"doPayment: no customer found for w_id {c_w_id}, d_id {c_d_id}, c_id {c_id}"
                else:
                    customers_query = """
                    MATCH (c:CUSTOMER {C_LAST: $c_last}) 
                      -[:BELONGS_TO]-> (:DISTRICT {D_ID: $c_d_id}) 
                      -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $c_w_id})
                    RETURN c
                    ORDER BY c.C_FIRST
                    """
                    customers = list(tx.run(customers_query, c_last=c_last, c_w_id=c_w_id, c_d_id=c_d_id))
                    assert len(customers) > 0, f"doPayment: no customer found for w_id {c_w_id}, d_id {c_d_id}, c_last {c_last}"
                    
                    # Get the midpoint customer
                    index = (len(customers) - 1) // 2
                    customer = customers[index]
                
                c = customer['c']
                customer_data = [
                    c['C_ID'], c['C_FIRST'], c['C_MIDDLE'], c['C_LAST'],
                    c['C_STREET_1'], c['C_STREET_2'], c['C_CITY'],
                    c['C_STATE'], c['C_ZIP'], c['C_PHONE'],
                    c['C_SINCE'], c['C_CREDIT'], c['C_CREDIT_LIM'],
                    c['C_DISCOUNT'], c['C_BALANCE'], c['C_YTD_PAYMENT'],
                    c['C_PAYMENT_CNT'], c['C_DATA']
                ]

                # Get warehouse data
                warehouse_query = """
                MATCH (w:WAREHOUSE {W_ID: $w_id})
                RETURN w.W_NAME, w.W_STREET_1, w.W_STREET_2, w.W_CITY, w.W_STATE, w.W_ZIP
                """
                warehouse_data = tx.run(warehouse_query, w_id=w_id).single()

                # Get district data
                district_query = """
                MATCH (d:DISTRICT {D_ID: $d_id}) 
                  -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                RETURN d.D_NAME, d.D_STREET_1, d.D_STREET_2, d.D_CITY, d.D_STATE, d.D_ZIP
                """
                district_data = tx.run(district_query, d_id=d_id, w_id=w_id).single()

                # Update warehouse and district YTD
                tx.run("""
                MATCH (w:WAREHOUSE {W_ID: $w_id})
                SET w.W_YTD = w.W_YTD + $h_amount
                """, w_id=w_id, h_amount=h_amount)

                tx.run("""
                MATCH (d:DISTRICT {D_ID: $d_id}) 
                  -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                SET d.D_YTD = d.D_YTD + $h_amount
                """, d_id=d_id, w_id=w_id, h_amount=h_amount)

                h_data = f"{warehouse_data[0]}    {district_data[0]}"

                # Update customer
                c_balance = c['C_BALANCE'] - h_amount
                c_ytd_payment = c['C_YTD_PAYMENT'] + h_amount
                c_payment_cnt = c['C_PAYMENT_CNT'] + 1

                if c['C_CREDIT'] == constants.BAD_CREDIT:
                    new_data = f"{c['C_ID']} {c_d_id} {c_w_id} {d_id} {w_id} {h_amount}"
                    c_data = (new_data + "|" + c['C_DATA'])[:constants.MAX_C_DATA]
                    update_query = """
                    MATCH (c:CUSTOMER {C_ID: $c_id}) 
                      -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $c_w_id}) 
                      -[:BELONGS_TO]-> (:DISTRICT {D_ID: $c_d_id})
                    SET c.C_BALANCE = $c_balance,
                        c.C_YTD_PAYMENT = $c_ytd_payment,
                        c.C_PAYMENT_CNT = $c_payment_cnt,
                        c.C_DATA = $c_data
                    """
                    tx.run(update_query, c_id=c['C_ID'], c_w_id=c_w_id, c_d_id=c_d_id,
                           c_balance=c_balance, c_ytd_payment=c_ytd_payment,
                           c_payment_cnt=c_payment_cnt, c_data=c_data)
                else:
                    update_query = """
                    MATCH (c:CUSTOMER {C_ID: $c_id}) 
                      -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $c_w_id}) 
                      -[:BELONGS_TO]-> (:DISTRICT {D_ID: $c_d_id})
                    SET c.C_BALANCE = $c_balance,
                        c.C_YTD_PAYMENT = $c_ytd_payment,
                        c.C_PAYMENT_CNT = $c_payment_cnt
                    """
                    tx.run(update_query, c_id=c['C_ID'], c_w_id=c_w_id, c_d_id=c_d_id,
                           c_balance=c_balance, c_ytd_payment=c_ytd_payment,
                           c_payment_cnt=c_payment_cnt)

                # Create history record
                history_query = """
                MATCH (c:CUSTOMER {C_ID: $c_id}) 
                  -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $c_w_id}) 
                  -[:BELONGS_TO]-> (:DISTRICT {D_ID: $c_d_id})
                match (d:DISTRICT {D_ID: $d_id}) -[:BELONGS_TO]-> (w:WAREHOUSE {W_ID: $w_id})
                CREATE (h:HISTORY {H_C_ID: $c_id, H_C_D_ID: $c_d_id, H_C_W_ID: $c_w_id,
                                   H_D_ID: $d_id, H_W_ID: $w_id, H_DATE: $h_date,
                                   H_AMOUNT: $h_amount, H_DATA: $h_data})
                CREATE (h) -[:CUSTOMER_HISTORY]-> (c)
                CREATE (h) -[:DISTRICT_HISTORY]-> (d)
                CREATE (h) -[:WAREHOUSE_HISTORY]-> (w)
                """
                tx.run(history_query, c_id=c['C_ID'], c_w_id=c_w_id, c_d_id=c_d_id,
                       d_id=d_id, w_id=w_id, h_date=h_date, h_amount=h_amount, h_data=h_data)

                tx.commit()

                # TPC-C 2.5.3.3: Must display the following fields
                return ([warehouse_data, district_data, customer_data], 0)        
            ## WITH
        ## WITH
    
    ## ----------------------------------------------
    ## T5: doStockLevel
    ## ----------------------------------------------    
    def doStockLevel(self, params):
        w_id = params["w_id"]
        d_id = params["d_id"]
        threshold = params["threshold"]
        
        with self.driver.session(database=self.database) as session:
            with session.begin_transaction() as tx:
                # Get the next order ID for the district
                district_query = """
                MATCH (d:DISTRICT {D_ID: $d_id}) 
                  -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                RETURN d.D_NEXT_O_ID as d_next_o_id
                """
                result = tx.run(district_query, d_id=d_id, w_id=w_id).single()
                assert result, f"doStockLevel: no district found for w_id {w_id}, d_id {d_id}"
                o_id = result['d_next_o_id']

                # Count distinct items with stock quantity below threshold
                stock_count_query = """
                MATCH (w:WAREHOUSE {W_ID: $w_id})
                  -[:STOCKED_BY]-> (s:STOCK) 
                  -[:STOCK_OF]-> (i:ITEM)
                WHERE s.S_QUANTITY < $threshold
                WITH i
                MATCH (o:ORDER) -[:PLACED_BY]-> (c:CUSTOMER)
                  -[:BELONGS_TO]-> (:DISTRICT {D_ID: $d_id})
                  -[:BELONGS_TO]-> (:WAREHOUSE {W_ID: $w_id})
                MATCH (o)<-[:PART_OF]-(ol:ORDER_LINE) -[:CONTAINS]-> (i)
                WHERE o.O_ID < $o_id AND o.O_ID >= $o_id_low
                RETURN COUNT(DISTINCT i) as stock_count
                """
                result = tx.run(stock_count_query, 
                                w_id=w_id, 
                                d_id=d_id, 
                                threshold=threshold, 
                                o_id=o_id, 
                                o_id_low=o_id-20).single()
                
                stock_count = result['stock_count']
                
                tx.commit()
                
                return (int(stock_count), 0)
            ## WITH
        ## WITH
        
    ## ----------------------------------------------
    ## Post-execution verification
    ## ----------------------------------------------
    def executeVerify(self):
        logging.info("Neo4j:")
        logging.info(self.get_counts())

    ## ----------------------------------------------
    ## Get counts
    ## ----------------------------------------------
    def get_counts(self):
        tables = ["ITEM", "WAREHOUSE", "DISTRICT", "CUSTOMER", "STOCK", "ORDERS", "NEW_ORDER", "ORDER_LINE", "HISTORY"]
        with self.driver.session(database=self.database) as session:
            verification = "\n{\n"
            for table in tables:
                if table == "ORDERS":
                    # Count orders that are not new orders
                    q = "MATCH (o:ORDER) WHERE NOT o.O_NEW_ORDER RETURN count(o) as count"
                elif table == "NEW_ORDER":
                    # Count orders that are new orders
                    q = "MATCH (o:ORDER) WHERE o.O_NEW_ORDER RETURN count(o) as count"
                else:
                    # Count nodes with the given label
                    q = f"MATCH (n:{table}) RETURN count(n) as count"
                
                result = session.run(q).single()
                count = result["count"]
                verification += f"    \"{table}\": {count}\n"
            verification += "}"
            return verification

## CLASS
