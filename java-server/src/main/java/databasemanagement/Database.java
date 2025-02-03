package databasemanagement;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Database {
	
	/**user entered arguments*/
	private final String url;
	private final String user;
	private final String password;
	
	/**mySQL variables*/
	private static Connection connection;
	private static ResultSet resultSet;
	private static Statement statement;
	private static PreparedStatement pS;
	
	public Database(String[] args) {
		url = args[0];
		user = args[1];
		password = args[2];

		System.out.println("Connecting to database through url: " + url + "\n");
		
		connection = establishConnection(url, user, password);
	}
	
	//opens a connection with db when class is initialized 
	public Connection establishConnection(String url, String user, String password) {
	    try {
    	    Class.forName("com.mysql.cj.jdbc.Driver");
    	    
    	    connection = DriverManager.getConnection(url, user, password);
	    } catch (ClassNotFoundException e) {
            System.out.println(e);
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    System.out.println("Error closing database resources: " + sqle.getMessage());
                }
                System.exit(-1);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.exit(-1);
        }
	    
	    return connection;
	}
	
	//close resources when app is exited
	public void closeResources() {
	    System.out.println("Closing database resources...");
	    try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
            if (pS != null) {
                pS.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing database resources: " + e.getMessage());
            System.exit(-1);
        }
	}

	//lists all tables names in the database
	public List<String> listTables() {
		String query = "SELECT TABLE_NAME FROM information_schema.tables WHERE table_schema = ?";
		try {
			pS = connection.prepareStatement(query);
			pS.setString(1, connection.getCatalog());
			resultSet = pS.executeQuery();

			List<String> tables = new ArrayList<>();
			while (resultSet.next()) {
				tables.add(resultSet.getString("TABLE_NAME"));
			}
			return tables;
		} catch (SQLException e) {
			System.out.println("SQL Error: " + e.getMessage());
			return new ArrayList<>();
		} finally {
			try {
				if (resultSet != null) resultSet.close();
				if (pS != null) pS.close();
			} catch (SQLException e) {
				System.out.println("Error closing resources: " + e.getMessage());
			}
		}
	}

	//drop all tables in the database
	public boolean dropAllTables(boolean quiet) {
		if (!quiet) System.out.println("Dropping all tables...");
		try {
			// Disable foreign key checks to avoid dependency issues
			try (Statement stmt = connection.createStatement()) {
				stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
			}
	
			// Retrieve all table names
			List<String> tables = listTables();
			for (String table : tables) {
				String dropQuery = "DROP TABLE IF EXISTS " + table;
				try (Statement stmt = connection.createStatement()) {
					stmt.execute(dropQuery);
					if (!quiet) System.out.println("Dropped table: " + table);
				} catch (SQLException e) {
					if (!quiet) System.out.println("Error dropping table " + table + ": " + e.toString());
				}
			}
	
			// Re-enable foreign key checks
			try (Statement stmt = connection.createStatement()) {
				stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
			}
	
			if (!quiet) System.out.println("All tables dropped successfully.");
			return true;
		} catch (SQLException e) {
			if (!quiet) System.out.println("Error during table drop: " + e.toString());
			return false;
		}
	}
	
	//recreate all tables in the database
	public boolean  createAllTables(boolean quiet) {
		dropAllTables(true); // Drop existing tables first
	
		final String CREATEFILE = "./scripts/createTables.txt";
		final String INSERTFILE = "./scripts/fillTables.txt";

		try {
			// Read file content
			StringBuilder sqlBuilder = new StringBuilder();
			try (Scanner fileScanner = new Scanner(new java.io.File(CREATEFILE))) {
				while (fileScanner.hasNextLine()) {
					sqlBuilder.append(fileScanner.nextLine()).append("\n");
				}
			}

			try (Scanner fileScanner = new Scanner(new java.io.File(INSERTFILE))) {
				while (fileScanner.hasNextLine()) {
					sqlBuilder.append(fileScanner.nextLine()).append("\n");
				}
			}
	
			// Split SQL statements by semicolon (;)
			String[] sqlStatements = sqlBuilder.toString().split(";");
	
			try (Statement stmt = connection.createStatement()) {
				for (String sql : sqlStatements) {
					if (!sql.trim().isEmpty()) { // Skip empty lines
						stmt.execute(sql.trim() + ";"); // Add back the semicolon
					}
				}
				if (!quiet) System.out.println("All tables created successfully.");
			}			
			return true;

		} catch (FileNotFoundException e) {
			System.out.println("Could not find " + CREATEFILE + "\n" + e.toString());
			return false;
		} catch (SQLException e) {
			System.out.println("Error creating tables: " + e.getMessage());
			return false;
		}
	}

	//printe a table from the database
	public List<String> printTable(String table) {
		List<String> response = new ArrayList<>();

		try {
	        statement = connection.createStatement();
	        
	        resultSet = statement.executeQuery("SELECT * FROM " + table);
	        
	    	response = printTable(resultSet);

			return response;
	    } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
			System.out.println("Could not print table.");
			response.add(e.toString());
			return response;
		}
	}
	
	//execute user provided sql into the database
	public String executeSql(String command) {
		StringBuilder result = new StringBuilder();
		try {
			statement = connection.createStatement();
			boolean hasResultSet = statement.execute(command); // Handles all SQL types
	
			if (hasResultSet) { 
				resultSet = statement.getResultSet();
				ResultSetMetaData metaData = resultSet.getMetaData();
				int columnCount = metaData.getColumnCount();
	
				// Header
				for (int i = 1; i <= columnCount; i++) {
					result.append(metaData.getColumnName(i)).append("\t");
				}
				result.append("\n");
	
				// Rows
				while (resultSet.next()) {
					for (int i = 1; i <= columnCount; i++) {
						result.append(resultSet.getString(i)).append("\t");
					}
					result.append("\n");
				}
			} else { 
				int updateCount = statement.getUpdateCount(); 
				result.append("Update Count: ").append(updateCount); 
			}
		} catch (SQLException e) {
			return "Error: " + e.getMessage(); 
		}
		return result.toString();
	}
	

	//retrive and create rows from a database table to return to public printTable method
	private List<String> printTable(ResultSet rs) throws SQLException {
	    
		List<String> tableRows = new ArrayList<>();

		String row = "";
		int spaces;

		if (rs == null) {
	        return null;
	    }
	    
	    //get metadata for given set
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int columnCount = rsmd.getColumnCount();
	    
	    //print top of table
		row += "+";

	    for (int i = 1; i <= columnCount; i++) {
	        row += ("-".repeat(25));
	        if (i < columnCount) {
	            row += "|";
	        }
	    }
	    row += "+";
		tableRows.add(row);
	    
	    //print table header
	    row = "|";
	    for (int i = 1; i <= columnCount; i++) {
	        String columnName = rsmd.getColumnName(i);
	        //print centered
			spaces = printSpaces(12 - ((columnName.length() / 2) < 1 ? 1 : columnName.length() / 2));
			row += (" ".repeat(spaces));
			row += columnName;
	        spaces = printSpaces(25 - (spaces + columnName.length()));
			row += (" ".repeat(spaces));
			row += "|";
	    }
		tableRows.add(row);
	    
	  	//print header divider
        row = "|";
        for (int i = 1; i <= columnCount; i++) {
            row += ("-".repeat(25));
            if (i < columnCount) {
                row += '|';
            }
        }
        row += "|";
	    tableRows.add(row);

	    //print row data
	    while (rs.next()) {
	        row = "|";
	        for (int i = 1; i <= columnCount; i++) {
	            String data = rs.getString(i);
	            //print centered
	            spaces = printSpaces(12 - ((data.length() / 2) < 1 ? 1 : data.length() / 2));
	            row += (" ".repeat(spaces));
				row += data;
	            spaces = printSpaces(25 - (spaces + data.length()));
				row += (" ".repeat(spaces));
	            row += "|";
	        }
			tableRows.add(row);
	    }
	    
	    //print bottom of table
        row = "+";
        for (int i = 1; i <= columnCount; i++) {
            row += ("-".repeat(25));
            if (i < columnCount) {
               row += "|";
            }
        }
        row += "+";
		tableRows.add(row);

		return tableRows;
	}
	
	//return an amount of spaces to print from a given integer
	private int printSpaces(int count) {
		String spaces = "";
		for (int i = 0; i < count ; i++) {
			spaces += " ";
		}
		return spaces.length();
	}
}