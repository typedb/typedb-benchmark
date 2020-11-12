java_library(
    name = "simulation-lib",
    srcs = [
        "SimulationRunner.java",
    ],
    resource_strip_prefix = "config/",
    resources = [
        "//config:logback.xml",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//config",
        "//common:simulation-common",
        "//common/world",
        "//grakn",
        "//neo4j",
        "@graknlabs_grabl_tracing//client",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:commons_cli_commons_cli",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_binary(
    name = "simulation-big",
    args = [
        "config/config_big.yml",
        "$(locations //common/data)",
        "$(locations //grakn/data)",
        "$(locations //neo4j/data)",
    ],
    data = [
        "//config:config_big.yml",
        "//common/data",
        "//grakn/data",
        "//neo4j/data",
    ],
    main_class = "grakn.simulation.SimulationRunner",
    runtime_deps = [":simulation-lib"],
)

java_binary(
    name = "simulation-small",
    args = [
        "config/config_small.yml",
        "$(locations //common/data)",
        "$(locations //grakn/data)",
        "$(locations //neo4j/data)",
    ],
    data = [
        "//config:config_small.yml",
        "//common/data",
        "//grakn/data",
        "//neo4j/data",
    ],
    main_class = "grakn.simulation.SimulationRunner",
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
