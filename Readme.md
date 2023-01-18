# Read Me First
## Overall Design
1. The pub/sub system is based on a queue system by topic/channel and each queue contains a list of messages. The main classes
   handling the domain are:
    * Queue class: The class contains a list of messages with insertion order respected (LinkedList<>). 
      The class checks its list of messages every 5 seconds(default ticker but can be defined in another value) and remove the messages based on the criteria defined (if they were served to all the subscribers or if they are expired). In any case, getMessage check if the messages are expired before returning them, and if a consumer requested in a previous opportunity only are returned the newest.
    * PubSub: The class handles the different queues by topic and the possibility of subscription to those. This is a static class with one only instance shared by the application.
    * Message: a simple class where is defined the message and its expiration.
    
2. Additional to the domain classes exists a service class to interact with the data domain and the inputs to the system, as by example the expiration for each message is defined in seconds(int) and internally is stored as an Instant java time with the seconds added to this Instant.now().    
    
## How to execute
1. In a terminal write ````mvn clean package```` in the root folder of the project, this will generate a war file
2. Deploying the WAR to Tomcat

To have our WAR file deployed and running in Tomcat, we'll need to complete the following steps:
* Download Apache Tomcat and unpackage it into a tomcat folder
* Copy our WAR file from target/spring-boot-deployment.war to the tomcat/webapps/ folder
* From a terminal, navigate to the tomcat/bin folder and execute

````catalina.bat run (on Windows)````

```catalina.sh run (on Unix-based systems)```

## Endpoints 
1. To publish messages => ````POST http://localhost:8080/pubSubSystem/<topic_name>```` (This will create automatically the topic/channel), it returns a text ````messag published```` if all was OK
2. To subscribe => ````POST http://localhost:8080/pubSubSystem/<topic_name>/subscribe````, if the topic doesn't exist you will receive a 404; in case of a successful response(200)  you will receive the subscribe key (treat this as a password)
3. To get messages => ````GET http://localhost:8080/pubSubSystem/<topic_name>````, for now, this is not streaming fashion.
4. To unsubscribe => ````DELETE http://localhost:8080/pubSubSystem/<topic_name>/unsusbcribe````. it returns the message ````unsubscribed```` if all was ok.


## TO-DOs (Next Steps)
1. Implement Streaming fashion data in get_messages endpoint.
2. Add lombok to reduce the exposure of some fields/properties in some classes
3. Improve the security mechanism to handle user/subKey better.
4. Improve in message format returned by some of the endpoints
5. Add Swagger to have documentation of the endpoints.
6. Increment unit test coverage and add integration tests.