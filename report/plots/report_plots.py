#
# Copyright (C) 2021 Vaticle
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

import argparse
import collections
import json
import os
import urllib.request
from line_chart import line_chart
from overview_chart import overview_chart


def get_json(json_url):
    user_agent = 'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.7) Gecko/2009021910 Firefox/3.0.7'
    request = urllib.request.Request(json_url, headers={"Authorization": f"Token {os.environ.get('FACTORY_USER_TOKEN')}",
                                                        "User-Agent": user_agent})
    with urllib.request.urlopen(request) as response:
        return json.loads(response.read())


def get_trace_overviews(json_data):
    performance_analysis = json_data["performance-analysis"]
    trace_overviews = performance_analysis["trace-overviews"]
    return trace_overviews


def reformat_iterations_in_overview_metric_entry(overview_metric_entry):
    reformatted = {}
    for iteration_str, value in overview_metric_entry.items():
        reformatted[int(iteration_str)] = value
    return reformatted


def reformat_iterations_in_overviews(overviews):
    for agent_name, overview in overviews.items():
        for metric, metric_entry in overview.items():
            if metric in ["average", "standard-deviation"]:
                overview[metric] = collections.OrderedDict(
                    sorted(reformat_iterations_in_overview_metric_entry(metric_entry).items()))
    return overviews


def get_json_url(commit_sha, analysis_id):
    return f"https://grabl.io/api/data/jmsfltchr/simulation/{commit_sha}/analysis/performance-analysis?q={{" \
           f"%22analysis%22:{{%22id%22:{{%22selected%22:%22{analysis_id}%22}},%22trace%22:{{%22path%22:[{{" \
           f"%22optional%22:true}}],%22tracker%22:{{%22optional%22:true}},%22labels%22:{{%22names%22:[]}}," \
           f"%22iteration%22:{{}}}}}}}}"


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Create plots for a benchmark report.')
    parser.add_argument('commit_sha', type=str,
                        help='The commit sha of the simulation to use to generate the report')
    parser.add_argument('typedbanalysis_id', type=int,
                        help='The analysis ID of the TypeDB analysis from the commit')
    parser.add_argument('neo4j_analysis_id', type=int,
                        help='The analysis ID of the Neo4j analysis from the commit')
    parser.add_argument('overview_iterations_to_plot', type=int, nargs='+',
                        help='The iterations to plot for the overview chart')
    args = parser.parse_args()

    typedboverviews = reformat_iterations_in_overviews(
        get_trace_overviews(get_json(get_json_url(args.commit_sha, args.typedbanalysis_id))))
    neo4j_overviews = reformat_iterations_in_overviews(
        get_trace_overviews(get_json(get_json_url(args.commit_sha, args.neo4j_analysis_id))))

    typedbcolor = [113 / 256, 87 / 256, 202 / 256]
    neo4j_color = [24 / 256, 127 / 256, 183 / 256]
    bar_edgecolor = "#000"

    agents_to_chart = list(set(typedboverviews.keys()).intersection(set(neo4j_overviews.keys())))
    agents_to_chart.remove("closeClient")
    agents_to_chart.remove("closeSession")
    agents_to_chart.remove("openSession")

    capsize = 3  # width of the errorbar caps

    image_extension = "png"

    # Overview charts
    overview_chart(args.overview_iterations_to_plot, agents_to_chart, capsize, bar_edgecolor, typedboverviews,
                   neo4j_overviews, typedbcolor, neo4j_color, "png")

    # Line charts
    for agent in agents_to_chart:
        line_chart(agent, typedboverviews, neo4j_overviews, typedbcolor, neo4j_color, capsize, image_extension)
