import csv
import os
import json

ROOT = os.getcwd()
SF = 0.1

def main():

    # open config.json and load it into data
    data = json.load(open(os.path.join(ROOT, "config.json")))
    
    # Function to get full file paths in a directory
    def get_full_file_paths(directory):
        return [os.path.join(directory, filename) for filename in os.listdir(directory) if filename.endswith('.csv')]
    
    # all csv files inside place folder in an array
    place_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "Place")
    place_csv = get_full_file_paths(place_directory)
    
    if not os.path.exists(os.path.join(place_directory, "Country")):
        os.makedirs(os.path.join(place_directory, "Country"))
    if not os.path.exists(os.path.join(place_directory, "City")):
        os.makedirs(os.path.join(place_directory, "City"))
    if not os.path.exists(os.path.join(place_directory, "Continent")):
        os.makedirs(os.path.join(place_directory, "Continent"))

    # for all Place preprocess them into multiple csv file according to type
    for filename in place_csv:
        with open(filename, encoding='utf-8') as csv_file:
            csv_reader = csv.reader(csv_file, delimiter='|')
            # Extract the header
            header = next(csv_reader)
            header_str = '|'.join(header) + '\n'
            file_name_only = os.path.basename(filename)

            # Country CSV
            country_path = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "Place", "Country", file_name_only)
            with open(country_path, 'w', encoding='utf-8') as country_csv:
                country_csv.write(header_str)

                # City CSV
                city_path = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "Place", "City", file_name_only)
                with open(city_path, 'w', encoding='utf-8') as city_csv:
                    city_csv.write(header_str)

                    # Continent CSV
                    continent_path = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "Place", "Continent", file_name_only)
                    with open(continent_path, 'w', encoding='utf-8') as continent_csv:
                        continent_csv.write(header_str)

                        for row in csv_reader:
                            row_str = '|'.join(row) + '\n'
                            if row[3] == "Country":
                                country_csv.write(row_str)
                            elif row[3] == "City":
                                city_csv.write(row_str)
                            elif row[3] == "Continent":
                                continent_csv.write(row_str)
                            else:
                                print("Error in Place preprocessing: unrecognized type in row", row)
                                # Handle error as needed

    # all csv files inside country folder in an array
    country_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "static", "Place", "Country")
    country_csv = get_full_file_paths(country_directory)

    data["entities"]["Country"]["data"] = country_csv
    data["relations"]["isPartOfForCountry"]["data"] = country_csv

    # all csv files inside city folder in an array
    city_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "static", "Place", "City")
    city_csv = get_full_file_paths(city_directory)

    data["entities"]["City"]["data"] = city_csv
    data["relations"]["isPartOfForCity"]["data"] = city_csv

    # all csv files inside continent folder in an array
    continent_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "static", "Place", "Continent")
    continent_csv = get_full_file_paths(continent_directory)

    data["entities"]["Continent"]["data"] = continent_csv
    

    
    # all csv files inside Organisation folder in an array
    Organisation_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "static", "Organisation")
    Organisation_csv = get_full_file_paths(Organisation_directory)
    
    # create company and university folder in organisation folder
    if not os.path.exists(os.path.join(Organisation_directory, "Company")):
        os.makedirs(os.path.join(Organisation_directory, "Company"))
    if not os.path.exists(os.path.join(Organisation_directory, "University")):
        os.makedirs(os.path.join(Organisation_directory, "University"))
    
    # for all Organisation preprocess them into multiple csv file according to type
    for filename in Organisation_csv:
        with open(filename, encoding='utf-8') as csv_file:
            csv_reader = csv.reader(csv_file, delimiter='|')
            # Extract the header
            header = next(csv_reader)
            header_str = '|'.join(header) + '\n'
            file_name_only = os.path.basename(filename)
            
            # Company CSV
            company_path = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "Organisation", "Company", file_name_only)
            with open(company_path, 'w', encoding='utf-8') as company_csv:
                company_csv.write(header_str)

                # University CSV
                university_path = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "static", "Organisation", "University", file_name_only)
                with open(university_path, 'w', encoding='utf-8') as university_csv:
                    university_csv.write(header_str)
                    for row in csv_reader:
                        row_str = '|'.join(row) + '\n'
                        # # print(row_str)
                        if row[1] == "Company":
                            company_csv.write(row_str)
                        elif row[1] == "University":
                            university_csv.write(row_str)
                        else:
                            print("Error in Organisation preprocessing: unrecognized type in row", row)
                            # Handle error as needed
                            
    # all csv files inside company folder in an array
    company_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "static", "Organisation", "Company")
    company_csv = get_full_file_paths(company_directory)
            
    data["entities"]["Company"]["data"] = company_csv
    data["relations"]["isLocatedInForCompany"]["data"] = company_csv
    
    
    # all csv files inside university folder in an array
    university_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "static", "Organisation", "University")
    university_csv = get_full_file_paths(university_directory)

    data["entities"]["University"]["data"] = university_csv
    data["relations"]["isLocatedInForUniversity"]["data"] = university_csv
    
    # all csv files inside tagclass folder in an array
    tagclass_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "static", "TagClass")
    tagclass_csv = get_full_file_paths(tagclass_directory)
    
    data["entities"]["TagClass"]["data"] = tagclass_csv
    data["relations"]["isSubclassOf"]["data"] = tagclass_csv
            
    # all csv files inside tag folder in an array
    tag_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "static", "Tag")
    tag_csv = get_full_file_paths(tag_directory)
    
    data["entities"]["Tag"]["data"] = tag_csv
    data["relations"]["hasType"]["data"] = tag_csv
    
    # all csv files inside person folder in an array 
    person_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "dynamic", "Person")
    person_csv = get_full_file_paths(person_directory)
            
    data["entities"]["Person"]["data"] = person_csv
    data["relations"]["isLocatedInForPerson"]["data"] = person_csv
    
    # all csv files inside forum folder in an array
    forum_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot", "dynamic", "Forum")
    forum_csv = get_full_file_paths(forum_directory)
    
    data["entities"]["Forum"]["data"] = forum_csv
    data["relations"]["hasModerator"]["data"] = forum_csv
    
    
    # all csv files inside post folder in an array
    post_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Post")
    post_csv = get_full_file_paths(post_directory)
    
    data["entities"]["Post"]["data"] = post_csv
    data["relations"]["hasCreator"]["data"] = post_csv
    data["relations"]["containerOf"]["data"] = post_csv
    data["relations"]["isLocatedInForPost"]["data"] = post_csv
        
    # all csv files inside comment folder in an array
    comment_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Comment")
    comment_csv = get_full_file_paths(comment_directory)
    
    data["entities"]["Comment"]["data"] = comment_csv
    data["relations"]["hasCreatorForComments"]["data"] = comment_csv
    data["relations"]["isLocatedInForComments"]["data"] = comment_csv
    data["relations"]["replyOfPost"]["data"] = comment_csv
    data["relations"]["replyOfComment"]["data"] = comment_csv
    
    
    # all csv files inside Comment_hasTag_Tag folder in an array
    comment_hasTag_tag_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Comment_hasTag_Tag")
    comment_hasTag_tag_csv = get_full_file_paths(comment_hasTag_tag_directory)
    
    data["relations"]["hasTag"]["data"] = comment_hasTag_tag_csv
    
    # all csv files inside Forum_hasMember_Person folder in an array
    forum_hasMember_person_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Forum_hasMember_Person")
    forum_hasMember_person_csv = get_full_file_paths(forum_hasMember_person_directory)
            
    data["relations"]["hasMember"]["data"] = forum_hasMember_person_csv

    # all csv files inside Forum_hasTag_Tag folder in an array
    forum_hasTag_tag_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Forum_hasTag_Tag")
    forum_hasTag_tag_csv = get_full_file_paths(forum_hasTag_tag_directory)
            
    data["relations"]["hasTagForum"]["data"] = forum_hasTag_tag_csv
    
    # all csv files inside Post_hasTag_Tag folder in an array
    post_hasTag_tag_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot", "dynamic", "Post_hasTag_Tag")
    post_hasTag_tag_csv = get_full_file_paths(post_hasTag_tag_directory)
        
    data["relations"]["hasTagPost"]["data"] = post_hasTag_tag_csv
    
    # all csv files inside Person_hasInterest_Tag folder in an array
    person_hasInterest_tag_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk", "initial_snapshot","dynamic", "Person_hasInterest_Tag")
    person_hasInterest_tag_csv = get_full_file_paths(person_hasInterest_tag_directory)

    
    data["relations"]["hasInterest"]["data"] = person_hasInterest_tag_csv
    
    # all csv files inside Person_knows_Person folder in an array
    person_knows_person_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot","dynamic", "Person_knows_Person")
    person_knows_person_csv = get_full_file_paths(person_knows_person_directory)
           
    data["relations"]["knows"]["data"] = person_knows_person_csv
    
    # all csv files inside Person_likes_Comment folder in an array
    person_likes_comment_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot","dynamic", "Person_likes_Comment")
    person_likes_comment_csv = get_full_file_paths(person_likes_comment_directory)
               
    data["relations"]["likesComment"]["data"] = person_likes_comment_csv

    # all csv files inside Person_likes_Post folder in an array
    person_likes_post_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot","dynamic", "Person_likes_Post")
    person_likes_post_csv = get_full_file_paths(person_likes_post_directory)
           
    data["relations"]["likesPost"]["data"] = person_likes_post_csv
    
    # all csv files inside Person_studyAt_University folder in an array
    person_studyAt_university_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot","dynamic", "Person_studyAt_University")
    person_studyAt_university_csv = get_full_file_paths(person_studyAt_university_directory)
    
    data["relations"]["studyAt"]["data"] = person_studyAt_university_csv
    
    # all csv files inside Person_workAt_Company folder in an array
    person_workAt_company_directory = os.path.join(ROOT, "data", f"out-sf{SF}", "graphs", "csv", "bi", "composite-merged-fk","initial_snapshot","dynamic", "Person_workAt_Company")
    person_workAt_company_csv = get_full_file_paths(person_workAt_company_directory)
        
    data["relations"]["workAt"]["data"] = person_workAt_company_csv
    
    with open(os.path.join(ROOT, "config.json"), 'w') as outfile:
        json.dump(data, outfile, indent=4)

if __name__ == "__main__":
    main()    