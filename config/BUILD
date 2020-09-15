exports_files([
    "config_big.yml",
    "config_small.yml",
])

java_library(
    name = "config",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = ["@maven//:org_yaml_snakeyaml"],
)
