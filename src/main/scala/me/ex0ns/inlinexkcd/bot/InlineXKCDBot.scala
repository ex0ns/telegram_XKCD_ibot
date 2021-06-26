package me.ex0ns.inlinexkcd.bot

import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods._
import com.bot4s.telegram.models._
import fs2.Stream
import sttp.client3._
import sttp.client3.asynchttpclient.future.AsyncHttpClientFutureBackend
import me.ex0ns.inlinexkcd.database.Comics.DuplicatedComic
import me.ex0ns.inlinexkcd.database.{Comics, Groups}
import me.ex0ns.inlinexkcd.helpers.StringHelpers._
import me.ex0ns.inlinexkcd.models.{Comic, Group}
import me.ex0ns.inlinexkcd.parser.XKCDHttpParser

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import cats.effect.IO
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import cats.effect.unsafe.implicits.global

class InlineXKCDBot(val token: String) extends TelegramBot with Commands[Future] with Polling  {

  implicit val backend: SttpBackend[Future, Any] = AsyncHttpClientFutureBackend()
  override val client: RequestHandler[Future] = new FutureSttpClient(token)

  private val me = Await.result(request(GetMe), Duration.Inf)
  private val parser = new XKCDHttpParser()

  logger.debug("Bot is up and running !")

  def parseComic(notify: Boolean = false): Future[Either[Exception, Comic]] = {
    val nextComic = for {
      comic <- Comics.lastID
      newComic <- parser.parseID(comic._id + 1)
    } yield newComic

    nextComic.flatMap {
      case Right(comic) =>
        if(notify) comic.notifyAllGroups(request).flatMap(_ => parseComic(notify))
        else parseComic(notify)
      case Left(_ : DuplicatedComic) => parseComic(notify)
      case x @ Left(_) => Future.successful(x)
      case _ =>
        logger.error("An unknown error happened, interrupting parsing")
        Future.successful(Left(new Exception("An unknown error happened, interrupting parsing")))
    }
  }

  Comics.empty.map {
    case true => parser.parseAll()
    case false => parseComic() // Parse comics we could have missed
  }

  val cronScheduler = Cron4sScheduler.systemDefault[IO]
  val evenSeconds = Cron.unsafeParse("0 */15 9-23 * * ?")

  val startParse = Stream.eval(IO(logger.debug("Calling parse")) *> IO(parseComic(true)))
  val scheduled = cronScheduler.awakeEvery(evenSeconds) >> startParse

  scheduled.compile.drain.unsafeRunAndForget()

  override def receiveInlineQuery(inlineQuery: InlineQuery): Future[Unit] = {
    val superFuture = super.receiveInlineQuery(inlineQuery)
    val results =
      if (inlineQuery.query.isEmpty) Comics.lasts
      else Comics.search(inlineQuery.query)

    results.foreach(documents => {
      val pictures = documents.map(comic => {
        InlineQueryResultPhoto(comic._id.toString, comic.img, comic.img)
      })
      request(AnswerInlineQuery(inlineQuery.id, pictures))
    })

    superFuture
  }

  override def receiveMessage(message: Message): Future[Unit] = {
    val superFuture = super.receiveMessage(message)
    message.newChatMembers.foreach(users => users
      .filter((user) => user.id == me.id)
      .foreach(_ => {
        Groups.insert(message.chat.id.toString)
      })
    )

    message.leftChatMember
      .filter((user) => user.id == me.id)
      .foreach(_ => {
        Groups.remove(message.chat.id.toString)
      })

    message.text.foreach {
      case "/start" => Groups.insert(message.chat.id.toString)
      case "/stop" => Groups.remove(message.chat.id.toString)
      case "/debug" => Comics.top().map(comics =>
        comics.headOption match {
          case Some(comic) => comic.notify(Group(23835217))(request)
          case _ =>
        })
      case "/stats" =>
        Comics.top().map(cs => {
          val text = cs.map(c => s"${c.title.altWithUrl(c.img)} - ${c.views} views\n").mkString
          request(SendMessage(message.chat.id, "Top 5 comics".bold + "\n" + text, Some(ParseMode.Markdown), disableWebPagePreview = Some(true)))
        })
    }
    superFuture
  }

  /*
   * /setinlinefeedback must be enable for the bot
   */
  override def receiveChosenInlineResult(
                                     chosenInlineResult: ChosenInlineResult) = {
    Comics.increaseViews(chosenInlineResult.resultId.toInt).map(_ => ())
  }
}
