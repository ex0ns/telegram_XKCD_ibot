package me.ex0ns.inlinexkcd

import me.ex0ns.inlinexkcd.bot.InlineXKCDBot
import me.ex0ns.inlinexkcd.parser.XKCDHttpParser

/**
  * Created by ex0ns on 6/8/16.
  */
object main {
  def main(args: Array[String]): Unit = {
    if (args.length == 0) InlineXKCDBot.run()
    else {
      if (args(0) == "parse") {
        val parser = new XKCDHttpParser()
        if (args.length == 1) parser.parseAll()
        else parser.parseID(args(1).toInt)
      }
    }
  }
}
