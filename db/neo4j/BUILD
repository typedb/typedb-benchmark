java_library(
    name = "neo4j",
    srcs = glob([
        "agents/interaction/*.java",
        "initialise/*.java",
        "yaml_tool/*.java",
        "driver/*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//db/common/agents",
        "//db/common/driver",
        "//db/common/initialise",
        "//db/common/world",
        "//db/common/yaml_tool",
        "@graknlabs_grabl_tracing//client",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_apache_commons_commons_lang3",
        "@neo4j//:org_neo4j_driver_neo4j_java_driver",
        "@maven//:com_google_guava_guava",
    ],
)
