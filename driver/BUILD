exports_files([
    "config.yaml",
])

java_library(
    name = "driver",
    srcs = glob(["*.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@neo4j//:org_neo4j_driver_neo4j_java_driver",
    ],
    visibility = ["//visibility:public"],
)
