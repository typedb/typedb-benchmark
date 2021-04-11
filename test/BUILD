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
load("@graknlabs_bazel_distribution//artifact:rules.bzl", "artifact_extractor")

artifact_extractor(
    name = "grakn-core-extractor-linux",
    artifact = "@graknlabs_grakn_core_artifact_linux//file",
)

artifact_extractor(
    name = "grakn-cluster-extractor-linux",
    artifact = "@graknlabs_grakn_cluster_artifact_linux//file",
)

java_test(
    name = "comparison-test",
    srcs = [
        "ComparisonTest.java",
        "ComparisonTestRunner.java",
        "ComparisonTestSuite.java",
        "BenchmarksForComparison.java",
    ],
    test_class = "grakn.benchmark.test.ComparisonTest",
    deps = [
        "//simulation/agent",
        "//simulation/common",
        "//config",
        "//grakn",
        "//neo4j",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:commons_cli_commons_cli",
        "@maven//:javax_annotation_javax_annotation_api",
        "@maven//:javax_annotation_javax_annotation_api_1_3_2",
        "@maven//:junit_junit",
        "@maven//:org_slf4j_slf4j_api",
    ],
    size = "enormous",
    resource_strip_prefix = "config/",
    resources = [
        "//config:logback.xml",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "agpl"
)
