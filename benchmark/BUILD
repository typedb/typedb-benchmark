exports_files([
    "templates/agent_section.tex",
    "templates/agent_queries.tex",
    "templates/introduction.tex",
    "templates/agent_intro.tex",
    ])

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
