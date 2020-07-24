exports_files([
    "config.yaml",
])

java_library(
    name = "config",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = ["@maven//:org_yaml_snakeyaml"],
)
