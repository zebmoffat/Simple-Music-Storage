package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import databasemanagement.Database;

public class MainTest {

    Database database;    

    @BeforeEach
    void setUp() {
        String url = System.getProperty("arg1", "jdbc:mysql://localhost:3306/sms");
        String username = System.getProperty("arg2", "root");
        String password = System.getProperty("arg2", "root");
        String[] args = {url, username, password}; 
        database = new Database(args);
        database.createAllTables(true);
        database.dropAllTables(true);
    }

    // Test database constructor
    @Test
    void databaseConnection() {
        assertNotNull(database, "Database should be initialized");
        assertNotNull(database.establishConnection(
            System.getProperty("arg1", "jdbc:mysql://localhost:3306/sms"),
            System.getProperty("arg2", "root"),
            System.getProperty("arg3", "root")
        ), "Database connection should not be null");
    }

    // Test whether database can create all tables and insert all values
    @Test
    void tableCreation() {
        assertEquals(true, database.createAllTables(false));
    }

    // Test whether database can drop all tables
    @Test
    void tableDrop() {
        assertEquals(true, database.dropAllTables(false));
    }

    // Test that listed tables are correct
    @Test
    void listTables() {
        assertEquals(new ArrayList<String>(), database.listTables());
        
        database.createAllTables(true);
        
        List<String> list = new ArrayList<>(Arrays.asList("album", "artist", "created", "genre", "made", "producer", "record_label", "song", "works_for"));
        assertEquals(list, database.listTables());
    }


    // Test that database cannot complete queries after it is closed
    @Test
    void closeResources() {
        database.closeResources();
        assertEquals(false, database.createAllTables(false));
        assertEquals(false, database.dropAllTables(false));
    }
}