#####
# Based on this test decided to use get instead of fetch
#####

import time
import matplotlib.pyplot as plt
from statistics import mean
from typedb.driver import TypeDB, TransactionType

DB_NAME = "tpcc"
SERVER_ADDR = "127.0.0.1:1729"
w_id = 2
d_id = 5
c_id = 18
DPW = 10
CPD = 3000

def query_get(driver, db_name):
    with driver.transaction(db_name, TransactionType.READ) as tx:
        q = f"""
match
$c isa CUSTOMER, has C_ID {w_id * DPW * CPD + d_id * CPD + c_id},
has C_FIRST $c_first, has C_MIDDLE $c_middle, has C_LAST $c_last,
has C_STREET_1 $c_street_1, has C_STREET_2 $c_street_2, has C_CITY $c_city,
has C_STATE $c_state, has C_ZIP $c_zip, has C_PHONE $c_phone,
has C_SINCE $c_since, has C_CREDIT $c_credit, has C_CREDIT_LIM $c_credit_lim,
has C_DISCOUNT $c_discount, has C_BALANCE $c_balance, has C_DATA $c_data;
select $c_first, $c_middle, $c_last, $c_street_1, $c_street_2, $c_city,
$c_state, $c_zip, $c_phone, $c_since, $c_credit, $c_credit_lim, $c_discount,
$c_balance, $c_data;
"""
        customers = list(tx.query(q).resolve().as_concept_rows())
        customer_data = [
            customers[0].get('c_first').as_attribute().get_value(),
            customers[0].get('c_middle').as_attribute().get_value(),
            customers[0].get('c_last').as_attribute().get_value(),
            customers[0].get('c_street_1').as_attribute().get_value(),
            customers[0].get('c_street_2').as_attribute().get_value(),
            customers[0].get('c_city').as_attribute().get_value(),
            customers[0].get('c_state').as_attribute().get_value(),
            customers[0].get('c_zip').as_attribute().get_value(),
            customers[0].get('c_phone').as_attribute().get_value(),
            customers[0].get('c_since').as_attribute().get_value(),
            customers[0].get('c_credit').as_attribute().get_value(),
            customers[0].get('c_credit_lim').as_attribute().get_value(),
            customers[0].get('c_discount').as_attribute().get_value(),
            customers[0].get('c_balance').as_attribute().get_value(),
            customers[0].get('c_data').as_attribute().get_value(),
        ]
        return customer_data

def query_fetch(driver, db_name):
    with driver.transaction(db_name, TransactionType.READ) as tx:
        q = f"""
match
$c isa CUSTOMER, has C_ID {w_id * DPW * CPD + d_id * CPD + c_id};
fetch {{
  "c attrs": {{ $c.* }}
}}
"""
        customer = list(tx.query(q).as_concept_documents())
        customer_data = [
            customer[0]['c']['C_FIRST'],
            customer[0]['c']['C_MIDDLE'],
            customer[0]['c']['C_LAST'],
            customer[0]['c']['C_STREET_1'],
            customer[0]['c']['C_STREET_2'],
            customer[0]['c']['C_CITY'],
            customer[0]['c']['C_STATE'],
            customer[0]['c']['C_ZIP'],
            customer[0]['c']['C_PHONE'],
            customer[0]['c']['C_SINCE'],
            customer[0]['c']['C_CREDIT'],
            customer[0]['c']['C_CREDIT_LIM'],
            customer[0]['c']['C_DISCOUNT'],
            customer[0]['c']['C_BALANCE'],
            customer[0]['c']['C_DATA'],
        ]
        return customer_data

def run_performance_test(driver, db_name, num_iterations=10000):
    get_times = []
    fetch_times = []

    for _ in range(num_iterations):
        # Time query_get
        start = time.time()
        _ = query_get(driver, db_name)
        end = time.time()
        get_times.append(end - start)

        # Time query_fetch
        start = time.time()
        _ = query_fetch(driver, db_name)
        end = time.time()
        fetch_times.append(end - start)

    return get_times, fetch_times

def plot_histograms(get_times, fetch_times):
    max_time = 0.040  # Set maximum time to 0.040 seconds

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))

    ax1.hist(get_times, bins=30, edgecolor='black', range=(0, max_time))
    ax1.set_title('Query Get Times')
    ax1.set_xlabel('Time (seconds)')
    ax1.set_ylabel('Frequency')
    ax1.set_xlim(0, max_time)

    ax2.hist(fetch_times, bins=30, edgecolor='black', range=(0, max_time))
    ax2.set_title('Query Fetch Times')
    ax2.set_xlabel('Time (seconds)')
    ax2.set_ylabel('Frequency')
    ax2.set_xlim(0, max_time)

    plt.tight_layout()
    plt.savefig('query_performance_histograms.png')
    plt.close()

if __name__ == "__main__":
    with TypeDB.core_driver(SERVER_ADDR) as driver:
        get_times, fetch_times = run_performance_test(driver, DB_NAME)
        
        print("Performance Test Results:")
        print(f"query_get:")
        print(f"  Average: {mean(get_times):.6f} seconds")
        print(f"query_fetch:")
        print(f"  Average: {mean(fetch_times):.6f} seconds")

        plot_histograms(get_times, fetch_times)
        print("Histograms saved as 'query_performance_histograms.png'")
