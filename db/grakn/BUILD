java_library(
    name = "grakn",
    srcs = glob([
        "agents/interaction/*.java",
        "initialise/*.java",
        "yaml_tool/*.java",
        "driver/*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//db/grakn/schema",
        "//db/common/agents",
        "//db/common/driver",
        "//db/common/initialise",
        "//db/common/world",
        "//db/common/yaml_tool",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grabl_tracing//client",
        "@graknlabs_graql//java:graql",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:com_google_guava_guava",
    ],
)
