#
# Copyright (C) 2021 Grakn Labs
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

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



