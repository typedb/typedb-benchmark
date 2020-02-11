java_library(
    name = "simulation-lib",
    srcs = glob(["*.java"]),
    resources = [
        "conf/logback.xml",
    ],
    resource_strip_prefix = "conf/",
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grabl_tracing//protocol:protocol",
        "@graknlabs_grabl_tracing//client:grabl-tracing",
        "//dependencies/maven/artifacts/commons-cli:commons-cli",
        "//common:common",
        "//yaml_tool:yaml_tool",
        "//agents:agents",
    ],
    visibility = ["//visibility:public"],
)

java_binary(
    name = "simulation",
    main_class = "grakn.simulation.Simulation",
    runtime_deps = [":simulation-lib"],
    data = [
        "//data",
        "//schema",
    ],
    args = [
        "$(locations //data)",
        "$(locations //schema)",
    ],
)

java_binary(
    name = "simulation-debug",
    main_class = "grakn.simulation.Simulation",
    runtime_deps = [":simulation-lib"],
    jvm_flags = [
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,address=5005",
    ]
)