package me.ex0ns.inlinexkcd

import me.ex0ns.inlinexkcd.bot.InlineXKCDBot
import me.ex0ns.inlinexkcd.parser.XKCDHttpParser

import scala.io.Source
import scala.util.Properties

/**
  * Created by ex0ns on 6/8/16.
  */
object main {
  def main(args: Array[String]): Unit = {
    def token =
      Properties
        .envOrNone("TELEGRAM_KEY")
        .getOrElse(Source.fromFile("telegram.key").getLines().next)

    if (args.length == 0) new InlineXKCDBot(token).run()
    else {
      if (args(0) == "parse") {
        val parser = new XKCDHttpParser()
        if (args.length == 1) parser.parseAll()
        else parser.parseID(args(1).toInt)
      }
    }
  }
}
