java_library(
    name = "csv_loader",
    srcs = glob(["**/*.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "//dependencies/maven/artifacts/org/apache/commons:commons-csv",
        "//dependencies/maven/artifacts/args4j:args4j",
    ],
    visibility = ["//visibility:public"],
)

java_binary(
    name = "csv_loader-binary",
    main_class = "grakn.simulation.csv_loader.GraknCSVLoaderCLI",
    runtime_deps = [
        ":csv_loader",
    ],
    visibility = ["//visibility:public"],
)