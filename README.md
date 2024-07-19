# Catalyst
 Modern java framework api

    

### simple server
```java
CatalystServer server = CatalystServer.init();
server.get("/hello", ((req, res) -> res.send("hello")));
server.listen(8080);
```
###  middleware and afterResponse
```java
server.middleware(((req, res, chain) -> {
    System.out.println("hello  chain");
    chain.next(req, res);
}));
server.afterResponse(((req, res, chain) -> {
    System.out.println("afterresponse");
    chain.next(req, res);
}));
```

###  group 
```java
GroupRoute api = server.group("/api/v1");
api.get("/hello", ((req, res) -> res.send("hello from group")));
api.middleware(((req, res, chain) -> {
    System.out.println("in group api");
    chain.next(req, res);
}));
api.afterResponse(((req, res, chain) -> {
    System.out.println("after Response");
    chain.next(req, res);
}));
```


