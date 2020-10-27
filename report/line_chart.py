from matplotlib import pyplot as plt


def line_chart(agent_name, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color, capsize):

    iterations = list(grakn_overviews.get(agent_name)['average'].keys())

    grakn_averages = unwrap_overviews_for_lines(agent_name, grakn_overviews, "average")
    grakn_error = unwrap_overviews_for_lines(agent_name, grakn_overviews, "standard-deviation")
    neo4j_averages = unwrap_overviews_for_lines(agent_name, neo4j_overviews, "average")
    neo4j_error = unwrap_overviews_for_lines(agent_name, neo4j_overviews, "standard-deviation")

    fig = plt.figure()
    plt.errorbar(iterations, grakn_averages, yerr=grakn_error, label='Grakn', capsize=capsize, color=grakn_color, lolims=True)
    plt.errorbar(iterations, neo4j_averages, yerr=neo4j_error, label='Neo4j', capsize=capsize, color=neo4j_color, lolims=True)

    ax = fig.axes[0]
    ax.set_ylabel('Time (ms)')
    ax.set_xlabel('Iteration')
    ax.set_title('Time Taken to Execute Agent per Iteration')
    ax.set_xticks(iterations)
    plt.legend(loc='upper left')
    plt.savefig(f'agent_{agent_name}.svg')


def unwrap_overviews_for_lines(overview_name, overviews, key):
    return overviews.get(overview_name)[key].values()
