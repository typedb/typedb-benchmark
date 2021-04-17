/*
 * Copyright (C) 2021 Grakn Labs
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

package grakn.benchmark.common.params;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Command(name = "benchmark", mixinStandardHelpOptions = true)
public class Options {

    @Option(names = {"--database"}, required = true, converter = DatabaseConverter.class,
            description = "The database to run this benchmark against")
    private DatabaseType database;

    @Nullable
    @Option(names = {"--address"},
            description = "Database address URI")
    private String address;

    @Option(names = {"--config"}, required = true,
            description = "Simulation configuration file")
    private File config;

    @Nullable
    @ArgGroup(exclusive = false, multiplicity = "0..1",
            heading = "Grabl tracing options to run this benchmark with")
    private GrablTracing tracing;

    public static Optional<Options> parseCLIOptions(String[] args) {
        return parseCLIOptions(args, new Options());
    }

    public static <T> Optional<T> parseCLIOptions(String[] args, T options) {
        CommandLine commandLine = new CommandLine(options);
        try {
            CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
            if (commandLine.isUsageHelpRequested()) {
                commandLine.usage(commandLine.getOut());
                return Optional.empty();
            } else if (commandLine.isVersionHelpRequested()) {
                commandLine.printVersionHelp(commandLine.getOut());
                return Optional.empty();
            } else {
                return Optional.of(parseResult.asCommandLineList().get(0).getCommand());
            }
        } catch (CommandLine.ParameterException ex) {
            commandLine.getErr().println(ex.getMessage());
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, commandLine.getErr())) {
                ex.getCommandLine().usage(commandLine.getErr());
            }
            return Optional.empty();
        }
    }

    public DatabaseType database() {
        return database;
    }

    public String address() {
        if (address != null) return address;
        else return database.defaultAddress();
    }

    public File config() {
        return config;
    }

    public Optional<GrablTracing> tracing() {
        return Optional.ofNullable(tracing);
    }

    public static class DatabaseConverter implements CommandLine.ITypeConverter<DatabaseType> {

        @Override
        public DatabaseType convert(String value) {
            DatabaseType databaseType;
            if ((databaseType = DatabaseType.of(value)) == null) {
                String expected = Arrays.stream(DatabaseType.values()).map(DatabaseType::key).collect(Collectors.joining(", "));
                throw new IllegalArgumentException(String.format("Unexpected database type: '%s'. Expected database types are: %s", value, expected));
            } else {
                return databaseType;
            }
        }
    }

    public static class GrablTracing {

        @Option(names = {"--grabl"}, required = true, description = "Grabl tracing server address")
        private String grabl;

        @Option(names = {"--org"}, required = true, description = "GitHub organisation name")
        private String org;

        @Option(names = {"--repo"}, required = true, description = "GitHub repository name")
        private String repo;

        @Option(names = {"--commit"}, required = true, description = "Git commit SHA")
        private String commit;

        @ArgGroup(exclusive = false, multiplicity = "0..1", heading = "Authentication credentials for Grabl tracing server")
        private Credentials credentials;

        public static class Credentials {

            @Option(names = {"--username"}, required = true, description = "Grabl tracing username")
            private String username;

            @Option(names = {"--token"}, required = true, description = "Grabl tracing API token")
            private String token;

            public String username() {
                return username;
            }

            public String token() {
                return token;
            }
        }

        public String grabl() {
            return grabl;
        }

        public String org() {
            return org;
        }

        public String repo() {
            return repo;
        }

        public String commit() {
            return commit;
        }

        public Optional<Credentials> credentials() {
            return Optional.ofNullable(credentials);
        }
    }
}
