package me.ex0ns.inlinexkcd.helpers

import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonString

/**
  * Created by thibault on 13.12.16.
  */
object DocumentHelpers {

  implicit class AdvancedDocument(document: Document) {
    def getFields(fields: String*) : Seq[String] = {
      fields.flatMap(document.get[BsonString](_).map(_.getValue))
    }
  }
}
