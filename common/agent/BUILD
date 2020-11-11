java_library(
    name = "agent",
    srcs = glob([
        "base/*.java",
        "*.java",
        "insight/*.java",
        "write/*.java",
        "region/*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//common/action",
        "//common/driver",
        "//common/world",
        "//common/utils",
        "@graknlabs_grabl_tracing//client",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:com_google_guava_guava",
    ],
)
