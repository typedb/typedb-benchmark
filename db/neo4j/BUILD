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
        "//config",
        "//db/common/agents",
        "//db/common/driver",
        "//db/common/yaml_tool",
        "//db/common/initialise",
        "//db/common/world",
        "@graknlabs_grabl_tracing//client",
        "@neo4j//:org_neo4j_driver_neo4j_java_driver",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_apache_commons_commons_csv",
    ],
)
