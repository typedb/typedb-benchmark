import numpy as np
import json
import urllib.request

from line_chart import line_chart
from overview_chart import overview_chart
import os


def get_json(json_url):
    user_agent = 'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.7) Gecko/2009021910 Firefox/3.0.7'
    request = urllib.request.Request(json_url, headers={"Authorization": f"Token {os.environ.get('GRABL_USER_TOKEN')}", 'User-Agent': user_agent})
    with urllib.request.urlopen(request) as response:
        return json.loads(response.read())


def get_trace_overviews(json_data):
    performance_analysis = json_data["performance-analysis"]
    trace_overviews = performance_analysis["trace-overviews"]
    return trace_overviews


if __name__ == "__main__":
    neo4j_overviews = get_trace_overviews(get_json(f"https://grabl.io/api/data/jmsfltchr/simulation/a3ac46dd615e256a8935ba47ae6fea79a0ba2f82/analysis/performance-analysis?q=%7B%22id%22%3A%7B%22selected%22%3A%22819791133617188864%22%7D,%22trace%22%3A%7B%22path%22%3A%5B%7B%22optional%22%3Atrue%7D%5D,%22tracker%22%3A%7B%22optional%22%3Atrue%7D,%22labels%22%3A%7B%22names%22%3A%5B%5D%7D,%22iteration%22%3A%7B%7D%7D%7D"))
    grakn_overviews = get_trace_overviews(get_json(f"https://grabl.io/api/data/jmsfltchr/simulation/a3ac46dd615e256a8935ba47ae6fea79a0ba2f82/analysis/performance-analysis?q=%7B%22id%22%3A%7B%22selected%22%3A%223651461777878837248%22%7D,%22trace%22%3A%7B%22path%22%3A%5B%7B%22optional%22%3Atrue%7D%5D,%22tracker%22%3A%7B%22optional%22%3Atrue%7D,%22labels%22%3A%7B%22names%22%3A%5B%5D%7D,%22iteration%22%3A%7B%7D%7D%7D"))

    neo4j_color = [24/256, 127/256, 183/256]
    grakn_color = [113/256, 87/256, 202/256]
    bar_edgecolor = "#000"

    agents = ['InsertPersonAction', 'closeSession', 'openSession']
    x = np.arange(len(agents))  # the label locations
    width = 0.2  # the width of the bars
    capsize = 3  # width of the errorbar caps

    # Overview charts
    overview_chart(1, agents, x, width, capsize, bar_edgecolor, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color)
    overview_chart(3, agents, x, width, capsize, bar_edgecolor, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color)
    overview_chart(5, agents, x, width, capsize, bar_edgecolor, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color)

    # Line charts
    for agent in agents:
        line_chart(agent, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color, capsize)
