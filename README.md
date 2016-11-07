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


### Testing

@TODO

### Contribute

Feel free to contribute, report any bug or submit ideas to improve the bot !

### License

See [License](https://github.com/ex0ns/telegram_XKCD_ibot/blob/master/LICENSE)
