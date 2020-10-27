from matplotlib import pyplot as plt


def overview_chart(iteration, labels, x, width, capsize, bar_edgecolor, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color):
    neo4j_average = unwrap_overviews(neo4j_overviews, "average", labels, iteration)
    neo4j_error = unwrap_overviews(neo4j_overviews, "standard-deviation", labels, iteration)
    grakn_average = unwrap_overviews(grakn_overviews, "average", labels, iteration)
    grakn_error = unwrap_overviews(grakn_overviews, "standard-deviation", labels, iteration)

    fig, ax = plt.subplots()
    rects2 = ax.bar(x - width / 2,
                    neo4j_average,
                    width,
                    yerr=neo4j_error,
                    capsize=capsize,
                    label='Neo4j',
                    color=neo4j_color,
                    edgecolor=bar_edgecolor)
    rects1 = ax.bar(x + width / 2,
                    grakn_average,
                    width,
                    yerr=grakn_error,
                    capsize=capsize,
                    label='Grakn',
                    color=grakn_color,
                    edgecolor=bar_edgecolor)

    # # For some unknown crazy reason, putting the above calls into a function doesn't work
    # def bar(values, error, label, color):
    #     global ax
    #     # global plt
    #     ax.bar(x + width / 2,
    #            values,
    #            width,
    #            yerr=error,
    #            capsize=capsize,
    #            label=label,
    #            color=color,
    #            edgecolor=bar_edgecolor,
    #            ax=ax)
    #     # return ax
    #
    # bar(neo4j_average, neo4j_error, "Neo4j", neo4j_color)
    # bar(grakn_average, grakn_error, "Grakn", grakn_color)

    # Add some text for labels, title and custom x-axis tick labels, etc.
    ax.set_ylabel('Time (ms)')
    ax.set_xlabel('Agent')
    ax.set_title('Time taken to execute agent')
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()

    # rotate x-axis labels
    # plt.xticks(rotation=15, ha='right')

    plt.savefig(f'overview_iteration_{iteration}.svg')


def unwrap_overviews(overviews, metric, overviews_to_plot, iteration):
    values = []
    for overview_name in overviews_to_plot:
        values.append(overviews.get(overview_name)[metric].get(str(iteration)))
    return values
