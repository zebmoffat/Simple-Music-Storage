package databasemanagement;

import java.util.List;

public class DatabaseServiceImpl extends DatabaseManagementServiceGrpc.DatabaseManagementServiceImplBase {

    private final Database database;

    public DatabaseServiceImpl(Database database) {
        this.database = database;
    }

    @Override
    public void listTableNames(databasemanagement.DatabaseService.ListTablesRequest request,
                              io.grpc.stub.StreamObserver<databasemanagement.DatabaseService.TableNamesResponse> responseObserver) {
        
        databasemanagement.DatabaseService.TableNamesResponse.Builder response = databasemanagement.DatabaseService.TableNamesResponse.newBuilder();

        List<String> tables = database.listTables();

        if (!tables.isEmpty()) {
            response.setSuccess(true);
            response.addAllTableNames(tables);
        } else {
            response.setSuccess(false).addTableNames("No tables were present in the database.");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void dropAllTables(databasemanagement.DatabaseService.DropAllTablesRequest request,
            io.grpc.stub.StreamObserver<databasemanagement.DatabaseService.OperationResponse> responseObserver) {

        boolean success = database.dropAllTables(false);

        databasemanagement.DatabaseService.OperationResponse.Builder response = databasemanagement.DatabaseService.OperationResponse.newBuilder();
        
        response.setSuccess(success);

        if (success) {
            response.setMessage("All tables dropped successfully.");
        } else {
            response.setMessage("An error occured while attempting to drop tables.");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createAllTables(databasemanagement.DatabaseService.CreateAllTablesRequest request,
            io.grpc.stub.StreamObserver<databasemanagement.DatabaseService.OperationResponse> responseObserver) {

        boolean success = database.createAllTables(false);

        databasemanagement.DatabaseService.OperationResponse response = databasemanagement.DatabaseService.OperationResponse
                .newBuilder()
                .setSuccess(success)
                .setMessage(success ? "All tables created successfully." : "Could not create tables.")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void printTable(databasemanagement.DatabaseService.StringCommandRequest request,
            io.grpc.stub.StreamObserver<databasemanagement.DatabaseService.TableContentResponse> responseObserver) {

        databasemanagement.DatabaseService.TableContentResponse.Builder response = databasemanagement.DatabaseService.TableContentResponse.newBuilder();

        List<String> table = database.printTable(request.getCommand());
        
        if (table != null) {
            response.setSuccess(true);
            response.addAllTableRows(table);
        } else {
            response.setSuccess(false);
            response.addAllTableRows(null);
        }

    
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void executeSql(databasemanagement.DatabaseService.StringCommandRequest request,
            io.grpc.stub.StreamObserver<databasemanagement.DatabaseService.OperationResponse> responseObserver) {

        databasemanagement.DatabaseService.OperationResponse.Builder response = databasemanagement.DatabaseService.OperationResponse.newBuilder();

        String command = request.getCommand();

        String result = database.executeSql(command);

        System.out.println("Resulting string from command:\n" + result + "\n");

        if (!(result.substring(0, 6).equals("Error:"))) {
            response.setSuccess(true);
        } else {
            response.setSuccess(false);
        }
        response.setMessage(result);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}