java_library(
    name = "simulation-common",
    srcs = glob([
        "*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//db/common/context",
        "//db/common/agents",
        "//db/common/world",
        "//db/common/initialise",
        "//config",
        "//utils",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_slf4j_slf4j_api",
    ],
)