package me.ex0ns.inlinexkcd.bot

import cronish.Cron
import cronish.dsl._
import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models._
import me.ex0ns.inlinexkcd.database.Comics.DuplicatedComic
import me.ex0ns.inlinexkcd.database.{Comics, Groups}
import me.ex0ns.inlinexkcd.helpers.DocumentHelpers._
import me.ex0ns.inlinexkcd.parser.XKCDHttpParser

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.{Failure, Properties, Success}

/**
  * Created by ex0ns on 06/08/16.
  */
object InlineXKCDBot extends TelegramBot with Commands with Polling  {

  override def token =
    Properties
      .envOrNone("TELEGRAM_KEY")
      .getOrElse(Source.fromFile("telegram.key").getLines().next)

  private val me = Await.result(request(GetMe), Duration.Inf)
  private val parser = new XKCDHttpParser()

  logger.debug("Bot is up and running !")

  def parseComic(notify: Boolean = false): Unit = {
    Comics.lastID onSuccess {
      case Some(comic) =>
        parser.parseID(comic._id + 1) onComplete {
          case Success(newComic) if notify =>
            newComic.notifyAllGroups(request)
            parseComic(notify)
          case Failure(_ : DuplicatedComic) => parseComic(notify)
          case _ => logger.error("An unknown error happened, interrupting parsing")
        }
    }
  }

  Comics.empty onSuccess {
    case true => parser.parseAll()
    case false => parseComic() // Parse comics we could have missed
  }

  task(parseComic(true)) executes Cron("00", "*/15", "9-23", "*", "*", "*", "*")

  override def onInlineQuery(inlineQuery: InlineQuery) = {
    val results =
      if (inlineQuery.query.isEmpty) Comics.lasts
      else Comics.search(inlineQuery.query)

    results.foreach(documents => {
      val pictures = documents.flatMap(_.toComic).map(comic => {
        InlineQueryResultPhoto(comic._id.toString, comic.img, comic.img)
      })
      request(AnswerInlineQuery(inlineQuery.id, pictures))
    })
  }

  override def onMessage(message: Message) = {
    message.newChatMember
      .filter((user) => user.id == me.id)
      .foreach(_ => {
        Groups.insert(message.chat.id.toString)
      })

    message.leftChatMember
      .filter((user) => user.id == me.id)
      .foreach(_ => {
        Groups.remove(message.chat.id.toString)
      })
  }

  /*
   * /setinlinefeedback must be enable for the bot
   */
  override def onChosenInlineResult(
                                         chosenInlineResult: ChosenInlineResult) = {
    Comics.increaseViews(chosenInlineResult.resultId.toInt)
  }
}
