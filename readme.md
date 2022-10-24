# Installation manual

## Build the project using the command below : 

```bash
mvn clean package
```

# Usage manual :

## In order to test the javanaise 2 project : 

1. Head to the enhancedIrc package
2. Start the coordinator main method
3. Start different clients using the EnhancedIrc main method

## In order to test the burst :

1. Head to the counter package 
2. Start the coordinator main method
3. Start the main method in the Main class

## In order to test local cache saturation :

1. Head to the localCache package 
2. Start the coordinator main method
3. Start the main method in the Main class

## In order to test the management of client failure

1. Head to the enhancedIrc package
2. Start the coordinator main method
3. Start different clients using the EnhancedIrc main method
4. Stop a client 
5. Continue to read and write in the shared object with the others

## In order to test the management of coordinator failure

1. Head to the enhancedIrc package
2. Start the coordinator main method
3. Start different clients using the EnhancedIrc main method
4. Stop the coordinator and start it again with the Coordinator main method
5. Continue to read and write with the clients
