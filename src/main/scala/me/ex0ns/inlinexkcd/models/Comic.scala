package me.ex0ns.inlinexkcd.models


import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.methods.{ParseMode, SendMessage, SendPhoto}
import com.bot4s.telegram.models.InputFile
import me.ex0ns.inlinexkcd.helpers.StringHelpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final case class Comic(_id: Int,
                       title: String,
                       img: String,
                       num: Int,
                       alt: Option[String],
                       link: Option[String],
                       views: Option[Int])
  extends Notification {
  def getBoldTitle: String = title.bold

  def getText: String =
    alt.getOrElse("") +
      link.map(x => s"\n\n$x").getOrElse("") +
      s"\n\nhttps://explainxkcd.com/$num"

  override def notify(group: Group)(implicit request: RequestHandler[Future]): Future[Unit] = {
    for {
      response <- Future { scalaj.http.Http(img).asBytes }
      if response.isSuccess
      bytes = response.body
      photo = InputFile(img.split('/').last, bytes)
      _ <- request(SendMessage(group.id, getBoldTitle, Some(ParseMode.Markdown)))
      _ <- request(SendPhoto(group.id, photo))
      _ <- request(
        SendMessage(group.id,
          getText,
          Some(ParseMode.Markdown),
          disableWebPagePreview = Some(true)))
    } yield ()
  }
}
