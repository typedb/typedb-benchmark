load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "neo4j",
    srcs = glob([
        "*.java",
        "action/*.java",
        "action/read/*.java",
        "action/write/*.java",
        "action/insight/*.java",
        "driver/*.java",
        "yaml_tool/*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//common:simulation-common",
        "//common/action",
        "//common/driver",
        "//common/utils",
        "//common/world",
        "//common/yaml_tool",
        "//config",
        "@graknlabs_grabl_tracing//client",
        "@graknlabs_common//:common",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_slf4j_slf4j_api",
        "@neo4j//:org_neo4j_driver_neo4j_java_driver",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = [":neo4j"],
    license_type = "agpl",
    size = "small",
)