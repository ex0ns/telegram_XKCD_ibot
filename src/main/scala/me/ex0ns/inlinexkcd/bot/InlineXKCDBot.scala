package me.ex0ns.inlinexkcd.bot

import com.typesafe.scalalogging.Logger
import cronish.Cron
import cronish.dsl._
import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.methods.AnswerInlineQuery
import info.mukel.telegrambot4s.models._
import me.ex0ns.inlinexkcd.database.Database
import me.ex0ns.inlinexkcd.parser.XKCDHttpParser
import org.mongodb.scala.bson.{BsonInt32, BsonString}
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.{Failure, Success}

/**
  * Created by ex0ns on 06/08/16.
  */
object InlineXKCDBot extends TelegramBot with Commands with Polling{

  override def token = Source.fromFile("telegram.key").getLines().next
  private val logger = Logger(LoggerFactory.getLogger(InlineXKCDBot.getClass))
  private val parser = new XKCDHttpParser()
  private val database = new Database()

  logger.debug("Bot is up and running !")

  val parseComic = task {
    database.lastID().onSuccess {
      case document =>
        val id = document.get[BsonInt32]("_id").get.intValue()
        parser.parseID(id + 1)
    }
  }

  parseComic executes Cron("00", "00", "16", "*", "*", "1,3,5", "*") //"every Monday, Wednesday, Friday at 16"

  override def handleInlineQuery(inlineQuery: InlineQuery) = {
    database.search(inlineQuery.query).map(documents => {
      val results = documents.map(document =>
        (document.get[BsonInt32]("_id").get.intValue().toString, document.get[BsonString]("img").get.getValue))

      val pictures = results.map{case (id, url) => InlineQueryResultPhoto(id, url, url)}
      api.request(AnswerInlineQuery(inlineQuery.id, pictures))
    })
  }

  /*
   * /setinlinefeedback must be enable for the bot
   */
  override def handleChosenInlineResult(chosenInlineResult: ChosenInlineResult) = {
    database.increaseViews(chosenInlineResult.resultId.toInt)
  }
}
