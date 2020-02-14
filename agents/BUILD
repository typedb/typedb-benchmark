java_library(
    name = "agents",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//common",
        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
    ],
)
