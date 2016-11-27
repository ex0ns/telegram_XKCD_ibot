package me.ex0ns.inlinexkcd.database

import com.typesafe.scalalogging.Logger
import org.mongodb.scala.MongoClient

import scala.util.Properties.envOrElse

/**
  * Created by ex0ns on 6/8/16.
  */
trait Database {
  protected val URL         = envOrElse("DB_URL", "localhost")
  protected val PORT        = envOrElse("DB_PORT", "27017")

  protected val mongoClient = MongoClient(s"mongodb://$URL:$PORT")
  protected val database    = mongoClient.getDatabase("xkcd")
  protected val logger      : Logger
}
