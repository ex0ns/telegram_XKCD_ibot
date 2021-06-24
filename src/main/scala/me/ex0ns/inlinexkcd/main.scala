package me.ex0ns.inlinexkcd

import me.ex0ns.inlinexkcd.bot.InlineXKCDBot
import me.ex0ns.inlinexkcd.parser.XKCDHttpParser

import scala.io.Source
import scala.util.Properties
import me.ex0ns.inlinexkcd.database.Comics
import scala.concurrent.duration._
import scala.concurrent.Await

object main {
  def main(args: Array[String]): Unit = {
    def token =
      Properties
        .envOrNone("TELEGRAM_KEY")
        .getOrElse(Source.fromFile("telegram.key").getLines().next)

    if (args.isEmpty) new InlineXKCDBot(token).run()
    else {
      if (args.head == "parse") {
        val parser = new XKCDHttpParser()
        if (args.length == 1) parser.parseAll()
        else parser.parseID(args(1).toInt)
      }
    }
  }
}
