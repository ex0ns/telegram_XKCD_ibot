package me.ex0ns.inlinexkcd.parser

import com.typesafe.scalalogging.Logger
import fr.hmil.scalahttp.client.HttpRequest
import me.ex0ns.inlinexkcd.database.Comics
import me.ex0ns.inlinexkcd.database.Comics.DuplicatedComic
import me.ex0ns.inlinexkcd.models.Comic
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
  * Created by ex0ns on 6/8/16.
  */
class XKCDHttpParser {

  private val logger = Logger(LoggerFactory.getLogger(classOf[XKCDHttpParser]))

  /**
    * Fetch XKCD comic based on its ID
    *
    * @param id the ID of the strip to fetch
    * @return a future that may contain the HttpResponse (if successful)
    */
  def parseID(id: Int): Future[Comic] = {
    val document = Comics.exists(id)
    document.flatMap {
      case true =>
        logger.debug(s"Document with id: $id already exists")
        Future.failed(new DuplicatedComic) // we do not want to stop at the first item we have in the DB
      case false =>
        HttpRequest(s"http://xkcd.com/$id/info.0.json")
          .send()
          .flatMap(comic => Comics.insert(comic.body))
    }
  }

  /**
    * Convert a Future[T] to Future[Try] to be able to count the number of failed/successful Future
    * See http://stackoverflow.com/questions/20874186/scala-listfuture-to-futurelist-disregarding-failed-futures
    * @param f The future to convert
    * @return A future of Try
    */
  private def futureToFutureTry[T](f: Future[T]): Future[Try[T]] =
    f.map(Success(_))
      .recover({
        case e => logger.warn(e.toString); Failure(e)
      })

  /**
    * Fetch in parallel size pages, starting from startingPage, and returns the number of failed Future
    * @param startingPage Index of the starting page
    * @param size The size of the range
    * @return the number of failed Future
    */
  private def bulkFetch(startingPage: Int, size: Int) = {
    val t = Range(startingPage, startingPage + size)
      .take(size)
      .map(parseID)
      .map(futureToFutureTry)
    val r = Await.result(Future.sequence(t), Duration.Inf)
    r.count(_.isFailure)
  }

  /**
    * Fetch and parse all XKCD comics
    * @param step The number of pages to fetch in parallel
    */
  def parseAll(step: Int = 10) = {
    Stream
      .from(0, step)
      .map(x => bulkFetch(x, step))
      .takeWhile(_ != step)
      .force
  }

}
