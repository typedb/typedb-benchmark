java_library(
    name = "grakn",
    srcs = glob([
        "*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//agents",
        "//common",
        "//world",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@maven//:org_apache_commons_commons_lang3",
    ],
)
