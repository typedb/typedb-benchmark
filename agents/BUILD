java_library(
    name = "agents",
    srcs = glob([
        "*.java",
        "base/*.java",
        "common/*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//common",
        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/org/apache/commons:commons-lang3",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grabl_tracing//client",
    ],
)
