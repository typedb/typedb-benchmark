load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "yaml_tool",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@graknlabs_graql//java:graql",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_yaml_snakeyaml",
    ],
)

checkstyle_test(
    name = "checkstyle",
    targets = [":yaml_tool"],
    license_type = "agpl"
)
