package me.ex0ns.inlinexkcd.database

import com.typesafe.scalalogging.Logger
import me.ex0ns.inlinexkcd.helpers.DocumentHelpers._
import me.ex0ns.inlinexkcd.models.Group
import org.mongodb.scala.Document
import org.mongodb.scala.bson._
import org.mongodb.scala.model.Filters._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by ex0ns on 11/4/16.
  */
final object Groups extends Collection with Database {

  override val collection = database.getCollection("groups")
  override val logger = Logger(LoggerFactory.getLogger(Groups.getClass))

  private val MESSAGES_LIMIT_TIME = 1000
  private val MESSAGES_LIMIT = 30

  /**
    * Insert a new group to the database
    *
    * @param groupId the ID of the group
    */
  override def insert(groupId: String) = {
    val group = Document("_id" -> BsonString(groupId))
    collection.insertOne(group).head()
  }

  /**
    * Remove a group from the database
    * @param groupId the id of the group to remove
    */
  def remove(groupId: String) = collection.deleteOne(equal("_id", BsonString(groupId))).toFuture()

  /**
    * Find all the documents in the collection
    * @return  all the document in the collection
    */
  def all = collection.find().toFuture().map((documents) => documents.flatMap(_.toGroup))

  def notifyAllGroups(notify : (Group) => Unit) = Groups.all.map((groups) => {
    groups.grouped(MESSAGES_LIMIT).foreach((groupCluster) => {
      groupCluster.foreach(notify)
      Thread.sleep(MESSAGES_LIMIT_TIME)
    })
  })


}
