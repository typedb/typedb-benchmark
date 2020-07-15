java_library(
    name = "agents",
    srcs = glob([
        "base/*.java",
        "interaction/*.java",
        "utils/*.java",
        "world/*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//utils",
        "//config",
        "//db/common/driver",
        "//db/common/world",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_apache_commons_commons_lang3",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grabl_tracing//client",
    ],
)
