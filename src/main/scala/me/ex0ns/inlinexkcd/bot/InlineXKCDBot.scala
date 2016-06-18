package me.ex0ns.inlinexkcd.bot

import com.typesafe.scalalogging.Logger
import info.mukel.telegrambot4s.api._
import me.ex0ns.inlinexkcd.database.Database
import org.slf4j.LoggerFactory

import scala.io.Source


/**
  * Created by ex0ns on 06/08/16.
  */
object InlineXKCDBot extends TelegramBot with Commands with Polling{

  override def token = Source.fromFile("keys/telegram.key").getLines().next
  private val logger = Logger(LoggerFactory.getLogger(InlineXKCDBot.getClass))
  private val database = new Database()

  logger.debug("Bot is up and running !")

  on("/search") { implicit  msg => args =>
    database.search(args.mkString(" ")).map(x => {
      reply({
        x.take(1).map(y => y.toJson()).mkString(" and ")
      })
    })
  }

  on("/help") { implicit msg => _ =>
    reply(usage)
  }

  val usage: String =
    """
      |Inline KXCD usage
    """.stripMargin
}
