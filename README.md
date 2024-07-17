# Catalyst
 Modern java framework api
## setup
    curl https://raw.githubusercontent.com/natthphong/catalyst/main/setting.xml | sed "s/{github-username}/natthaphong/" | sed "s/{github-personal-access-token}/***/" | tee ~/.m2/settings.xml
    

### example
    AppServer app = new AppServer();
        app.middleware((req, res, chain) -> {
            System.out.println("Request received: " + req.getPath());
            chain.next(req, res);
        });
        app.get("/hello",((req, res) -> {
            System.out.println("hello");
            return "HELLO";
        }));
        app.listen(8080);

