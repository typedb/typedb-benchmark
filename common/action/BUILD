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
        "@maven//:com_google_guava_guava",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
