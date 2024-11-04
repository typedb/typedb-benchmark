from typedb.driver import *
from tqdm import tqdm
import csv
import os
import time
import asyncio
import glob
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor
import threading

ROOT = os.getcwd()
SF = 0.003

def load_place(driver):

    def insert_data(session, row):
        # Insert entities and attributes
        query = f'insert $place isa {row[3]}, has id {row[0]}, has name "{row[1]}", has url "{row[2]}";'
        return query

    def insert_relations(session, row):
        # Insert relations
        query = f"""
        match
            $place1 isa Place, has id {row[0]};
            $place2 isa Place, has id {row[4]};
        insert
            (part: $place1, partOf: $place2) isa isPartOf;
        """
        return query

    directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "Place")

    with driver.session("ldbcsnb", SessionType.DATA) as session:
        # First Pass: Insert entities and attributes
        for filename in os.listdir(directory):
            if filename.endswith('.csv'):
                filepath = os.path.join(directory, filename)
                with open(filepath, encoding='utf-8') as csv_file:     
                    csv_reader = csv.reader(csv_file, delimiter='|')
                    next(csv_reader)  # Skip the header

                    with session.transaction(TransactionType.WRITE) as tx:
                        for row in csv_reader:
                            tx.query.insert(insert_data(session, row))
                        tx.commit()
                
        # Second Pass: Insert relations
        for filename in os.listdir(directory):
            if filename.endswith('.csv'):
                filepath = os.path.join(directory, filename)
                with open(filepath, encoding='utf-8') as csv_file:  
                    csv_reader = csv.reader(csv_file, delimiter='|')
                    next(csv_reader)  # Skip the header

                    with session.transaction(TransactionType.WRITE) as tx:
                        for row in csv_reader:
                            if row[4]:
                                tx.query.insert(insert_relations(session, row))
                        tx.commit()

                
def load_organization(driver):
    directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "Organisation")

    with driver.session("ldbcsnb", SessionType.DATA) as session:
        for filename in os.listdir(directory):
            if filename.endswith('.csv'):
                filepath = os.path.join(directory, filename)
                with open(filepath, encoding='utf-8') as csv_file:  
                    csv_reader = csv.reader(csv_file, delimiter='|')
                    next(csv_reader)  # Skip the header
                    with session.transaction(TransactionType.WRITE) as tx:
                        for row in csv_reader:
                            # Determine the specific subtype of Organisation based on the data
                            organisation_type = row[1]  # It should be either 'University' or 'Company'
                            
                            # Construct the insert query based on the schema
                            query = f'''
                            match
                                $place isa Place, has id {row[4]};
                            insert
                                $org isa {organisation_type}, 
                                    has id {row[0]}, 
                                    has name "{row[2]}", 
                                    has url "{row[3]}";
                                (location: $org, locatedIn: $place) isa isLocatedIn;
                            '''
                            tx.query.insert(query)
                        tx.commit()

def load_tagclass(driver):
    def insert_entities_and_attributes(session, row, tx):
        # Construct the insert query for entities and attributes
        tagclass_query = f'''
        insert $tagclass isa TagClass, 
            has id {row[0]}, 
            has name "{row[1]}", 
            has url "{row[2]}";
        '''
        
        tx.query.insert(tagclass_query)

    def insert_relations(session, row, tx):
        # Check if there's a superclass (SubclassOfTagClassId is not empty)
        if row[3]:
            # Insert the isSubclassOf relation for the tagclass
            subclass_relation_query = f'''
            match
                $tagclass isa TagClass, has id {row[0]};
                $superclass isa TagClass, has id {row[3]};
            insert
                (subclass: $tagclass, superclass: $superclass) isa isSubclassOf;
            '''
            
            tx.query.insert(subclass_relation_query)

    # Load the tagclasses, their attributes, and their relations
    directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "TagClass")
    
    with driver.session("ldbcsnb", SessionType.DATA) as session:
        with session.transaction(TransactionType.WRITE) as tx:
            for filename in os.listdir(directory):
                if  filename.endswith('.csv'):
                    filepath = os.path.join(directory, filename)
                    with open(filepath, encoding='utf-8') as csv_file:
                        csv_reader = csv.reader(csv_file, delimiter='|')
                        next(csv_reader)  # Skip the header
                        
                        # First Pass: Insert entities and attributes
                        for row in csv_reader:
                            insert_entities_and_attributes(session, row, tx)

                        # Reset the file reader to start from the beginning again
                        csv_file.seek(0)
                        next(csv_reader)  # Skip the header

                        # Second Pass: Insert relations
                        for row in csv_reader:
                            insert_relations(session, row, tx)
                    tx.commit()
            
def load_tags(driver, batch_size=1000):

    directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "Tag")

    with driver.session("ldbcsnb", SessionType.DATA) as session:
        for filename in os.listdir(directory):
            if filename.endswith('.csv'):
                filepath = os.path.join(directory, filename)
                with open(filepath, encoding='utf-8') as csv_file:
                    csv_reader = csv.reader(csv_file, delimiter='|')
                    next(csv_reader)  # Skip the header

                    processed_rows = 0
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction
                    
                    for row in csv_reader:
                        # Construct the insert query for the tag, its attributes, and its hasType relation
                        query = f'''
                        match
                            $tagclass isa TagClass, has id {row[3]};
                        insert
                            $tag isa Tag, 
                                has id {row[0]}, 
                                has name "{row[1]}",
                                has url "{row[2]}";
                            (tag: $tag, tagClass: $tagclass) isa hasType;
                        '''
                        tx.query.insert(query)
                        
                        processed_rows += 1
                        
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            
                            tx = session.transaction(TransactionType.WRITE)
                    
                    if processed_rows % batch_size != 0:
                        tx.commit()
            

def count_rows_in_directory(directory):
    total_rows = 0
    for filepath in glob.glob(os.path.join(directory, '*.csv')):
        with open(filepath, 'rb') as csv_file:
            total_rows += csv_file.read().count(b'\n') - 1  # subtract 1 for the header
    return total_rows
  
              
def load_person(driver, batch_size=5000):
    directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Person")

    with driver.session("ldbcsnb", SessionType.DATA) as session:
        for filename in os.listdir(directory):
            if filename.endswith('.csv'):
                filepath = os.path.join(directory, filename)
                with open(filepath, encoding='utf-8') as csv_file:
                    csv_reader = csv.reader(csv_file, delimiter='|')
                    next(csv_reader)  # Skip the header

                    queries = []
                    for row in csv_reader:
                        # Parse and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        email_query = " ".join([f'has email "{email}",' for email in row[10].split(';')])
                        language_query = " ".join([f'has speaks "{lang}",' for lang in row[9].split(';')])

                        # Construct the insert query
                        query = f'''
                        match
                            $city isa City, has id {row[8]};
                        insert
                            $person isa Person,
                                has id {row[1]},
                                has firstName "{row[2]}",
                                has lastName "{row[3]}",
                                has gender "{row[4]}",
                                has birthday {row[5]},
                                has locationIP "{row[6]}",
                                has browserUsed "{row[7]}",
                                {email_query}
                                {language_query}
                                has creationDate {formatted_date};
                            (location: $person, locatedIn: $city) isa isLocatedIn;
                        '''
                        queries.append(query)

                        # If batch size is reached, execute the queries and clear the list
                        if len(queries) == batch_size:
                            with session.transaction(TransactionType.WRITE) as tx:
                                for q in queries:
                                    tx.query.insert(q)
                                tx.commit()
                            queries.clear()

                    # If there are remaining queries, execute them
                    if queries:
                        with session.transaction(TransactionType.WRITE) as tx:
                            for q in queries:
                                tx.query.insert(q)
                            tx.commit()

def process_file(filename, driver, directory, batch_size, pbar, pbar_lock):
    filepath = os.path.join(directory, filename)
    with open(filepath, encoding='utf-8') as csv_file:
        csv_reader = csv.reader(csv_file, delimiter='|')
        next(csv_reader)  # Skip the header

        processed_rows = 0

        with driver.session("ldbcsnb", SessionType.DATA) as session:
            tx = session.transaction(TransactionType.WRITE)  # Start the first transaction

            for row in csv_reader:
                # Parse and format the creation date
                creation_date = datetime.fromisoformat(row[0])
                formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                # Construct the insert query for the forum, its attributes, and its hasModerator relation
                query = f'''
                match   
                    $moderator isa Person, has id {row[3]};
                insert
                    $forum isa Forum,
                        has id {row[1]},
                        has title "{row[2]}",
                        has creationDate {formatted_date};
                    (moderator: $moderator, moderated: $forum) isa hasModerator;
                '''
                tx.query.insert(query)

                processed_rows += 1

                # Commit and start a new transaction if batch size is reached
                if processed_rows % batch_size == 0:
                    tx.commit()
                    tx = session.transaction(TransactionType.WRITE)
                    
                    with pbar_lock:  # Lock the progress bar for thread safety
                        pbar.update(batch_size)

            # Commit the last batch if there are any remaining rows
            if processed_rows % batch_size != 0:
                tx.commit()

                with pbar_lock:  # Lock the progress bar for thread safety
                    pbar.update(processed_rows % batch_size)

    return processed_rows

def load_forum(driver, batch_size=10000):
    directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Forum")
    
    # Estimate total rows (this is a rough estimate, assuming average file sizes)
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    pbar_lock = threading.Lock()  # Create a lock for the progress bar

    filenames = [f for f in os.listdir(directory) if f.endswith('.csv')]
    
    with ThreadPoolExecutor() as executor:
        # Use partial to pass additional arguments (pbar and pbar_lock) to process_file
        from functools import partial
        fn = partial(process_file, driver=driver, directory=directory, batch_size=batch_size, pbar=pbar, pbar_lock=pbar_lock)
        list(executor.map(fn, filenames))

    pbar.close()


def load_posts(driver, batch_size=10000):
    with driver.session("ldbcsnb", SessionType.DATA) as session:
        directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Post")
        
        # Estimate total rows (this is a rough estimate, assuming average file sizes)
        total_rows = count_rows_in_directory(directory)
        pbar = tqdm(total=total_rows, desc="Processed rows")
        
        for filename in os.listdir(directory):
            if filename.endswith('.csv'):
                filepath = os.path.join(directory, filename)
                with open(filepath, encoding='utf-8') as csv_file:
                    csv_reader = csv.reader(csv_file, delimiter='|')
                    next(csv_reader)  # Skip the header

                    processed_rows = 0
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction
                    
                    for row in csv_reader:
                        # Parse and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]
                        
                        # Construct the insert query for the post, its attributes, and its relations
                        query = f'''
                        match
                            $creator isa Person, has id {row[8]};
                            $forum isa Forum, has id {row[9]};
                            $country isa Country, has id {row[10]};
                        insert
                            $post isa Post,
                                has id {row[1]},
                                has imageFile "{row[2]}",
                                has locationIP "{row[3]}",
                                has browserUsed "{row[4]}",
                                has language "{row[5]}",
                                has content "{row[6]}",
                                has length {row[7]},
                                has creationDate {formatted_date};
                            (created: $post, creator: $creator) isa hasCreator;
                            (contain: $post, container: $forum) isa containerOf;
                            (location: $post, locatedIn: $country) isa isLocatedIn;
                        '''
                        tx.query.insert(query)
                        
                        processed_rows += 1
                        
                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar
                    
                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        
                        pbar.update(processed_rows % batch_size)  # Update the progress bar
        
        pbar.close()

def load_comments(driver, batch_size=10000):
    with driver.session("ldbcsnb", SessionType.DATA) as session:
        directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Comment")

        
        # Estimate total rows (this is a rough estimate, assuming average file sizes)
        total_rows = count_rows_in_directory(directory)
        pbar = tqdm(total=total_rows, desc="Processed rows")
        
        for filename in os.listdir(directory):
            if filename.endswith('.csv'):
                filepath = os.path.join(directory, filename)
                with open(filepath, encoding='utf-8') as csv_file:
                    csv_reader = csv.reader(csv_file, delimiter='|')
                    next(csv_reader)  # Skip the header

                    processed_rows = 0
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction
                    
                    for row in csv_reader:
                        # Parse and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]
                        
                        # Parent relation setup (either to a Post or another Comment)
                        parent_relation = ""
                        if row[8]:  # ParentPostId
                            parent_relation = f'(reply: $comment, repliedTo: $parentPost) isa replyOf;'
                        elif row[9]:  # ParentCommentId
                            parent_relation = f'(reply: $comment, repliedTo: $parentComment) isa replyOf;'
                        
                        # Construct the insert query for the comment, its attributes, and its relations
                        query = f'''
                        match
                            $creator isa Person, has id {row[6]};
                            $country isa Country, has id {row[7]};
                        '''
                        if row[8]:
                            query += f'$parentPost isa Post, has id {row[8]};'
                        elif row[9]:
                            query += f'$parentComment isa Comment, has id {row[9]};'
                        
                        query += f'''
                        insert
                            $comment isa Comment,
                                has id {row[1]},
                                has locationIP "{row[2]}",
                                has browserUsed "{row[3]}",
                                has content "{row[4]}",
                                has length {row[5]},
                                has creationDate {formatted_date};
                            (creator: $creator, created: $comment) isa hasCreator;
                            (location: $comment, locatedIn: $country) isa isLocatedIn;
                            {parent_relation}
                        '''
                        
                        tx.query.insert(query)
                        processed_rows += 1
                        
                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar
                    
                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

        pbar.close()


def load_comment_hasTag_Tag(driver, directory, batch_size=1000):
    # Estimate total rows (this is a rough estimate, assuming average file sizes)
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    
    processed_rows = 0

    for filename in os.listdir(directory):
        if filename.endswith('.csv'):
            filepath = os.path.join(directory, filename)
            with open(filepath, encoding='utf-8') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter='|')
                next(csv_reader)  # Skip the header

                with driver.session("ldbcsnb", SessionType.DATA) as session:
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction
                    
                    for row in csv_reader:
                        query = f'''
                        match
                            $comment isa Comment, has id {row[1]};
                            $tag isa Tag, has id {row[2]};
                        insert
                            (tag: $tag, tagged: $comment) isa hasTag;
                        '''
                        
                        tx.query.insert(query)
                        processed_rows += 1
                        
                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar
                    
                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_forum_hasMember_Person(driver, directory, batch_size=1000):
    # Estimate total rows (this is a rough estimate, assuming average file sizes)
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    
    processed_rows = 0

    for filename in os.listdir(directory):
        if filename.endswith('.csv'):
            filepath = os.path.join(directory, filename)
            with open(filepath, encoding='utf-8') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter='|')
                next(csv_reader)  # Skip the header

                with driver.session("ldbcsnb", SessionType.DATA) as session:
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction
                    
                    for row in csv_reader:
                        # Extract and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        query = f'''
                        match
                            $forum isa Forum, has id {row[1]};
                            $person isa Person, has id {row[2]};
                        insert
                            $relation (member: $person, memberOf: $forum) isa hasMember;
                            $relation has creationDate {formatted_date};
                        '''
                        
                        tx.query.insert(query)
                        processed_rows += 1
                        
                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar
                    
                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_forum_hasTag_Tag(driver, directory, batch_size=1000):
    # Estimate total rows (this is a rough estimate, assuming average file sizes)
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    
    processed_rows = 0

    for filename in os.listdir(directory):
        if filename.endswith('.csv'):
            filepath = os.path.join(directory, filename)
            with open(filepath, encoding='utf-8') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter='|')
                next(csv_reader)  # Skip the header

                with driver.session("ldbcsnb", SessionType.DATA) as session:
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction
                    
                    for row in csv_reader:
                        # Extract and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        query = f'''
                        match
                            $forum isa Forum, has id {row[1]};
                            $tag isa Tag, has id {row[2]};
                        insert
                            $relation (tag: $tag, tagged: $forum) isa hasTag;
                            $relation has creationDate {formatted_date};
                        '''
                        
                        tx.query.insert(query)
                        processed_rows += 1
                        
                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar
                    
                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_person_hasInterest_Tag(driver, directory, batch_size=1000):
    # Estimate total rows (this is a rough estimate, assuming average file sizes)
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    
    processed_rows = 0

    for filename in os.listdir(directory):
        if filename.endswith('.csv'):
            filepath = os.path.join(directory, filename)
            with open(filepath, encoding='utf-8') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter='|')
                next(csv_reader)  # Skip the header

                with driver.session("ldbcsnb", SessionType.DATA) as session:
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction
                    
                    for row in csv_reader:
                        # Extract and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        query = f'''
                        match
                            $person isa Person, has id {row[1]};
                            $tag isa Tag, has id {row[2]};
                        insert
                            $relation (interested: $person, interests: $tag) isa hasInterest;
                            $relation has creationDate {formatted_date};
                        '''
                        
                        tx.query.insert(query)
                        processed_rows += 1
                        
                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar
                    
                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_person_knows_person(driver, directory, batch_size=1000):
    # Estimate total rows (this is a rough estimate, assuming average file sizes)
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    
    processed_rows = 0

    for filename in os.listdir(directory):
        if filename.endswith('.csv'):
            filepath = os.path.join(directory, filename)
            with open(filepath, encoding='utf-8') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter='|')
                next(csv_reader)  # Skip the header

                with driver.session("ldbcsnb", SessionType.DATA) as session:
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction
                    
                    for row in csv_reader:
                        # Extract and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        query = f'''
                        match
                            $person1 isa Person, has id {row[1]};
                            $person2 isa Person, has id {row[2]};
                        insert
                            $relation (friend: $person1, friend: $person2) isa knows;
                            $relation has creationDate {formatted_date};
                        '''
                        
                        tx.query.insert(query)
                        processed_rows += 1
                        
                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar
                    
                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_person_likes_comment(driver, directory, batch_size=1000):
    # Estimate total rows
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")

    processed_rows = 0

    for filename in os.listdir(directory):
        if filename.endswith('.csv'):
            filepath = os.path.join(directory, filename)
            with open(filepath, encoding='utf-8') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter='|')
                next(csv_reader)  # Skip the header

                with driver.session("ldbcsnb", SessionType.DATA) as session:
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction

                    for row in csv_reader:
                        # Extract and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        query = f'''
                        match
                            $person isa Person, has id {row[1]};
                            $comment isa Comment, has id {row[2]};
                        insert
                            $relation (liker: $person, message: $comment) isa likes;
                            $relation has creationDate {formatted_date};
                        '''

                        tx.query.insert(query)
                        processed_rows += 1

                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar

                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_person_likes_post(driver, directory, batch_size=1000):
    # Estimate total rows
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    
    processed_rows = 0

    for filename in os.listdir(directory):
        if filename.endswith('.csv'):
            filepath = os.path.join(directory, filename)
            with open(filepath, encoding='utf-8') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter='|')
                next(csv_reader)  # Skip the header

                with driver.session("ldbcsnb", SessionType.DATA) as session:
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction

                    for row in csv_reader:
                        # Extract and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        query = f'''
                        match
                            $person isa Person, has id {row[1]};
                            $post isa Post, has id {row[2]};
                        insert
                            $relation (liker: $person, message: $post) isa likes;
                            $relation has creationDate {formatted_date};
                        '''

                        tx.query.insert(query)
                        processed_rows += 1

                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar

                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_person_studyAt_university(driver, directory, batch_size=1000):
    # Estimate total rows
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    
    processed_rows = 0

    with driver.session("ldbcsnb", SessionType.DATA) as session:
        for filename in os.listdir(directory):
            if filename.endswith('.csv'):
                filepath = os.path.join(directory, filename)
                with open(filepath, encoding='utf-8') as csv_file:
                    csv_reader = csv.reader(csv_file, delimiter='|')
                    next(csv_reader)  # Skip the header

                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction

                    for row in csv_reader:
                        # Extract and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        query = f'''
                        match
                            $person isa Person, has id {row[1]};
                            $university isa University, has id {row[2]};
                        insert
                            $relation (student: $person, university: $university) isa studyAt;
                            $relation has creationDate {formatted_date};
                            $relation has classYear {row[3]};
                        '''

                        tx.query.insert(query)
                        processed_rows += 1

                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar

                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_person_workAt_company(driver, directory, batch_size=1000):
    # Estimate total rows
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    
    processed_rows = 0

    for filename in os.listdir(directory):
        if filename.endswith('.csv'):
            filepath = os.path.join(directory, filename)
            with open(filepath, encoding='utf-8') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter='|')
                next(csv_reader)  # Skip the header

                with driver.session("ldbcsnb", SessionType.DATA) as session:
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction

                    for row in csv_reader:
                        # Extract and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        query = f'''
                        match
                            $person isa Person, has id {row[1]};
                            $company isa Company, has id {row[2]};
                        insert
                            $relation (employee: $person, employer: $company) isa workAt;
                            $relation has creationDate {formatted_date};
                            $relation has workFrom {row[3]};
                        '''

                        tx.query.insert(query)
                        processed_rows += 1

                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar

                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_post_hasTag_tag(driver, directory, batch_size=1000):
    # Estimate total rows
    total_rows = count_rows_in_directory(directory)
    pbar = tqdm(total=total_rows, desc="Processed rows")
    
    processed_rows = 0

    for filename in os.listdir(directory):
        if filename.endswith('.csv'):
            filepath = os.path.join(directory, filename)
            with open(filepath, encoding='utf-8') as csv_file:
                csv_reader = csv.reader(csv_file, delimiter='|')
                next(csv_reader)  # Skip the header

                with driver.session("ldbcsnb", SessionType.DATA) as session:
                    tx = session.transaction(TransactionType.WRITE)  # Start the first transaction

                    for row in csv_reader:
                        # Extract and format the creation date
                        creation_date = datetime.fromisoformat(row[0])
                        formatted_date = creation_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

                        query = f'''
                        match
                            $post isa Post, has id {row[1]};
                            $tag isa Tag, has id {row[2]};
                        insert
                            $relation (tag: $tag, tagged: $post) isa hasTag;
                            $relation has creationDate {formatted_date};
                        '''

                        tx.query.insert(query)
                        processed_rows += 1

                        # Commit and start a new transaction if batch size is reached
                        if processed_rows % batch_size == 0:
                            tx.commit()
                            tx = session.transaction(TransactionType.WRITE)
                            pbar.update(batch_size)  # Update the progress bar

                    # Commit the last batch if there are any remaining rows
                    if processed_rows % batch_size != 0:
                        tx.commit()
                        pbar.update(processed_rows % batch_size)  # Update the progress bar

    pbar.close()

def load_relations(driver, start_time):
    ROOT_DYNAMIC = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic")
    batchsize = 10000
    # Load Comment_hasTag_Tag
    load_comment_hasTag_Tag(driver, os.path.join(ROOT_DYNAMIC, "Comment_hasTag_Tag"),batchsize)
    print_elapsed_time(start_time, "Loaded Comment_hasTag_Tag")

    # Load Forum_hasMember_Person
    load_forum_hasMember_Person(driver, os.path.join(ROOT_DYNAMIC, "Forum_hasMember_Person"),batchsize)
    print_elapsed_time(start_time, "Loaded Forum_hasMember_Person")

    # Load Forum_hasTag_Tag
    load_forum_hasTag_Tag(driver, os.path.join(ROOT_DYNAMIC, "Forum_hasTag_Tag"),batchsize)
    print_elapsed_time(start_time, "Loaded Forum_hasTag_Tag")

    # Load Person_hasInterest_Tag
    load_person_hasInterest_Tag(driver, os.path.join(ROOT_DYNAMIC, "Person_hasInterest_Tag"),batchsize)
    print_elapsed_time(start_time, "Loaded Person_hasInterest_Tag")

    # Load Person_knows_Person
    load_person_knows_person(driver, os.path.join(ROOT_DYNAMIC, "Person_knows_Person"),batchsize)
    print_elapsed_time(start_time, "Loaded Person_knows_Person")

    # Load Person_likes_Comment
    load_person_likes_comment(driver, os.path.join(ROOT_DYNAMIC, "Person_likes_Comment"),batchsize)
    print_elapsed_time(start_time, "Loaded Person_likes_Comment")

    # Load Person_likes_Post
    load_person_likes_post(driver, os.path.join(ROOT_DYNAMIC, "Person_likes_Post"),batchsize)
    print_elapsed_time(start_time, "Loaded Person_likes_Post")

    # Load Person_studyAt_University
    load_person_studyAt_university(driver, os.path.join(ROOT_DYNAMIC, "Person_studyAt_University"),batchsize)
    print_elapsed_time(start_time, "Loaded Person_studyAt_University")

    # Load Person_workAt_Company
    load_person_workAt_company(driver, os.path.join(ROOT_DYNAMIC, "Person_workAt_Company"),batchsize)
    print_elapsed_time(start_time, "Loaded Person_workAt_Company")

    # Load Post_hasTag_Tag
    load_post_hasTag_tag(driver, os.path.join(ROOT_DYNAMIC, "Post_hasTag_Tag"),batchsize)
    print_elapsed_time(start_time, "Loaded Post_hasTag_Tag")

def print_elapsed_time(start_time, message):
    elapsed_time = time.time() - start_time
    print(f"{message} - Elapsed time: {elapsed_time:.2f} seconds")

def main():
    start_time = time.time()
    # Connect to the TypeDB server
    with TypeDB.core_driver("localhost:1729") as driver:
        # Check if the database exists
        if driver.databases.contains("ldbcsnb"):
            driver.databases.get("ldbcsnb").delete()
        # Create a TypeDB database
        driver.databases.create("ldbcsnb")
        # print("hi")
        with driver.session("ldbcsnb", SessionType.SCHEMA) as session:
            with open("schema.tql", "r") as schema:
                with session.transaction(TransactionType.WRITE) as tx:
                    query = schema.read()
                    tx.query.define(query)
                    tx.commit()

        print("schema,",time.time() - start_time)
        # #static
        load_place(driver)
        print_elapsed_time(start_time, "Loaded Place")
        
        load_organization(driver)
        print_elapsed_time(start_time, "Loaded Organization")
        
        load_tagclass(driver)
        print_elapsed_time(start_time, "Loaded TagClass")
        
        load_tags(driver, 10000)
        print_elapsed_time(start_time, "Loaded Tags")

        print_elapsed_time(start_time, "Loaded Static Data")        
        
        # dynamic
        
        load_person(driver, 10000)
        print_elapsed_time(start_time, "Loaded Person")

        load_forum(driver, 10000)
        print_elapsed_time(start_time, "Loaded Forum")
        
        load_posts(driver,10000)
        print_elapsed_time(start_time, "Loaded Posts") 
        
        load_comments(driver, 10000)
        print_elapsed_time(start_time, "Loaded Comments")
        
        load_relations(driver, start_time)
        print_elapsed_time(start_time, "Loaded Relations")
        
        print_elapsed_time(start_time, "Loaded Dynamic Data")

if __name__ == "__main__":
    main()    

