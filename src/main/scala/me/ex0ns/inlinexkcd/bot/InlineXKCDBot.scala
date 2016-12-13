package me.ex0ns.inlinexkcd.bot

import com.typesafe.scalalogging.Logger
import cronish.Cron
import cronish.dsl._
import fr.hmil.scalahttp.client.HttpResponse
import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models._
import me.ex0ns.inlinexkcd.database.{Comics, Groups}
import me.ex0ns.inlinexkcd.helpers.DocumentHelpers._
import me.ex0ns.inlinexkcd.models.Group
import me.ex0ns.inlinexkcd.parser.XKCDHttpParser
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonInt32, BsonString}
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.Properties

/**
  * Created by ex0ns on 06/08/16.
  */
object InlineXKCDBot extends TelegramBot with Commands with Polling {

  override def token = Properties.envOrNone("TELEGRAM_KEY").getOrElse(Source.fromFile("telegram.key").getLines().next)

  private val MESSAGE_ORDER_DELAY = 200

  private val me = Await.result(api.request(GetMe), Duration.Inf)
  private val logger = Logger(LoggerFactory.getLogger(InlineXKCDBot.getClass))

  private val parser = new XKCDHttpParser()

  logger.debug("Bot is up and running !")

  def parseComic(notify : Boolean = false): Unit = {

    def notifyAllGroups(url: String, title: String, text: String) = {

      def notifyNewXKCD(group: Group) : Unit = {
        api.request(SendMessage(Left(group._id), title, Some(ParseMode.Markdown)))
        Thread.sleep(MESSAGE_ORDER_DELAY)
        api.request(SendPhoto(Left(group._id), Right(url)))
        Thread.sleep(MESSAGE_ORDER_DELAY)
        api.request(SendMessage(Left(group._id), text))
      }

      Groups.notifyAllGroups(notifyNewXKCD)
      parseComic(notify)
    }

    Comics.lastID onSuccess {
      case Some(comic) =>
        parser.parseID(comic._id + 1) onSuccess {
          case response: HttpResponse if notify =>
            Document(response.body).toComic.foreach((comic) => notifyAllGroups(comic.img, comic.title, comic.getText))
          case _ => parseComic(notify)
        }
    }
  }

  Comics.empty onSuccess  {
    case true   => parser.parseAll()
    case false  => parseComic() // Parse comics we could have missed
  }

  task(parseComic(true)) executes Cron("00", "*/15", "9-23", "*", "*", "*", "*")

  override def handleInlineQuery(inlineQuery: InlineQuery) = {
    val results = if (inlineQuery.query.isEmpty) Comics.lasts else Comics.search(inlineQuery.query)
    results.map(documents => {
      val results = documents.map(document =>
        (document.get[BsonInt32]("_id").get.intValue().toString, document.get[BsonString]("img").get.getValue))

      val pictures = results.map { case (id, url) => InlineQueryResultPhoto(id, url, url) }
      api.request(AnswerInlineQuery(inlineQuery.id, pictures))
    })
  }

  override def handleMessage(message: Message) = {
    message.newChatMember.filter((user) => user.id == me.id).foreach(_ => {
      Groups.insert(message.chat.id.toString)
    })

    message.leftChatMember.filter((user) => user.id == me.id).foreach(_ => {
      Groups.remove(message.chat.id.toString)
    })
  }

  /*
   * /setinlinefeedback must be enable for the bot
   */
  override def handleChosenInlineResult(chosenInlineResult: ChosenInlineResult) = {
    Comics.increaseViews(chosenInlineResult.resultId.toInt)
  }
}
