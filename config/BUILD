exports_files([
    "config.yaml",
])

java_library(
    name = "config",
    srcs = glob(["*.java"]),
    deps = [
        "//db/common/agents",
        "//db/grakn",
        "@maven//:org_yaml_snakeyaml",
    ],
    visibility = ["//visibility:public"],
)
