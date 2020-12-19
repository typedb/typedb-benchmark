load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "action",
    srcs = glob([
        "*.java",
        "read/*.java",
        "write/*.java",
        "insight/*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//common/driver",
        "//common/utils",
        "//common/world",
        "@graknlabs_common//:common",
        "@maven//:com_google_guava_guava",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = [":action"],
    license_type = "agpl",
    size = "small",
)
