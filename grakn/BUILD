java_library(
    name = "grakn",
    srcs = glob([
        "agents/*.java",
        "initialise/*.java",
        "yaml_tool/*.java",
        "driver/*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//agents",
        "//common",
        "//driver",
        "//yaml_tool",
        "//initialise",
        "//world",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grabl_tracing//client",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_apache_commons_commons_csv",
    ],
)
