# Simple-Music-Storage
Simple-Music-Storage is an application that enables seamless communication between clients and a MySQL database server. Built using Java for the server-side, the app supports CRUD operations on the database. Communication between the server and clients is handled via gRPC, ensuring efficient and secure data exchange.

Clients are designed in both Python and Go, allowing flexibility in how users interact with the server. You can use either the Python or Go client, or both, to manage data stored in the MySQL database, providing an easy-to-use interface for simple music storage and management.

Key features:
* Java-based server implementing CRUD operations on a MySQL database
* gRPC for fast and secure communication between server and clients
* Python and Go clients for user interaction with the database
* Basic unit tests for Java server functionality using JUnit

## How to run
### Java Server
1. Download Java for your operating system from [https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/)
2. Download the Gradle build tool from [https://gradle.org/install/](https://gradle.org/install/)
3. Change the arguments in the run task or the system properties in the test task to be your MySQL database address, your database username, and your database passwod
4. In the java-server directory run the command ```gradle run``` to run the server or ```gradle test``` to run the server tests

### Python Client
1. Download Python for your operating system from [https://www.python.org/downloads/](https://www.python.org/downloads/)
2. In the python-client directory, run ```python main.py``` when the server is online

### Go Client
1. Download the Go language for your operating system from [https://go.dev/dl/](https://go.dev/dl/)
2. In the go-client directory, run ```go run main.go``` when the server is online

## How to build
### gRPC
1. Download gRPC for each language you will use from [https://grpc.io/](https://grpc.io/)

### protoc
1. Install Protocol Buffers from [https://github.com/protocolbuffers/protobuf/releases](https://github.com/protocolbuffers/protobuf/releases)

### Java Server
1. Download Java for your operating system from [https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/)
2. Download the Gradle build tool from [https://gradle.org/install/](https://gradle.org/install/)
3. In the build.gradle file
4. run ```gradle build```
5. Pass the arguments [MySQL server address, database username, database password] when running the jar

### Python Client
1. Install gRPC and gRPC tools from [https://grpc.io/docs/languages/python/quickstart/](https://grpc.io/docs/languages/python/quickstart/)
2. In the root directory run ```python -m grpc_tools.protoc -I=proto --python_out=./python-client --grpc_python_out=./python-client ./proto/database_service.proto```
3. Run the Python client with ```python main.py```

### Go Client
1. Install the Go Plugins with  
   ```go install google.golang.org/protobuf/cmd/protoc-gen-go@latest```  
   ```go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest```  
2. In the projects root directory run ```protoc --go_out=go-client --go-grpc_out=go-client proto/database_service.proto```  
3. Run ```go build``` in the go-server directory