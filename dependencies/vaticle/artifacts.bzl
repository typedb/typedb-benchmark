#
# Copyright (C) 2021 Vaticle
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

load("@vaticle_dependencies//distribution/artifact:rules.bzl", "native_artifact_files")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment", "deployment_private")

def vaticle_typedb_artifacts():
    native_artifact_files(
        name = "vaticle_typedb_artifact",
        group_name = "vaticle_typedb",
        artifact_name = "typedb-server-{platform}-{version}.{ext}",
        tag_source = deployment["artifact.release"],
        commit_source = deployment["artifact.snapshot"],
        commit = "77a4d559de9886c8919d12d946e14ef335fc7413",
    )

def graknlabs_grakn_cluster_artifacts():
    native_artifact_files(
        name = "graknlabs_grakn_cluster_artifact",
        group_name = "graknlabs_grakn_cluster",
        artifact_name = "grakn-cluster-all-{platform}-{version}.{ext}",
        tag_source = deployment_private["artifact.release"],
        commit_source = deployment_private["artifact.snapshot"],
        commit = "3a94c636a49b302433e0c1f20c437550c85523a8",
    )
