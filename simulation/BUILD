load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "simulation",
    srcs = glob([
        "*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//simulation/agent",
        "//simulation/driver",
        "//simulation/world",
        "//simulation/action",
        "//simulation/utils",
        "//config",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = [":simulation"],
    license_type = "agpl",
    size = "small",
)