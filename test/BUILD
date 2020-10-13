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

load("@graknlabs_dependencies//distribution/artifact:rules.bzl", "artifact_extractor")

artifact_extractor(
    name = "grakn-extractor",
    artifact = "@graknlabs_grakn_core_artifact//file",
)

java_test(
    name = "simulation-test",
    srcs = [
        "SimulationTest.java",
        "SimulationTestSuite.java",
        "SimulationRunner.java",
        "SimulationsUnderTest.java",
    ],
    data = [
        "//db/common/data",
        "//db/grakn/data",
        "//db/grakn/schema:graql-schema",
        "//db/neo4j/data",
    ],
    test_class = "grakn.simulation.test.SimulationTest",
    deps = [
        "//:simulation-lib",
        "//config",
        "//db/common/agent",
        "//db/common/action",
        "//db/common/world",
        "//db/grakn",
        "//db/neo4j",
        "//utils",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:junit_junit",
        "@maven//:javax_annotation_javax_annotation_api",
        "@maven//:javax_annotation_javax_annotation_api_1_3_2",
    ],
)
