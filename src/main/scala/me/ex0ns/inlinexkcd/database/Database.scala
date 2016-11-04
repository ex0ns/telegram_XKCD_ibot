package me.ex0ns.inlinexkcd.database

import com.typesafe.scalalogging.Logger
import org.mongodb.scala.{Document, MongoClient, MongoCollection}

/**
  * Created by ex0ns on 6/8/16.
  */
trait Database {
  protected val mongoClient = MongoClient()
  protected val database    = mongoClient.getDatabase("xkcd")
  protected val logger      : Logger
}
