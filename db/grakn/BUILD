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
        "//db/common/agents",
        "//db/common/driver",
        "//db/common/yaml_tool",
        "//db/common/initialise",
        "//db/common/world",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grabl_tracing//client",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_apache_commons_commons_csv",
    ],
)
