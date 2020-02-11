java_library(
    name = "agents",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//common",
        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grabl_tracing//client:grabl-tracing",
    ],
)
