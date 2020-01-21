java_library(
    name = "yaml_tool",
    srcs = glob(["*.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "//dependencies/maven/artifacts/org/yaml:snakeyaml",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
        "//dependencies/maven/artifacts/org/apache/commons:commons-lang3",
        "//dependencies/maven/artifacts/args4j:args4j",
    ],
    visibility = ["//visibility:public"],
)