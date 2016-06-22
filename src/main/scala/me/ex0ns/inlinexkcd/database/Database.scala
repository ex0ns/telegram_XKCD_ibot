package me.ex0ns.inlinexkcd.database

import com.typesafe.scalalogging.Logger
import org.mongodb.scala.bson._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.{Document, MongoClient, MongoCollection}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * Created by ex0ns on 6/8/16.
  */
class Database {
  private val mongoClient = MongoClient()
  private val database = mongoClient.getDatabase("xkcd")
  private val collection: MongoCollection[Document] = database.getCollection("comics")
  private val logger = Logger(LoggerFactory.getLogger(classOf[Database]))

  Seq(
    collection.createIndex(Document("title" -> "text")),
    collection.createIndex(Document("alt" -> "text")),
    collection.createIndex(Document("transcript" -> "text"))
  ).foreach(_.head())


  /**
    * Inserts a XKCD comic given its id
    *
    * @param obj the ID of the strip to insert
    */
  def insert(obj: String) = {
    val strip = Document(obj)
    strip.get[BsonInt32]("num") match {
      case Some(x) => collection.insertOne(strip + ("_id" -> x)).head()
      case None => logger.warn("No num column found")
    }
  }

  /**
    * Retrieve a XKCD comic given an ID
    *
    * @param id the id of the comic to get
    */
  def get(id: Int) : Future[Document] = {
    collection.find(equal("_id", id)).first().head()
  }

  /**
    * Check if a comic already is in our database
    *
    * @param id the id of the comic to check for
    */
  def exists(id: Int) : Future[Boolean] = {
    collection.count(equal("_id", id)).map(l => l != 0).head()
  }

  /**
    * Search for comics
    *
    * @param word list of words to search
    */
  def search(word: String) : Future[Seq[Document]] = {
    collection.find(text(word)).sort(descending("year", "month")).toFuture()
  }

  /**
    * Get the last ID of the inserted comic
    * @return the last ID in the database
    */
  def lastID(): Future[Document] = {
    collection.find().sort(descending("_id")).head()
  }

}
