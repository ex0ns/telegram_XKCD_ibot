package me.ex0ns.inlinexkcd.bot

import com.typesafe.scalalogging.Logger
import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.methods.AnswerInlineQuery
import info.mukel.telegrambot4s.models.{InlineQuery, InlineQueryResultPhoto}
import me.ex0ns.inlinexkcd.database.Database
import org.mongodb.scala.bson.BsonString
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

  override def handleInlineQuery(inlineQuery: InlineQuery) = {
    database.search(inlineQuery.query).map(documents => {
      val urls : Seq[String] = documents.map(_.get[BsonString]("img").get.getValue)
      val pictures = urls.zipWithIndex.map{case (x,i) => InlineQueryResultPhoto(i.toString, x, x)}
      api.request(AnswerInlineQuery(inlineQuery.id, pictures))
    })
  }
}
