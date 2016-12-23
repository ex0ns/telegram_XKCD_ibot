package me.ex0ns.inlinexkcd.models

import info.mukel.telegrambot4s.api.TelegramApiAkka
import info.mukel.telegrambot4s.methods.{ParseMode, SendMessage, SendPhoto}
import me.ex0ns.inlinexkcd.helpers.StringHelpers._
/**
  * Created by ex0ns on 12/13/16.
  */
final case class Comic(_id: Int,
                       title: String,
                       img: String,
                       num: Int,
                       alt: Option[String],
                       link: Option[String])
  extends Notification {
  def getBoldTitle: String = title.bold

  def getText: String =
    alt.getOrElse("") +
      link.map(x => s"\n\n$x").getOrElse("") +
      s"\n\nhttps://explainxkcd.com/$num"

  override def notify(group: Group)(implicit api: TelegramApiAkka): Unit = {
    api.request(SendMessage(Left(group._id), title, Some(ParseMode.Markdown)))
    Thread.sleep(MESSAGE_ORDER_DELAY)
    api.request(SendPhoto(Left(group._id), Right(img)))
    Thread.sleep(MESSAGE_ORDER_DELAY)
    api.request(
      SendMessage(Left(group._id),
        getText,
        Some(ParseMode.Markdown),
        Some(true)))
  }
}
