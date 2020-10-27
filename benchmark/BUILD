exports_files([
    "Report.groovy",
    "agent_descriptions.groovy",
    "templates/agent_section.tex",
    "templates/agent_queries.tex",
    "templates/introduction.tex",
    "templates/agent_intro.tex",
    ])

java_library(
    name = "benchmark-lib",
    srcs = glob([
        "*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//db/grakn/schema",
        "//db/grakn",
        "//db/neo4j",
        "//db/common:simulation-common",
        "//db/common/driver",
        "//db/common/agent",
        "//db/common/action",
        "//db/common/utils",
        "//db/common/world",
        "//db/common/yaml_tool",
        "//config:config",
        "//utils",
        "@maven//:org_apache_commons_commons_csv",
        "@maven//:com_google_guava_guava",
        "@maven//:org_slf4j_slf4j_api",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grabl_tracing//client",
        "@graknlabs_graql//java:graql",
    ],
    data = [
        "Report.groovy",
        "agent_descriptions.groovy",
        "templates/agent_section.tex",
        "templates/agent_queries.tex",
        "templates/introduction.tex",
        "templates/agent_intro.tex",
    ]
)

java_binary(
    name = "benchmark",
    main_class = "grakn.simulation.benchmark.ReportJava",
    runtime_deps = [":benchmark-lib"],
)

load("@io_bazel_rules_groovy//groovy:groovy.bzl", "groovy_and_java_library")
load("@io_bazel_rules_groovy//groovy:groovy.bzl", "groovy_binary")

groovy_and_java_library(
    name = "benchmark-groovylib",
    srcs = glob([
        "*.groovy",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//db/grakn",
        "//db/neo4j",
        "//db/common/agent",
    ],
    data = [
        "templates/agent_section.tex",
        "templates/agent_queries.tex",
        "templates/introduction.tex",
        "templates/agent_intro.tex",
    ]
)

groovy_binary(
    name = "benchmark-groovy",
    srcs = glob(["*.groovy"]),
    main_class = "grakn.simulation.benchmark.Report",
    deps = [":benchmark-groovylib"],
)
