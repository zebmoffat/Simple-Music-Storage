package server;
/*
 * Entry point for Java multithreaded server
*/
import java.io.IOException;

import databasemanagement.Database;
import databasemanagement.DatabaseServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Main {

	final static int PORT = 8080;

	public static void main(String[] args) {
        
		if (args.length != 3) {
			System.out.println("Incorrect arguments. Usage:\n");
			System.out.println("java src.Main <database url> <username> <password>");
			System.out.println("\nMake sure to include a MySQL driver in classpath as well.\n");
			
			System.exit(1);
		}
		
        Database database = new Database(args); 
		
        try {
            // Start the gRPC server
            Server server = ServerBuilder
                .forPort(PORT)
                .addService(new DatabaseServiceImpl(database)) // Add grpc service to server
                .build();

            server.start();
            System.out.println("gRPC server started, listening on port " + PORT);
            

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down gRPC server...");
                server.shutdown();
                database.closeResources();
                System.out.println("Server shut down.");
            }));

            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to start gRPC server: " + e.getMessage());
        }
    }
}
