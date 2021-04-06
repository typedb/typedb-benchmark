load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "yaml_tool",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@graknlabs_graql//java:graql",
        "@graknlabs_graql//java/common",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_yaml_snakeyaml",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = [":yaml_tool"],
    license_type = "agpl",
    size = "small",
)
