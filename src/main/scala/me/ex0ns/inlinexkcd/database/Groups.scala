package me.ex0ns.inlinexkcd.database

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.Logger
import me.ex0ns.inlinexkcd.models.Group
import org.mongodb.scala.bson._
import org.mongodb.scala.model.Filters._
import org.slf4j.LoggerFactory
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.types.ObjectId
import org.mongodb.scala.MongoCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by ex0ns on 11/4/16.
  */
final object Groups extends Collection[Group] with Database {
  override def ct = implicitly

  final class InvalidGroupJSON extends Exception
  private val codecRegistry = fromRegistries(fromProviders(classOf[Group]), DEFAULT_CODEC_REGISTRY )
  private val codecDB = database.withCodecRegistry(codecRegistry)

  override val collection: MongoCollection[Group] = codecDB.getCollection("groups")
  override val logger = Logger(LoggerFactory.getLogger(Groups.getClass))

  /**
    * Insert a new group to the database
    *
    * @param groupId the ID of the group
    */
  override def insert(groupId: String) : Future[Group] = {
    val group: Group = Group(groupId.toLong) // Handle failure

    collection
      .insertOne(group)
      .head()
      .map(_ => group)
  }

  /**
    * Remove a group from the database
    * @param groupId the id of the group to remove
    */
  def remove(groupId: String) =
    collection.deleteOne(equal("id", groupId.toLong)).toFuture()

  /**
    * Find all the documents in the collection
    * @return  all the document in the collection
    */
  def all =
    collection
      .find()
      .toFuture()
      .map((documents) => documents)

}
