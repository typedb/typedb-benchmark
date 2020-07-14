java_library(
    name = "initialise",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//world",
        "//agents",
        "//yaml_tool",
        "//driver",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grabl_tracing//client",
        "@graknlabs_graql//java:graql",
        "@neo4j//:org_neo4j_driver_neo4j_java_driver",
    ],
)
