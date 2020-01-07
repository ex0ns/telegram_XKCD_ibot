package me.ex0ns.inlinexkcd.database

import com.typesafe.scalalogging.Logger
import me.ex0ns.inlinexkcd.helpers.DocumentHelpers._
import me.ex0ns.inlinexkcd.models.Comic
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ex0ns on 11/4/16.
  */
final object Comics extends Collection[Comic] with Database {
  override def ct = implicitly

  final class InvalidComicJSON extends Exception
  final class DuplicatedComic extends Exception

  private val codecRegistry = fromRegistries(fromProviders(classOf[Comic]), DEFAULT_CODEC_REGISTRY)
  private val codecDB = database.withCodecRegistry(codecRegistry)

  override val collection: MongoCollection[Comic] = codecDB.getCollection("comics")
  override val logger = Logger(LoggerFactory.getLogger(Comics.getClass))

  collection
    .createIndex(
      Document("transcript" -> "text", "title" -> "text", "alt" -> "text"))
    .head()

  /**
    * Inserts a XKCD comic given its id
    *
    * @param obj the ID of the strip to insert
    */
  override def insert(obj: String) : Future[Comic] = {
    val document = Document(obj)
    val comic = document.toComic
    comic match {
      case Some(comic) =>
        collection
          .insertOne(comic)
          .head()
          .map(_ => comic)
      case None => Future.failed(new InvalidComicJSON)
    }
  }

  /**
    * Search for comics (searches in transcript, title, alt)
    *
    * @param word the search keyword
    */
  def search(word: String): Future[Seq[Comic]] = {
    collection
      .find(text(word))
      .projection(metaTextScore("score"))
      .sort(metaTextScore("score"))
      .limit(DEFAULT_LIMIT_SIZE)
      .toFuture()
  }

  /**
    * Find the ID of the last document
    * @return the document with the greater id
    */
  def lastID: Future[Comic] = collection.find().sort(descending("_id")).head()

  /**
    * Increase the number of view of one comic
    *
    * @param id the ID of the comic
    */
  def increaseViews(id: Int): Future[Comic] = {
    collection.findOneAndUpdate(equal("_id", id), inc("views", 1)).head()
  }

  def top() : Future[Seq[Comic]] =
    collection.find().sort(descending("views")).limit(5).toFuture()

}
