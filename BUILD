java_library(
    name = "simulation-lib",
    srcs = glob(["*.java"]),
    resource_strip_prefix = "conf/",
    resources = [
        "conf/logback.xml",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//agents",
        "//common",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",
        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//yaml_tool",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grabl_tracing//client",
        "@graknlabs_graql//java:graql",
    ],
)

java_binary(
    name = "simulation",
    args = [
        "$(locations //data)",
        "$(locations //schema)",
    ],
    data = [
        "//data",
        "//schema",
    ],
    main_class = "grakn.simulation.Simulation",
    runtime_deps = [":simulation-lib"],
)

java_binary(
    name = "simulation-debug",
    jvm_flags = [
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,address=5005",
    ],
    main_class = "grakn.simulation.Simulation",
    runtime_deps = [":simulation-lib"],
)
