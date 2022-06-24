#
# Copyright (C) 2022 Vaticle
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

import collections
import numpy as np
from matplotlib import pyplot as plt
from matplotlib.pyplot import tight_layout


def overview_chart(iterations, agents, capsize, bar_edgecolor, typedboverviews, neo4j_overviews, typedbcolor,
                   neo4j_color, image_extension):

    fig, axs = plt.subplots(len(iterations), 1, sharex=True, figsize=(20, 20))
    first = True

    SMALL_SIZE = 14
    MEDIUM_SIZE = 18

    label_locations = np.arange(len(agents))
    bar_width = 0.3  # the width of the bars

    neo4j_overviews = sort_overviews(neo4j_overviews)
    typedboverviews = sort_overviews(typedboverviews)

    for iteration, ax in zip(iterations, axs):
        neo4j_average = unwrap_overviews(neo4j_overviews, "average", agents, iteration)
        neo4j_error = unwrap_overviews(neo4j_overviews, "standard-deviation", agents, iteration)
        typedbaverage = unwrap_overviews(typedboverviews, "average", agents, iteration)
        typedberror = unwrap_overviews(typedboverviews, "standard-deviation", agents, iteration)

        bars1 = ax.bar(label_locations - bar_width / 2,
                       neo4j_average,
                       bar_width,
                       yerr=neo4j_error,
                       capsize=capsize,
                       label='Neo4j',
                       color=neo4j_color,
                       edgecolor=bar_edgecolor)

        bars2 = ax.bar(label_locations + bar_width / 2,
                       typedbaverage,
                       bar_width,
                       yerr=typedberror,
                       capsize=capsize,
                       label='TypeDB',
                       color=typedbcolor,
                       edgecolor=bar_edgecolor)

        # Add some text for labels, title and custom x-axis tick labels, etc.
        ax.set_ylabel('Time (ms)', fontsize=MEDIUM_SIZE)
        ax.set_title(f'Time Taken to Execute Agents during Iteration {iteration}')
        ax.set_xticks(label_locations)
        ax.set_xticklabels(strip_labels(agents), rotation=45, ha='right')
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
