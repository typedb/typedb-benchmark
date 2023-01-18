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

load("@io_bazel_rules_kotlin//kotlin:core.bzl", "define_kt_toolchain")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

kt_jvm_library(
    name = "benchmark-lib",
    srcs = glob(["*.kt"]),
    resource_strip_prefix = "config/",
    visibility = ["//visibility:public"],
    deps = [
        "//common",
        "//simulation",
        "//typedb",
        "//neo4j",

        # External Vaticle Dependencies
        "@vaticle_factory_tracing//client",

        # External Maven Dependencies
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:io_github_microutils_kotlin_logging_jvm",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "benchmark",
    runtime_deps = [":benchmark-lib"],
    main_class = "com.vaticle.typedb.benchmark.BenchmarkKt",
    classpath_resources = ["//config:logback.xml"],
)

define_kt_toolchain(
    name = "kotlin_toolchain_strict_deps",
    api_version = "1.7",
    language_version = "1.7",
    experimental_strict_kotlin_deps = "error",
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", ".factory/*"]),
    exclude = glob([
        ".bazelversion",
        "LICENSE",
        "README.md",
        "docs/*",
    ]),
    license_type = "agpl-header",
)

checkstyle_test(
    name = "checkstyle-license",
    include = ["LICENSE"],
    license_type = "agpl-fulltext",
)
