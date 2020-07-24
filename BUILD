java_library(
    name = "simulation-lib",
    srcs = [
        "Simulation.java",
    ],
    resource_strip_prefix = "conf/",
    resources = [
        "conf/logback.xml",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//utils",
        "//config",
        "//db/grakn",
        "//db/common/agents",
        "//db/common/driver",
        "//db/common/initialise",
        "//db/common/world",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grabl_tracing//client",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "simulation",
    args = [
        "$(locations //db/common/data)",
        "$(locations //db/grakn/schema)",
        "$(locations //db/grakn/data)",
    ],
    data = [
        "//config:config.yaml",
        "//db/common/data",
        "//db/grakn/schema",
        "//db/grakn/data",
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
