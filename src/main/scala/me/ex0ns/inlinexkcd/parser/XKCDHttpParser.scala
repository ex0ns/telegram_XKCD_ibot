package me.ex0ns.inlinexkcd.parser

import com.typesafe.scalalogging.Logger
import fr.hmil.scalahttp.client.HttpRequest
import me.ex0ns.inlinexkcd.database.Database
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by ex0ns on 6/8/16.
  */
class XKCDHttpParser {

  private val MAX_CONTIGUOUS_FAILURE = 10
  private val logger = Logger(LoggerFactory.getLogger(classOf[XKCDHttpParser]))
  private val database = new Database()
  private val listOfFutures = ArrayBuffer[Future[_]]()

  /**
    * Fetch XKCD comic based on its ID
    *
    * @param id the ID of the strip to fetch
    */
  def parseID(id: Int) : Future[_] = {
    val document = database.exists(id)
    document.flatMap {
      case true =>
        logger.debug(s"Document with id: $id already exists")
        Future.successful(true) // we do not want to stop at the first item we have in the DB
      case false =>
        HttpRequest(s"http://xkcd.com/$id/info.0.json").send().map(httpResponse => {
          database.insert(httpResponse.body)
        })
    }
  }


  /**
    * Fetch and parse all XKCD comics
    */
  def parseAll() = {
    def handler(id: Int, failures: Int = 0): Unit = {
      if(failures > MAX_CONTIGUOUS_FAILURE) {
        logger.debug(s"Reached $failures contiguous error, shutting down")
        return
      }
      val download = parseID(id)
      listOfFutures += download
      download onComplete   {
        case Success(_) => handler(id+1, 0)
        case Failure(e) =>
          logger.warn(e.toString)
          handler(id+1, failures+1)
      }
    }

    //@TODO: make queries run in parallel
    handler(1)

    while(listOfFutures.nonEmpty) {
      println(listOfFutures.size)
      listOfFutures.foreach {
        future =>
          if(future.isCompleted) {
            listOfFutures -= future
          }
      }
      Thread.sleep(500)
    }

  }

}
