package me.ex0ns.inlinexkcd.database

import com.typesafe.scalalogging.Logger
import me.ex0ns.inlinexkcd.helpers.DocumentHelpers._
import me.ex0ns.inlinexkcd.models.Group
import org.mongodb.scala.Document
import org.mongodb.scala.bson._
import org.mongodb.scala.model.Filters._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ex0ns on 11/4/16.
  */
final object Groups extends Collection with Database {

  final class InvalidGroupJSON extends Exception

  override val collection = database.getCollection("groups")
  override val logger = Logger(LoggerFactory.getLogger(Groups.getClass))

  /**
    * Insert a new group to the database
    *
    * @param groupId the ID of the group
    */
  override def insert(groupId: String) : Future[Group] = {
    val document = Document("_id" -> BsonString(groupId))
    val group = document.toGroup

    if(group.isDefined)
      collection
        .insertOne(document)
        .head()
        .map(_ => group.get)
    else
      Future.failed(new InvalidGroupJSON)
  }

  /**
    * Remove a group from the database
    * @param groupId the id of the group to remove
    */
  def remove(groupId: String) =
    collection.deleteOne(equal("_id", BsonString(groupId))).toFuture()

  /**
    * Find all the documents in the collection
    * @return  all the document in the collection
    */
  def all =
    collection
      .find()
      .toFuture()
      .map((documents) => documents.flatMap(_.toGroup))

}
