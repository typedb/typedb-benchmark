#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
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
    data = [
        "//common/data",
        "//grakn/data",
        "//neo4j/data",
    ],
    test_class = "grakn.benchmark.test.ComparisonTest",
    deps = [
        "//common/action",
        "//common/agent",
        "//common/world",
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
    include = [":comparison-test"],
    license_type = "agpl",
    size = "small",
)

