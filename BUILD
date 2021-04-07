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
    resources = [
        "//config:logback.xml",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//config",
        "//simulation:simulation",
        "//simulation/common",
        "//grakn",
        "//neo4j",
        "@graknlabs_grabl_tracing//client",
        "@graknlabs_client_java//:client-java",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "benchmark-big",
    args = [
        "config/config_big.yml",
        "$(locations //simulation/data)",
        "$(locations //grakn/data)",
        "$(locations //neo4j/data)",
    ],
    data = [
        "//config:config_big.yml",
        "//simulation/data",
        "//grakn/data",
        "//neo4j/data",
    ],
    main_class = "grakn.benchmark.Benchmark",
    runtime_deps = [":benchmark-lib"],
)

java_binary(
    name = "benchmark-small",
    args = [
        "config/config_small.yml",
        "$(locations //simulation/data)",
        "$(locations //grakn/data)",
        "$(locations //neo4j/data)",
    ],
    data = [
        "//config:config_small.yml",
        "//simulation/data",
        "//grakn/data",
        "//neo4j/data",
    ],
    main_class = "grakn.benchmark.Benchmark",
    runtime_deps = [":benchmark-lib"],
)

java_binary(
    name = "benchmark-debug",
    jvm_flags = [
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,address=5005",
    ],
    main_class = "grakn.benchmark.Benchmark",
    runtime_deps = [":benchmark-lib"],
)

checkstyle_test(
    name = "checkstyle",
    include = [":benchmark-lib"],
    license_type = "agpl",
    size = "small",
)

# CI targets that are not declared in any BUILD file, but are called externally
filegroup(
    name = "ci",
    data = [
        "@graknlabs_dependencies//tool/bazelrun:rbe",
        "@graknlabs_dependencies//distribution/artifact:create-netrc",
        "@graknlabs_dependencies//tool/unuseddeps:unused-deps",
    ],
)
