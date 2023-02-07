/*
 * Copyright (C) 2022 Vaticle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.vaticle.typedb.benchmark.common.params

import picocli.CommandLine
import picocli.CommandLine.ArgGroup
import picocli.CommandLine.ITypeConverter
import picocli.CommandLine.ParameterException
import java.io.File

@CommandLine.Command(name = "benchmark", mixinStandardHelpOptions = true)
class Options {
    @CommandLine.Option(
        names = ["--database"],
        required = true,
        converter = [DatabaseConverter::class],
        description = ["The database to run this benchmark against"]
    )
    lateinit var database: DatabaseType; private set

    @CommandLine.Option(names = ["--address"], description = ["Database address URI"])
    private var _address: String? = null

    @CommandLine.Option(names = ["--config"], required = true, description = ["Simulation configuration file"])
    lateinit var configFile: File; private set

    @ArgGroup(
        exclusive = false,
        multiplicity = "0..1",
        heading = "Vaticle Factory tracing options to run this benchmark with"
    )
    val tracing: FactoryTracing? = null

    val address get(): String {
        return _address ?: database.defaultAddress
    }

    class DatabaseConverter : ITypeConverter<DatabaseType> {
        override fun convert(value: String) = DatabaseType.of(value)
    }

    class FactoryTracing {
        @CommandLine.Option(
            names = ["--factory"],
            required = true,
            description = ["Vaticle Factory tracing server address"]
        )
        lateinit var factoryURI: String

        @CommandLine.Option(names = ["--org"], required = true, description = ["GitHub organisation name"])
        lateinit var org: String

        @CommandLine.Option(names = ["--repo"], required = true, description = ["GitHub repository name"])
        lateinit var repo: String

        @CommandLine.Option(names = ["--commit"], required = true, description = ["Git commit SHA"])
        lateinit var commit: String

        @CommandLine.Option(
            names = ["--tags"],
            description = ["Tags for the analysis"],
            converter = [TagConverter::class]
        )
        val tags: List<String>? = null

        @ArgGroup(
            exclusive = false,
            multiplicity = "0..1",
            heading = "Authentication credentials for Vaticle Factory tracing server"
        )
        val credentials: Credentials? = null

        class TagConverter : ITypeConverter<List<String>> {
            override fun convert(value: String): List<String> {
                require(value.isNotBlank()) { "Tags cannot be a blank string" }
                return value.split(Regex("\\s*,\\s*"))
            }
        }

        class Credentials {
            @CommandLine.Option(
                names = ["--username"],
                required = true,
                description = ["Vaticle Factory tracing username"]
            )
            lateinit var username: String

            @CommandLine.Option(
                names = ["--token"],
                required = true,
                description = ["Vaticle Factory tracing API token"]
            )
            lateinit var token: String
        }
    }

    companion object {
        fun parseCLIOptions(args: Array<String>): Options? {
            return parseCLIOptions(args, Options())
        }

        fun <T : Any> parseCLIOptions(args: Array<String>, options: T): T? {
            val commandLine = CommandLine(options)
            return try {
                val parseResult = commandLine.parseArgs(*args)
                if (commandLine.isUsageHelpRequested) {
                    commandLine.usage(commandLine.out)
                    null
                } else if (commandLine.isVersionHelpRequested) {
                    commandLine.printVersionHelp(commandLine.out)
                    null
                } else {
                    parseResult.asCommandLineList()[0].getCommand<T>()
                }
            } catch (ex: ParameterException) {
                commandLine.err.println(ex.message)
                if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, commandLine.err)) {
                    ex.commandLine.usage(commandLine.err)
                }
                null
            }
        }
    }
}
