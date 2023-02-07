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
    name = "neo4j",
    srcs = glob(["*.kt", "*/*.kt", "*/*/*.kt"]),
    visibility = ["//visibility:public"],
    deps = [
        "//common",
        "//simulation",

        # External Vaticle Dependencies
        "@vaticle_typedb_common//:common",

        # External Maven Dependencies
        "@maven//:io_github_microutils_kotlin_logging_jvm",
        # TODO: @neo4j ???
        "@neo4j//:org_neo4j_driver_neo4j_java_driver",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", "*/*", "*/*/*"]),
    license_type = "agpl-header",
)
