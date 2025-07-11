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
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

kt_jvm_library(
    name = "typedb",
    srcs = glob(["*.kt", "*/*.kt"]),
    visibility = ["//visibility:public"],
    deps = [
        "//simulation/agent",
        "//simulation/common",

        # External Vaticle Dependencies
        "@typedb_maven_deps//:com_vaticle_typedb_typedb_common",
        "@typedb_maven_deps//:com_vaticle_typedb_typedb_driver",
        "@typedb_maven_deps//:com_vaticle_typeql_typeql_builder",
        "@typedb_maven_deps//:com_vaticle_typeql_typeql_common",
        "@typedb_maven_deps//:com_vaticle_typeql_typeql_lang",
        "@typedb_maven_deps//:com_vaticle_typeql_typeql_pattern",
        "@typedb_maven_deps//:com_vaticle_typeql_typeql_query",

        "@typedb_simulation//framework:typedb-simulation-framework",
        "@typedb_simulation//framework/typedb:typedb",

        # External Maven Dependencies
        "@maven//:io_github_microutils_kotlin_logging_jvm",
        "@maven//:org_slf4j_slf4j_api",
    ],
    data = ["//:iam-schema.tql"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", "*/*", "*/*/*"]),
    license_type = "apache-header",
)
