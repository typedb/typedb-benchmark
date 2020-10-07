java_library(
    name = "simulation-lib",
    srcs = [
        "SimulationRunner.java",
    ],
    resource_strip_prefix = "conf/",
    resources = [
        "conf/logback.xml",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//config",
        "//db/common:simulation-common",
        "//db/common/world",
        "//db/grakn",
#        "//db/neo4j",
        "//utils",
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
        "$(locations //db/common/data)",
        "$(locations //db/grakn/schema:graql-schema)",
        "$(locations //db/grakn/data)",
        "$(locations //db/neo4j/data)",
    ],
    data = [
        "//config:config_big.yml",
        "//db/common/data",
        "//db/grakn/data",
        "//db/grakn/schema:graql-schema",
        "//db/neo4j/data",
    ],
    main_class = "grakn.simulation.SimulationRunner",
    runtime_deps = [":simulation-lib"],
)

java_binary(
    name = "simulation-small",
    args = [
        "config/config_small.yml",
        "$(locations //db/common/data)",
        "$(locations //db/grakn/schema:graql-schema)",
        "$(locations //db/grakn/data)",
        "$(locations //db/neo4j/data)",
    ],
    data = [
        "//config:config_small.yml",
        "//db/common/data",
        "//db/grakn/data",
        "//db/grakn/schema:graql-schema",
        "//db/neo4j/data",
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
