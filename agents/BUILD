java_library(
    name = "agents",
    srcs = glob(["*.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "//common:common",
        "//dependencies/maven/artifacts/commons-cli:commons-cli",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
    ],
    visibility = ["//visibility:public"],
)