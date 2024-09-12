import psycopg2
import logging
import getpass

# Set up logging
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')

# Database connection parameters
db_params = {
    "host": "localhost",
    "port": 5432,
    "database": "tpcc",  # Replace with your database name
    "user": getpass.getuser(),  # Replace with your username
    "password": ""  # Replace with your password if any
}

print(getpass.getuser())

# SQL query
query = "SELECT I_ID, I_PRICE, I_NAME, I_DATA FROM ITEM"

conn = None
cur = None

try:
    # Connect to the database
    logging.info("Connecting to the PostgreSQL database...")
    conn = psycopg2.connect(**db_params)

    # Create a cursor
    cur = conn.cursor()

    # Execute the query
    logging.info("Executing query: %s", query)
    cur.execute(query)

    # Fetch the results
    results = cur.fetchall()

    # Print the results
    if results:
        logging.info("Query results:")
        for row in results:
            logging.info(row)
    else:
        logging.warning("No results found for the query.")

except (Exception, psycopg2.Error) as error:
    logging.error("Error while connecting to PostgreSQL or executing query:", exc_info=True)

finally:
    # Close the cursor and connection if they exist
    if cur:
        cur.close()
        logging.info("Cursor closed.")
    if conn:
        conn.close()
        logging.info("Database connection closed.")