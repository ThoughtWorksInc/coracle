# Coracle
-----------------

Coracle is a lightweight Clojure application for storing Activity Stream 2.0 activities into mongo, and subsequently retrieving them.


### Docker Deployment
----------------------

The latest Coracle image can be retrieved with:

```docker pull dcent/coracle```

A container can be started like this:

```docker run -d --name coracle --link mongo:mongo -p 7000:7000 dcent/coracle```

Note this assumes that you have a mongo container running with the name 'mongo'.


### Configuration
------------------

The following optional environment variables can be passed into the docker container to configure the application.

- ``` APP_HOST ``` - defaults to ```0.0.0.0```
- ``` APP_PORT ``` - defaults to ```7000```
- ``` MONGO_PORT ``` - defaults to ```27017```
- ``` MONGO_DB ``` - defaults to ```coracle```
- ``` MONGO_URI ``` - defaults will take host and port to generate itself.
- ``` BEARER_TOKEN ``` - defaults to ```nil```

### Using Bearer Token
----------------------

By setting the ``` BEARER_TOKEN ``` environment variable, the app will then only allow POST requests that have a matching token in the HTTP header (e.g. "bearer_token" "123")
