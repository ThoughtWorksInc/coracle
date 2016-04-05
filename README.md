# Coracle

Coracle is a lightweight Clojure application for storing Activity Stream 2.0 activities into mongo, and subsequently retrieving them.


----------------------
### Docker Deployment

The latest Coracle image can be retrieved with:

    docker pull dcent/coracle

A container can be started like this:

    docker run -d --name coracle --env-file=<path to env file>/coracle.env --link coracle-mongo:mongo -p 7000:7000 dcent/coracle

Note this assumes that you have a mongo container running with the name 'coracle-mongo'.

### Heroku Deployment

#### Heroku account setup:
- Create a heroku account
- Create new heroku app
- Install heroku tool-belt

#### mLab database setup 
- Create mLab account
- Create database
- Get MongoDB host, port and DB from the URI: `mongodb://[host]:[port]/[db]`

#### Local heroku configuration:
- Clone the repository: `git clone https://github.com/d-cent/coracle.git`
- Log in to your Heroku account: `heroku login`
- Add heroku remote: `heroku git:remote -a [APP_NAME]`
- Set buildpack: `heroku buildpacks:add heroku/clojure`
- Set Config Vars on Heroku
  - SECURE = true
  - BEARER_TOKEN = [anything]
  - EXTERNAL_JWK_SET_URL = [[APP_NAME].herokuapp.com/jwk-set]
  - MONGODB_HOST = [MongoDB host from mLab]
  - MONGODB_PORT = [MongoDB port from mLab]
  - MONGODB_DB = [MongoDB DB from mLab]

- Push to Heroku


----------------------
### Configuration

The following environment variables can be passed into the docker container to configure the application.

##### Required:

- **BEARER_TOKEN** -  The app will not start without it. Only POST requests that have a matching token in the HTTP header (e.g. "bearer-token" "123") are permitted.
- **EXTERNAL_JWK_SET_URL** - The app will not start without it. the location of the endpoint in the app to get the keyset. Will be the address of the coracle instance with /jwk-set
   
##### Optional:

- **HOST** - defaults to **0.0.0.0**
- **PORT** - defaults to **7000**
- **MONGODB_PORT** - defaults to **27017**
- **MONGODB_DB** - defaults to **coracle**
- **SECURE** - defaults to **false** - When set to **true** this will wrap the app with middleware that enforces https-only connections.
