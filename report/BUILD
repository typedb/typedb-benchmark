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

load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

exports_files([
    "templates/agent_section.tex",
    "templates/agent_queries.tex",
    "templates/introduction.tex",
    "templates/agent_intro.tex",
    ])

load("@io_bazel_rules_groovy//groovy:groovy.bzl", "groovy_and_java_library")
load("@io_bazel_rules_groovy//groovy:groovy.bzl", "groovy_binary")

groovy_and_java_library(
    name = "reportlib",
    srcs = glob([
        "*.groovy",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//grakn:grakn",
        "//neo4j:neo4j",
        "//simulation:simulation",
    ],
    data = [
        "templates/agent_section.tex",
        "templates/agent_queries.tex",
        "templates/introduction.tex",
        "templates/agent_intro.tex",
    ]
)

groovy_binary(
    name = "report",
    srcs = glob(["*.groovy"]),
    main_class = "grakn.benchmark.benchmark.Report",
    deps = [":reportlib"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", "plots/*.py"]),
    exclude = glob(["templates/*", "plots/requirements.txt"]),
    license_type = "agpl"
)