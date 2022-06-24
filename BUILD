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

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "benchmark-lib",
    srcs = glob(["*.java"]),
    resource_strip_prefix = "config/",
    visibility = ["//visibility:public"],
    deps = [
        "//common:common",
        "//simulation:simulation",
        "//typedb:typedb",
        "//neo4j:neo4j",
        "@vaticle_factory_tracing//client",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "benchmark",
    runtime_deps = [":benchmark-lib"],
    main_class = "com.vaticle.typedb.benchmark.Benchmark",
    classpath_resources = ["//config:logback.xml"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", ".grabl/*"]),
    exclude = glob(["docs/*"]),
    license_type = "agpl",
)
