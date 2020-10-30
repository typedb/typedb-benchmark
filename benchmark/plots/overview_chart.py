import collections

from matplotlib import pyplot as plt
from matplotlib.pyplot import tight_layout


def overview_chart(iterations, labels, x, width, capsize, bar_edgecolor, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color, image_extension):

    fig, axs = plt.subplots(len(iterations), 1, sharex=True, figsize=(20, 10))
    first = True

    SMALL_SIZE = 14
    MEDIUM_SIZE = 18

    neo4j_overviews = sort_overviews(neo4j_overviews)
    grakn_overviews = sort_overviews(grakn_overviews)

    for iteration, ax in zip(iterations, axs):
        neo4j_average = unwrap_overviews(neo4j_overviews, "average", labels, iteration)
        neo4j_error = unwrap_overviews(neo4j_overviews, "standard-deviation", labels, iteration)
        grakn_average = unwrap_overviews(grakn_overviews, "average", labels, iteration)
        grakn_error = unwrap_overviews(grakn_overviews, "standard-deviation", labels, iteration)

        bars1 = ax.bar(x - width / 2,
                       neo4j_average,
                       width,
                       yerr=neo4j_error,
                       capsize=capsize,
                       label='Neo4j',
                       color=neo4j_color,
                       edgecolor=bar_edgecolor)

        bars2 = ax.bar(x + width / 2,
                       grakn_average,
                       width,
                       yerr=grakn_error,
                       capsize=capsize,
                       label='Grakn',
                       color=grakn_color,
                       edgecolor=bar_edgecolor)

        # Add some text for labels, title and custom x-axis tick labels, etc.
        # ax.set_ylabel('Time (ms)')
        ax.set_ylabel('Time (ms)', fontsize=MEDIUM_SIZE)
        ax.set_title(f'Time Taken to Execute Agents during Iteration {iteration}')
        ax.set_xticks(x)
        ax.set_xticklabels(strip_labels(labels), rotation=45, ha='right')
        if first:
            ax.legend(loc='upper right')
            first = False

    plt.xlabel('Agent', fontsize=MEDIUM_SIZE)

    tight_layout()
    plt.savefig(f'overview.{image_extension}')


def sort_overviews(overviews):
    return collections.OrderedDict(sorted(overviews.items()))


def unwrap_overviews(overviews, metric, overviews_to_plot, iteration):
    values = []
    for overview_name in overviews_to_plot:
        values.append(overviews.get(overview_name)[metric].get(iteration))
    return values


def strip_labels(labels):
    stripped = []

    for label in labels:
        if label.endswith("Agent"):
            label = label[:-5]
        stripped.append(label)

    return stripped
