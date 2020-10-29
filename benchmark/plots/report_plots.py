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
    grakn_overviews = get_trace_overviews(get_json(f"https://grabl.io/api/data/jmsfltchr/simulation/f5d088a5a3f5f993f5edef009e62b2da9ca57fca/analysis/performance-analysis?q=%7B%22id%22%3A%7B%22selected%22%3A%22389987581477393408%22%7D,%22trace%22%3A%7B%22path%22%3A%5B%7B%22optional%22%3Atrue%7D%5D,%22tracker%22%3A%7B%22optional%22%3Atrue%7D,%22labels%22%3A%7B%22names%22%3A%5B%5D%7D,%22iteration%22%3A%7B%7D%7D%7D"))
    neo4j_overviews = get_trace_overviews(get_json(f"https://grabl.io/api/data/jmsfltchr/simulation/f5d088a5a3f5f993f5edef009e62b2da9ca57fca/analysis/performance-analysis?q=%7B%22id%22%3A%7B%22selected%22%3A%227339689722720291840%22%7D,%22trace%22%3A%7B%22path%22%3A%5B%7B%22optional%22%3Atrue%7D%5D,%22tracker%22%3A%7B%22optional%22%3Atrue%7D,%22labels%22%3A%7B%22names%22%3A%5B%5D%7D,%22iteration%22%3A%7B%7D%7D%7D"))

    grakn_color = [113/256, 87/256, 202/256]
    neo4j_color = [24/256, 127/256, 183/256]
    bar_edgecolor = "#000"

    agents = list(set(grakn_overviews.keys()).intersection(set(neo4j_overviews.keys())))
    agents.remove("closeClient")
    agents.remove("closeSession")
    agents.remove("openSession")

    x = np.arange(len(agents))  # the label locations
    width = 0.2  # the width of the bars
    capsize = 3  # width of the errorbar caps

    image_extension = "png"

    # Overview charts
    overview_iterations_to_plot = [4, 8, 12]
    overview_chart(overview_iterations_to_plot, agents, x, width, capsize, bar_edgecolor, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color, image_extension)

    # Line charts
    for agent in agents:
        line_chart(agent, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color, capsize, image_extension)
