import grpc
import database_service_pb2
import database_service_pb2_grpc

# Print client choices
def print_menu():
    print("Server action selection:")
    print("\tl. List table names")
    print("\td. Drop all tables")
    print("\tc. Create all tables")
    print("\tp. Print a table")
    print("\te. Execute sql")

    print("\n\tq. Quit program")

# List each table name in the database
def list_table_names(client):
    request = database_service_pb2.ListTablesRequest()
    try:
        response = client.ListTableNames(request)
        if response.success:
            print("Table names:")
            for table_name in response.table_names:
                print(f"\t{table_name}")
        else:
            print("There are no tables present in the database.")
    except grpc.RpcError as e:
        print(f"Error calling ListTableNames: {e}")

# Reset the database to the original example
def create_all_tables(client):
    request = database_service_pb2.CreateAllTablesRequest()
    try:
        response = client.CreateAllTables(request)
        print(f"CreateAllTables response: Success={response.success}, Message={response.message}")
    except grpc.RpcError as e:
        print(f"Error calling CreateAllTables: {e}")

# Reset the database to the original example
def drop_all_tables(client):
    request = database_service_pb2.DropAllTablesRequest()
    try:
        response = client.DropAllTables(request)
        print(f"DropAllTables response: Success={response.success}, Message={response.message}")
    except grpc.RpcError as e:
        print(f"Error calling DropAllTables: {e}")

# Print a table in the database if present
def print_table(client, table_name):
    request = database_service_pb2.StringCommandRequest(command=table_name)
    try:
        response = client.PrintTable(request)
        if response.success:
            print(f"{table_name}:")
            for row in response.table_rows:
                print(row)
        else:
            print(f"Failed to print table {table_name}.")
    except grpc.RpcError as e:
        print(f"Error while printing table: {e}")

# Executes a user enter command into the database
def execute_sql(client, command):
    request = database_service_pb2.StringCommandRequest(command=command)
    try:
        response = client.ExecuteSql(request)
        print(f"SQL Execution Response: \n{response.message}")
    except grpc.RpcError as e:
        print(f"Error while executing SQL: {e}")

# Read user entered input and talk to server
def read_choice(choice, client):
    if choice == 'l':
        list_table_names(client)
    elif choice == 'd':
        confirm = input("Are you sure you want to drop all tables? (y/n): ").strip().lower()
        if confirm == 'y':
            drop_all_tables(client)
    elif choice == 'c':
        confirm = input("This will reset the database to its original state. Continue? (y/n): ").strip().lower()
        if confirm == 'y':
            create_all_tables(client)
    elif choice == 'p':
        table_name = input("Enter the table name to print: ").strip()
        print_table(client, table_name)
    elif choice == 'e':
        command = input("Enter SQL command to execute: ").strip()
        execute_sql(client, command)
    elif choice == 'q':
        print("Disconnecting from server.")
    else:
        print(f"'{choice}' is not a valid choice. Please refer to the menu.")

# Entry point to program
def main():
    server_address = 'localhost:8080'
    with grpc.insecure_channel(server_address) as channel:
        client = database_service_pb2_grpc.DatabaseManagementServiceStub(channel)
        print(f"Connected to server at {server_address}")

        choice = '.'
        while choice != 'q':
            print_menu()
            choice = input("Enter your choice: ").strip().lower()
            read_choice(choice, client)

if __name__ == '__main__':
    main()
