load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "utils",
    srcs = glob([
        "*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "@graknlabs_grabl_tracing//client",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = [":utils"],
    license_type = "agpl",
    size = "small",
)
