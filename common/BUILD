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
    name = "common",
    srcs = glob(["*.java", "*/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@vaticle_typedb_common//:common",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:info_picocli_picocli",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_yaml_snakeyaml",
    ],
    data = [
        "//data:data",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", "*/*"]),
    license_type = "agpl"
)
