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
    name = "neo4j",
    srcs = glob(["*.java", "*/*.java", "*/*/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//simulation:simulation",
        "//simulation/action",
        "//simulation/common",
        "//simulation/driver",
        "//simulation/loader",
        "//config",
        "@graknlabs_grabl_tracing//client",
        "@graknlabs_common//:common",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_slf4j_slf4j_api",
        "@neo4j//:org_neo4j_driver_neo4j_java_driver",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = [":neo4j"],
    license_type = "agpl",
    size = "small",
)