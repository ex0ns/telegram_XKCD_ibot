package me.ex0ns.inlinexkcd.bot

import com.typesafe.scalalogging.Logger
import cronish.Cron
import cronish.dsl._
import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.methods.{AnswerInlineQuery, GetMe, SendMessage}
import info.mukel.telegrambot4s.models._
import me.ex0ns.inlinexkcd.database.{Comics, Database, Groups}
import me.ex0ns.inlinexkcd.parser.XKCDHttpParser
import org.mongodb.scala.bson.{BsonInt32, BsonString}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

/**
  * Created by ex0ns on 06/08/16.
  */
object InlineXKCDBot extends TelegramBot with Commands with Polling {

  override def token    = Source.fromFile("telegram.key").getLines().next
  private val me        = Await.result(api.request(GetMe), Duration.Inf)
  private val logger    = Logger(LoggerFactory.getLogger(InlineXKCDBot.getClass))

  private val parser    = new XKCDHttpParser()
  private val comics    = new Comics()
  private val groups    = new Groups()

  logger.debug("Bot is up and running !")

  def parseComic : Unit = {
    comics.lastID onSuccess {
      case document =>
        val id = document.get[BsonInt32]("_id").get.intValue()
        // Try to parse comics as long as ID is valid (many published the same day, or we missed one day)
        parser.parseID(id + 1) onSuccess   { case _ =>  parseComic }
    }
  }

  task(parseComic) executes Cron("00", "*/30", "12-18", "*", "*", "1,3,5", "*") //" every half hour, every Monday, Wednesday, Friday between 12 and 18"

  override def handleInlineQuery(inlineQuery: InlineQuery) = {
    val results = if(inlineQuery.query.isEmpty) comics.lasts else comics.search(inlineQuery.query)
    results.map(documents => {
      val results = documents.map(document =>
        (document.get[BsonInt32]("_id").get.intValue().toString, document.get[BsonString]("img").get.getValue))

      val pictures = results.map { case (id, url) => InlineQueryResultPhoto(id, url, url) }
      api.request(AnswerInlineQuery(inlineQuery.id, pictures))
    })
  }

  override def handleMessage(message : Message) = {
    message.newChatMember.filter((user) => user.id == me.id).foreach(_ => {
      groups.insert(message.chat.id.toString)
    })

    message.leftChatMember.filter((user) => user.id == me.id).foreach(_ => {
      groups.remove(message.chat.id.toString)
    })
  }

  /*
   * /setinlinefeedback must be enable for the bot
   */
  override def handleChosenInlineResult(chosenInlineResult: ChosenInlineResult) = {
    comics.increaseViews(chosenInlineResult.resultId.toInt)
  }
}
