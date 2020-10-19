import matplotlib.pyplot as plt
import numpy as np
import json


def get_json(file):
    with open(file) as json_file:
        return json.load(json_file)


def get_trace_overviews(json_data):
    performance_analysis = json_data["performance-analysis"]
    #     commit_sha = performance_analysis["commit"]["sha"]
    trace_overviews = performance_analysis["trace-overviews"]
    return trace_overviews


def get_overview_data(trace_overviews, iteration):
    values = []
    for overview_name, overview in trace_overviews.items():
        values.append(int(overview["durations"][iteration][2]))
    return trace_overviews.keys(), values


labels, grakn_values = get_overview_data(get_trace_overviews(get_json('grabl_grakn_mock_data.json')), "1")
_, neo4j_values = get_overview_data(get_trace_overviews(get_json('grabl_neo4j_mock_data.json')), "1")

neo4j_color = [24/256, 127/256, 183/256]
grakn_color = [113/256, 87/256, 202/256]
bar_edgecolor = "#000"

# labels = ['Person Birth', 'Marriage', 'Employment', 'Relocation', 'Friendship']
# neo4j_means = [20, 34, 30, 35, 27]
neo4j_means = neo4j_values
neo4j_error = [20, 40, 00, 50, 70, 20, 40, 00, 50, 70, 40]
# grakn_means = [25, 32, 34, 20, 25]
grakn_means = grakn_values
grakn_error = [50, 20, 40, 00, 50, 50, 20, 40, 00, 50, 40]

x = np.arange(len(labels))  # the label locations
width = 0.2  # the width of the bars
capsize = 3  # width of the errorbar caps

fig, ax = plt.subplots()
rects2 = ax.bar(x - width/2,
                neo4j_means,
                width,
                yerr=neo4j_error,
                capsize=capsize,
                label='Neo4j',
                color=neo4j_color,
                edgecolor=bar_edgecolor)
rects1 = ax.bar(x + width/2,
                grakn_means,
                width,
                yerr=grakn_error,
                capsize=capsize,
                label='Grakn',
                color=grakn_color,
                edgecolor=bar_edgecolor)


# def bar(values, error, label, color):
#     ax.bar(x + width / 2,
#            values,
#            width,
#            yerr=error,
#            capsize=capsize,
#            label=label,
#            color=color,
#            edgecolor=bar_edgecolor)
#
#
# bar(neo4j_means, neo4j_error, "Neo4j", neo4j_color)
# bar(grakn_means, grakn_error, "Grakn", grakn_color)

# Add some text for labels, title and custom x-axis tick labels, etc.
ax.set_ylabel('Time (s)')
ax.set_xlabel('Agents')
ax.set_title('Time taken to execute agent')
ax.set_xticks(x)
ax.set_xticklabels(labels)
ax.legend()

# rotate x-axis labels
plt.xticks(rotation=45, ha='right')

# def autolabel(rects):
#     """Attach a text label above each bar in *rects*, displaying its height."""
#     for rect in rects:
#         height = rect.get_height()
#         ax.annotate('{}'.format(height),
#                     xy=(rect.get_x() + rect.get_width() / 2, height),
#                     xytext=(0, 3),  # 3 points vertical offset
#                     textcoords="offset points",
#                     ha='center', va='bottom')

# autolabel(rects1)
# autolabel(rects2)

# fig.tight_layout()

plt.show()

# import numpy as np
# import matplotlib.pyplot as plt
#
#
# N = 5
# menMeans = (20, 35, 30, 35, 27)
# womenMeans = (25, 32, 34, 20, 25)
# menStd = (2, 3, 4, 1, 2)
# womenStd = (3, 5, 2, 3, 3)
# ind = np.arange(N)    # the x locations for the groups
# width = 0.35       # the width of the bars: can also be len(x) sequence
#
# p1 = plt.bar(ind, menMeans, width, yerr=menStd)
# p2 = plt.bar(ind, womenMeans, width,
#              bottom=menMeans, yerr=womenStd)
#
# plt.ylabel('Scores')
# plt.title('Scores by group and gender')
# plt.xticks(ind, ('G1', 'G2', 'G3', 'G4', 'G5'))
# plt.yticks(np.arange(0, 81, 10))
# plt.legend((p1[0], p2[0]), ('Men', 'Women'))
#
# plt.show()