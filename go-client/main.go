package main

import (
	"bufio"
	"context"
	"fmt"
	"go-client/proto"
	"log"
	"os"
	"unicode"

	"google.golang.org/grpc"
)

func main() {

	const serverAddress = "localhost:8080"

	// Create the gRPC client
	client, conn, err := createGRPCClient(serverAddress)
	if err != nil {
		log.Fatalf("Could not create client: %v", err)
	}
	defer conn.Close()

	fmt.Println("Connected to server at " + serverAddress)

	var menuSelection rune = '.'

	scanner := bufio.NewScanner(os.Stdin)

	for menuSelection != 'q' {
		printMenu()

		for scanner.Scan() {
			if len(scanner.Text()) != 0 {
				menuSelection = unicode.ToLower(rune(scanner.Text()[0]))
				fmt.Println()
				break
			} else {
				fmt.Println("Invalid")
			}
		}

		readChoice(menuSelection, client)
	}

	fmt.Println("Disconnecting from server.")
}

// Initialize gRPC client
func createGRPCClient(serverAddress string) (proto.DatabaseManagementServiceClient, *grpc.ClientConn, error) {
	// Connect to the Java gRPC server
	conn, err := grpc.Dial(serverAddress, grpc.WithInsecure())
	if err != nil {
		return nil, nil, fmt.Errorf("failed to connect: %v", err)
	}

	client := proto.NewDatabaseManagementServiceClient(conn)
	return client, conn, nil
}

// List each table name in the database
func listTableNames(client proto.DatabaseManagementServiceClient) {
	req := &proto.ListTablesRequest{} // Empty request, nothing additional needed

	res, err := client.ListTableNames(context.Background(), req)
	if err != nil {
		log.Fatalf("Error calling ListTableNames")
	}

	if res.Success {
		fmt.Println(("Table names:"))
		for _, tableName := range res.TableNames {
			fmt.Printf("\t%v\n", tableName) // Print each table name
		}
		fmt.Println()
	} else {
		fmt.Println("There are no tables present in the database.")
	}
}

// Reset the database to the original example
func createAllTables(client proto.DatabaseManagementServiceClient) {
	req := &proto.CreateAllTablesRequest{} // Empty request, nothing additional needed

	res, err := client.CreateAllTables(context.Background(), req)
	if err != nil {
		log.Fatalf("Error calling CreateAllTables: %v", err)
	}
	fmt.Printf("CreateAllTables response: Success=%v, Message=%s\n", res.Success, res.Message)
}

// Drops every table in the database
func dropAllTables(client proto.DatabaseManagementServiceClient) {
	req := &proto.DropAllTablesRequest{} // Empty request, nothing additional needed

	// Make the RPC call
	res, err := client.DropAllTables(context.Background(), req)
	if err != nil {
		log.Fatalf("Error calling DropAllTables: %v", err)
	}
	fmt.Printf("DropAllTables response: Success=%v, Message=%s\n", res.Success, res.Message)
}

// Print a table in the database if present
func printTable(client proto.DatabaseManagementServiceClient, tableName string) {
	req := &proto.StringCommandRequest{
		Command: tableName,
	}

	res, err := client.PrintTable(context.Background(), req)
	if err != nil {
		fmt.Printf("Error while printing table: %v\n", err)
		return
	}

	if res.Success {
		fmt.Printf("%v:\n", tableName)
	}
	for _, row := range res.TableRows {
		fmt.Println(row)
	}
	fmt.Println()

}

// Executes a user enter command into the database
func executeSql(client proto.DatabaseManagementServiceClient, command string) {
	req := &proto.StringCommandRequest{
		Command: command,
	}

	res, err := client.ExecuteSql(context.Background(), req)
	if err != nil {
		fmt.Printf("Error while executing statement: %v\n", err)
		return
	}

	fmt.Printf("Database response: \n%v\n", res)
}

// Print client choices
func printMenu() {
	fmt.Println("Server action selection:")
	fmt.Println("\tl. List table names")
	fmt.Println("\td. Drop all tables")
	fmt.Println("\tc. Create all tables")
	fmt.Println("\tp. Print a table")
	fmt.Println("\te. Execute sql")

	fmt.Println("\n\tq. Quit program")
}

func readChoice(choice rune, client proto.DatabaseManagementServiceClient) {
	var menuSelection rune = '.'

	scanner := bufio.NewScanner(os.Stdin)

	switch choice {
	case 'l':
		listTableNames(client)
	case 'd':
		fmt.Println("Are you sure you want to drop all tables? It may not be a good idea.")
		fmt.Println("y or n")

		for scanner.Scan() {
			if len(scanner.Text()) != 0 {
				menuSelection = unicode.ToLower(rune(scanner.Text()[0]))
				fmt.Println()
				if menuSelection == 'y' {
					dropAllTables(client)
				}
				break
			}
		}
	case 'c':
		fmt.Println("This will reset the database to its original state.")
		fmt.Println("Continue? y or n")

		scanner.Scan()
		if len(scanner.Text()) != 0 {
			menuSelection = unicode.ToLower(rune(scanner.Text()[0]))
			fmt.Println()
			if menuSelection == 'y' {
				createAllTables(client)
			}
		}
	case 'p':
		fmt.Println("Print which table?")

		if scanner.Scan() {
			if len(scanner.Text()) != 0 {
				tableChoice := scanner.Text()
				fmt.Println()
				printTable(client, tableChoice)
			}
		}
	case 'e':
		fmt.Println("Enter sql below to be executed")

		if scanner.Scan() {
			if len(scanner.Text()) != 0 {
				command := scanner.Text()
				fmt.Println()
				executeSql(client, command)
			}
		}
	case 'q':
		break
	default:
		fmt.Printf("%c is not a valid choice. Please refer to the menu.\n", choice)
	}
}
