hotdocs-cloud-java
==================
The hotdocs-cloud-java library is a client wrapper for the HotDocs Cloud Services REST API, implemented in Java.

The binary jar file may be downloaded from http://files.hotdocs.ws/download/hotdocs-cloud-1.0.0.jar.

To use the library, follow these steps:

1. Create a `Client` object.
2. Create a `Request` object.
3. Call the `sendRequest` method of the client object, passing in the request object.

For example, to create an embedded HotDocs session:
```java
Client client = new Client("SUBSCRIBER_ID", "SIGNING_KEY");  
Request request = new CreateSessionRequest("Package ID", "C:\\myfilepath\\package.hdpkg");  
String sessionId = client.sendRequest(request);  
```
