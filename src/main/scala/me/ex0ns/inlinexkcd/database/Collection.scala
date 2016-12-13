package me.ex0ns.inlinexkcd.database

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._

/**
  * Created by ex0ns on 11/4/16.
  */
trait Collection {
  protected val DEFAULT_LIMIT_SIZE = 50
  protected val collection: MongoCollection[Document]

  /**
    * Insert a new document into the database
    *
    * @param obj the document to insert (String representation)
    */
  def insert(obj: String): Unit

  /**
    * Retrieve a document given and ID
    *
    * @param id the ID of the document to find
    * @return a document (a future containing a document)
    */
  def get(id: Int) = collection.find(equal("_id", id)).first().head()

  /**
    * Check if a document exists
    *
    * @param id the ID of the document to check
    * @return wether the document exists or not
    */
  def exists(id: Int) = collection.count(equal("_id", id)).map(l => l != 0).head()

  /**
    * Find the last DEFAULT_LIMIT_SIZE documents
    *
    * @return the last DEFAULT_LIMIT_SIZE documents
    */
  def lasts = collection.find().sort(descending("_id")).limit(DEFAULT_LIMIT_SIZE).toFuture()

  /**
    * Check if the the collection is empty
    *
    * @return a future containing a boolean regarding the emptiness of the collection
    */
  def empty = collection.count().map(_ == 0).head()
}
