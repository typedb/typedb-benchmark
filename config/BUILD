load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

exports_files([
    "config_big.yml",
    "config_small.yml",
    "logback.xml",
])

java_library(
    name = "config",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = ["@maven//:org_yaml_snakeyaml"],
)

checkstyle_test(
    name = "checkstyle",
    targets = [":config"],
    license_type = "agpl"
)
