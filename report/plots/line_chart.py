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

from matplotlib import pyplot as plt


def line_chart(agent_name, grakn_overviews, neo4j_overviews, grakn_color, neo4j_color, capsize, image_extension):

    iterations = list(int(iteration) for iteration in grakn_overviews.get(agent_name)['average'].keys())

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
    plt.savefig(f'agent_{agent_name}.{image_extension}')


def unwrap_overviews_for_lines(overview_name, overviews, key):
    return overviews.get(overview_name)[key].values()
