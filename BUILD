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

java_library(
    name = "benchmark-lib",
    srcs = glob(["*.java"]),
    resource_strip_prefix = "config/",
    visibility = ["//visibility:public"],
    deps = [
        "//common:common",
        "//simulation:simulation",
        "//simulation/common:common",
        "//grakn:grakn",
        "//neo4j:neo4j",
        "@graknlabs_grabl_tracing//client",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "benchmark",
    runtime_deps = [":benchmark-lib"],
    main_class = "grakn.benchmark.Benchmark",
    classpath_resources = ["//config:logback.xml"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", ".grabl/*"]),
    exclude = glob(["docs/*"]),
    license_type = "agpl",
)
