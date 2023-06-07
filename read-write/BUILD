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

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

kt_jvm_library(
    name = "benchmark-read-write",
    srcs = glob(["*.kt"]),
    deps = [
        "//read-write/common",

        "//framework:typedb-simulation-framework",
        "//framework/typedb:typedb",
        "//framework/common:common",

        # External Vaticle Dependencies
        "@vaticle_typeql//java:typeql-lang",
        "@vaticle_typeql//java/pattern",
        "@vaticle_typeql//java/query",

        # External Maven Dependencies
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:io_github_microutils_kotlin_logging_jvm",
        "@maven//:org_slf4j_slf4j_api",
    ],
    data = [":schema.tql"]
)

java_binary(
    name = "benchmark-runner",
    data = ["//read-write/config:benchmark-config"],
    runtime_deps = [":benchmark-read-write"],
    main_class = "com.vaticle.typedb.benchmark.readwrite.Runner",
    classpath_resources = ["//read-write/config:logback-config"],
    visibility = ["//visibility:public"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", "*/*"]),
    license_type = "agpl-header",
)
