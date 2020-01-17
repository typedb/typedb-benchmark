java_library(
    name = "world",
    srcs = glob(["**/*.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "//framework:framework",
    ],
)

java_binary(
    name = "world-binary",
    main_class = "grakn.simulation.world.Main",
    runtime_deps = [":world"],
)