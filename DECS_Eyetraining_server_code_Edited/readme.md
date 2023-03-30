# server that logs eye training application activity

*The things that can't be shared is excluded such as root information and private URL(Client URL)*

Everything is up to your environment setting, as you know.

If you can't recognize nothing, following keywords will help you to find what you need: mongoDB, express

### Quick start

Don't forget to set your environment.

1. `$ npm install` 
2. Set environment variables depending your project setting (`USER`, `PASSWORD`, `CLIENT_URL`)
3. `node --exec babel-node index.js`


### Deployment example

1. (ubuntu bash) `$ docker exec -it <container-name> bash`
2. (docker bash) `# mongo -u root -p 12341234 -authenticationDatabase admin`
3. (docker bash) `db.createUser({user:'admin',pwd:'12341234',roles:['dbOwner']})`

might need `dockerd` command before using docker
`docker-compose -f stack.yml up` or `down` for boot

## DB Scheme

This, unreleased proejct, has only one purpose of logging user's activity. Although it should have developed and revised, it has stopped with first prototype because of the stakeholders of this project.

### User table (v 0.0.1)

| user table        |
| ----------------- |
| ID                |
| PW (hashed by ID) |
| sex               |
| surgery           |
| eyesight          |
| lens              |
| disorder          |
| userKey           |
| ----------------- |

## you might needs

- docker `https://codechacha.com/ko/fix-couldnt-connect-to-docker-daemon/`
- babel node  `https://www.daleseo.com/js-babel-node/`
- api documentation `https://gracious-agnesi-48e5fb.netlify.app/#errors`