package me.ex0ns.inlinexkcd.database

import com.typesafe.scalalogging.Logger
import me.ex0ns.inlinexkcd.helpers.DocumentHelpers._
import org.mongodb.scala._
import org.mongodb.scala.bson.{Document => _, _}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/**
  * Created by ex0ns on 11/4/16.
  */
final object Comics extends Collection with Database {

  override val collection = database.getCollection("comics")
  override val logger = Logger(LoggerFactory.getLogger(Comics.getClass))

  collection.createIndex(Document("transcript" -> "text", "title" -> "text", "alt" -> "text")).head()

  /**
    * Inserts a XKCD comic given its id
    *
    * @param obj the ID of the strip to insert
    */
  override def insert(obj: String) = {
    val strip = Document(obj)
    strip.get[BsonInt32]("num") match {
      case Some(x) => collection.insertOne(strip + ("_id" -> x)).head()
      case None => logger.warn("No num column found")
    }
  }

  /**
    * Search for comics (searches in transcript, title, alt)
    *
    * @param word the search keyword
    */
  def search(word: String) : Future[Seq[Document]] = {
    collection.find(text(word)).sort(descending("_id")).limit(DEFAULT_LIMIT_SIZE).toFuture()
  }

  /**
    * Find the ID of the last document
    * @return the document with the greater id
    */
  def lastID = collection.find().sort(descending("_id")).head().map(_.toComic)

  /**
    * Increase the number of view of one comic
    *
    * @param id the ID of the comic
    */
  def increaseViews(id: Int): Future[Document] = {
    collection.findOneAndUpdate(equal("_id", id), inc("views", 1)).head()
  }

}
