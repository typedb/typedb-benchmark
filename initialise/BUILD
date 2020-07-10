java_library(
    name = "initialise",
    srcs = glob(["*.java"]),
    deps = [
        "//world",
        "//yaml_tool",
        "//driver",
        "@graknlabs_graql//java:graql",
        "@graknlabs_client_java//:client-java",
        "@neo4j//:org_neo4j_driver_neo4j_java_driver",
        "@graknlabs_grabl_tracing//client",
    ],
    visibility = ["//visibility:public"],
)
