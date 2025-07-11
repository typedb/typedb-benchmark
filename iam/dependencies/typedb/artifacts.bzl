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

load("@typedb_dependencies//distribution/artifact:rules.bzl", "native_artifact_files")
load("@typedb_dependencies//distribution:deployment.bzl", "deployment", "deployment_private")

def typedb_artifacts():
    native_artifact_files(
        name = "typedb_artifact",
        group_name = "typedb-all-{platform}",
        artifact_name = "typedb-all-{platform}-{version}.{ext}",
        tag_source = deployment["artifact"]["release"]["download"],
        commit_source = deployment["artifact"]["snapshot"]["download"],
        tag = "2.29.1",
    )

def typedb_cluster_artifacts():
    native_artifact_files(
        name = "typedb_cluster_artifact",
        group_name = "typedb-cloud-all-{platform}",
        artifact_name = "typedb-cloud-all-{platform}-{version}.{ext}",
        tag_source = deployment_private["artifact"]["release"]["download"],
        commit_source = deployment_private["artifact"]["snapshot"]["download"],
        tag = "2.29.1",
    )
