load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "world",
    srcs = glob([
        "*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:org_apache_commons_commons_csv",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = [":world"],
    license_type = "agpl",
    size = "small",
)
