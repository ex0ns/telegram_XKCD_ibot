# XKCD Bot

XKCDBot is a [Telegram](https://telegram.org) bot, made in scala using the [telegrambot4s](https://github.com/mukel/telegrambot4s) library.
The goal is to send/share XKCD comics in a easier way.

### Installation && build

You obviously need Scala and SBT to make it run.

All you need to make it run is a mongoDB server (> 3.x) and a [Telegram Bot key](https://core.telegram.org/bots), this token must be placed into a 
file at the root of the project.

The first step is to fill the database:

```
sbt 'run parse'
```

This will create a database called 'xkcd' and a 'comics' collection within it.


Then you can run the bot using ```sbt run``` with no parameters.

### Features

You can search and post XKCD comics using the inline features of the bot ```@xkcdibot search```. If `search` is empty, 
then it will display the latest XKCD (ordered by date). 
Currently, the number of results is limiter to 50.

If you add the bot to a group, it will then automatically publish every new XKCD as soon as it's available.

### JAR

[SBT Assembly](https://github.com/sbt/sbt-assembly) is used to generate a single portable jar, juste run ```sbt assembly``` to create it.
When deploying the JAR, do not forget to send the ```telegram.key``` file as well (not bundle inside the JAR for security reasons).
 
### Docker

The easiest way to run your own copy of this bot (why would you even need to do that ??) is to use the docker image available [here](https://hub.docker.com/r/ex0ns/inlinexkcd/).
You'll need to pass `TELEGRAM_KEY` environment variable to docker for the bot to work.

In order for the docker to connect to the database on localhost, you must run it with `--net=host` option. 
You can also give `DB_URL` and `DB_PORT` environment variables to connect to a mongoDB server.

```bash
# Using a mongoDB instance on host
docker run -e "TELEGRAM_KEY=$(cat telegram.key)" --net=host --name inlinexkcd ex0ns/inlinexkcd:latest
# Using a remote mongoDB intance
sudo docker run -e "TELEGRAM_KEY=$(cat telegram.key) DB_URL=10.0.1.1 DB_PORT=27017" --name inlinexkcd ex0ns/inlinexkcd:latest
```

We use [docker-compose](https://docs.docker.com/compose/) to deploy a new MongoDB container as well as one running the actual bot.
If you cloned the repo, then simply run `sbt dockerComposeUp` to run docker compose and deploy the two new containers.

Otherwise you could just get a copy of the [dockerfile](https://raw.githubusercontent.com/ex0ns/telegram_XKCD_ibot/master/docker/docker-compose.yml) and run:

```bash
wget https://raw.githubusercontent.com/ex0ns/telegram_XKCD_ibot/master/docker/docker-compose.yml
TELEGRAM_KEY=$(cat telegram.key) docker-compose up
```

### Testing

@TODO

### Contribute

Feel free to contribute, report any bug or submit ideas to improve the bot !

### License

See [License](https://github.com/ex0ns/telegram_XKCD_ibot/blob/master/LICENSE)
