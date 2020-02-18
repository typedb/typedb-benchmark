import fileinput
import re

file_prefix = "logs/"
file_suffix = ".log"

logback_regex = re.compile(r"\d\d:\d\d:\d\d\.\d\d\d\s\[.+\]\s.+\s\-\s(.*)")
tracker_regex = re.compile(r"\((.+)\)(.*)")

def openfile(filekey):
    filename = file_prefix + filekey.lower().replace(" ", "_").replace(":", "-") + file_suffix
    return open(filename, "w")

def logsplit(inp):
    file_dict = {}
    current_file = None

    for line in inp:
        logback_match = logback_regex.match(line)
        if logback_match != None:
            tracker_match = tracker_regex.match(logback_match.group(1))
            if tracker_match != None:
                tracker = tracker_match.group(1)
                line = tracker_match.group(2)

                if tracker in file_dict:
                    current_file = file_dict[tracker]
                else:
                    current_file = openfile(tracker)
                    file_dict[tracker] = current_file
            else:
                current_file = None
        
        if current_file != None:
            current_file.write(line)
        else:
            print(line, end = "")

if __name__ == "__main__":
    logsplit(fileinput.input())



