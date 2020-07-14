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

java_library(
    name = "hello-world-example",
    srcs = [
        "HelloWorldExample.java",
    ],
    resource_strip_prefix = "conf/",
    resources = [
        "conf/logback.xml",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "@neo4j//:org_neo4j_driver_neo4j_java_driver",
    ],
)

java_binary(
    name = "simulation",
    args = [
        "$(locations //db/common/data)",
        "$(locations //db/grakn/schema)",
    ],
    data = [
        "//config:config.yaml",
        "//db/common/data",
        "//db/grakn/schema",
    ],
    main_class = "grakn.simulation.Simulation",
    runtime_deps = [":simulation-lib"],
)

java_binary(
    name = "hello-world",
    main_class = "grakn.simulation.HelloWorldExample",
    runtime_deps = [":hello-world-example"],
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
