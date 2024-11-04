package typeql;

import com.vaticle.typedb.driver.api.*;
import com.vaticle.typedb.driver.api.answer.ConceptMap;
import com.vaticle.typedb.driver.TypeDB;
import com.vaticle.typedb.driver.api.answer.JSON;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;


public class TypeDBConnectionTest {
    private static final String DB_NAME = "sample_app_db";
    private static final String SERVER_ADDR = "127.0.0.1:1729";

    private static final String CLOUD_USERNAME = "admin";
    private static final String CLOUD_PASSWORD = "password";

    public static void main(String[] args) {
        try (TypeDBDriver driver = TypeDB.coreDriver(SERVER_ADDR)) {
            if (driver.databases().contains(DB_NAME)) {
                System.out.println("Found existing DB... replacing it");
                driver.databases().get(DB_NAME).delete();
            }
            driver.databases().create(DB_NAME);
            try (TypeDBSession session = driver.session(DB_NAME, TypeDBSession.Type.SCHEMA)) {
                System.out.println("Loading schema");
                String schemaFile = "typeql/src/test/java/typeql/test-schema.tql";
                try (TypeDBTransaction tx = session.transaction(TypeDBTransaction.Type.WRITE)) {
                    String defineQuery = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir"), schemaFile)));
                    System.out.print("Running schema query...");
                    tx.query().define(defineQuery).resolve();
                    tx.commit();
                    System.out.println("OK");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read the schema file.", e);
                }
            }
            try (TypeDBSession session = driver.session(DB_NAME, TypeDBSession.Type.DATA)) {
                System.out.println("Loading data");
                String dataFile = "typeql/src/test/java/typeql/test-insert.tql";
                try (TypeDBTransaction tx = session.transaction(TypeDBTransaction.Type.WRITE)) {
                    String insertQuery = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir"), dataFile)));
                    System.out.print("Running data query...");
                    tx.query().insert(insertQuery);
                    tx.commit();
                    System.out.println("OK");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read the data file.", e);
                }
            }
            queries(driver, DB_NAME);
        } catch (TypeDBDriverException e) {
            e.printStackTrace();
        }
    }

    private static void queries(TypeDBDriver driver, String dbName) {
        System.out.println("Request 1 of 6: Fetch all users as JSON objects with full names and emails");
        List<JSON> users = fetchAllUsers(driver, dbName);

        String name = "Jack Keeper";
        String email = "jk@typedb.com";
        String secondRequestMessage = String.format("Request 2 of 6: Request 2 of 6: Add a new user with the full-name \"%s\" and email \"%s\"", name, email);
        System.out.println(secondRequestMessage);
        List<ConceptMap> newUser = insertNewUser(driver, dbName, name, email);

        String nameKevin = "Kevin Morrison";
        String thirdRequestMessage = String.format("Request 3 of 6: Find all files that the user \"%s\" has access to view (no inference)", nameKevin);
        System.out.println(thirdRequestMessage);
        List<ConceptMap> no_files = getFilesByUser(driver, dbName, nameKevin, false);

        String fourthRequestMessage = String.format("Request 4 of 6: Find all files that the user \"%s\" has access to view (with inference)", nameKevin);
        System.out.println(fourthRequestMessage);
        List<ConceptMap> files = getFilesByUser(driver, dbName, nameKevin, true);

        String old_path = "lzfkn.java";
        String new_path = "lzfkn2.java";
        String fifthRequestMessage = String.format("Request 5 of 6: Update the path of a file from \"%s\" to \"%s\"", old_path, new_path);
        System.out.println(fifthRequestMessage);
        List<ConceptMap> updated_files = updateFilePath(driver, dbName, old_path, new_path);

        String sixthRequestMessage = String.format("Request 6 of 6: Delete the file with path \"%s\"", new_path);
        System.out.println(sixthRequestMessage);
        boolean deleted = deleteFile(driver, dbName, new_path);
    }

    private static List<JSON> fetchAllUsers(TypeDBDriver driver, String dbName) {
        try (TypeDBSession session = driver.session(dbName, TypeDBSession.Type.DATA)) {
            try (TypeDBTransaction tx = session.transaction(TypeDBTransaction.Type.READ)) {
                String query = "match $u isa user; fetch $u: full-name, email;";
                List<JSON> answers = tx.query().fetch(query).collect(Collectors.toList());
                answers.forEach(json -> System.out.println("JSON: " + json.toString()));
                return answers;
            }
        }
    }

    public static List<ConceptMap> insertNewUser(TypeDBDriver driver, String dbName, String name, String email) {
        try (TypeDBSession session = driver.session(dbName, TypeDBSession.Type.DATA)) {
            try (TypeDBTransaction tx = session.transaction(TypeDBTransaction.Type.WRITE)) {
                String query = String.format(
                        "insert $p isa person, has full-name $fn, has email $e; $fn \"%s\"; $e \"%s\";", name, email);
                List<ConceptMap> response = tx.query().insert(query).collect(Collectors.toList());
                tx.commit();
                for (ConceptMap conceptMap : response) {
                    String fullName = conceptMap.get("fn").asAttribute().getValue().asString();
                    String emailAddress = conceptMap.get("e").asAttribute().getValue().asString();
                    System.out.println("Added new user. Name: " + fullName + ", E-mail: " + emailAddress);
                }
                return response;
            }
        }
    }

    public static List<ConceptMap> getFilesByUser(TypeDBDriver driver, String dbName, String name, boolean inference) {
        List<ConceptMap> filePaths = new ArrayList<>();
        TypeDBOptions options = new TypeDBOptions().infer(inference);
        try (TypeDBSession session = driver.session(dbName, TypeDBSession.Type.DATA);
             TypeDBTransaction tx = session.transaction(TypeDBTransaction.Type.READ, options)) {

            String userQuery = String.format("match $u isa user, has full-name '%s'; get;", name);
            List<ConceptMap> users = tx.query().get(userQuery).collect(Collectors.toList());

            if (users.size() > 1) {
                System.out.println("Error: Found more than one user with that name.");
                return null;
            } else if (users.size() == 1) {
                String fileQuery = String.format("""
                                                match
                                                $fn '%s';
                                                $u isa user, has full-name $fn;
                                                $p($u, $pa) isa permission;
                                                $o isa object, has path $fp;
                                                $pa($o, $va) isa access;
                                                $va isa action, has name 'view_file';
                                                get $fp;""", name);
                tx.query().get(fileQuery).forEach(filePaths::add);
                filePaths.forEach(path -> System.out.println("File: " + path.get("fp").asAttribute().getValue().toString()));
                if (filePaths.isEmpty()) {
                    System.out.println("No files found. Try enabling inference.");
                }
                return filePaths;
            } else {
                System.out.println("Warning: No users found with that name.");
                return null;
            }
        } catch (TypeDBDriverException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ConceptMap> updateFilePath(TypeDBDriver driver, String dbName, String oldPath, String newPath) {
        List<ConceptMap> response = new ArrayList<>();
        try (TypeDBSession session = driver.session(dbName, TypeDBSession.Type.DATA);
             TypeDBTransaction tx = session.transaction(TypeDBTransaction.Type.WRITE)) {
            String query = String.format("""
                                        match
                                        $f isa file, has path $old_path;
                                        $old_path = '%s';
                                        delete
                                        $f has $old_path;
                                        insert
                                        $f has path '%s';""", oldPath, newPath);
            response = tx.query().update(query).collect(Collectors.toList());
            if (!response.isEmpty()) {
                tx.commit();
                System.out.println(String.format("Total number of paths updated: %s", response.size()));
                return response;
            } else {
                System.out.println("No matched paths: nothing to update");
            }

        } catch (TypeDBDriverException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static boolean deleteFile(TypeDBDriver driver, String dbName, String path) {
        try (TypeDBSession session = driver.session(dbName, TypeDBSession.Type.DATA);
             TypeDBTransaction tx = session.transaction(TypeDBTransaction.Type.WRITE)) {

            String query = String.format("match $f isa file, has path '%s'; get;", path);
            List<ConceptMap> response = tx.query().get(query).collect(Collectors.toList());

            if (response.size() == 1) {
                tx.query().delete(String.format("match $f isa file, has path '%s'; delete $f isa file;", path)).resolve();
                tx.commit();
                System.out.println("The file has been deleted.");
                return true;
            } else if (response.size() > 1) {
                System.out.println("Matched more than one file with the same path. No files were deleted.");
                return false;
            } else {
                System.out.println("No files matched in the database. No files were deleted.");
                return false;
            }
        } catch (TypeDBDriverException e) {
            e.printStackTrace();
            return false;
        }
    }
}