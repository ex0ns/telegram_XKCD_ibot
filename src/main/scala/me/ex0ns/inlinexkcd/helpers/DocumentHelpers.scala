package me.ex0ns.inlinexkcd.helpers

import me.ex0ns.inlinexkcd.models.{Comic, Group}
import org.mongodb.scala._
import org.mongodb.scala.bson.{Document => _, _}

/**
  * Created by ex0ns on 12/13/16.
  */
object DocumentHelpers {
  /* We are currently waiting for the case class support of mongoDB driver
     See https://jira.mongodb.org/browse/SCALA-168
   */
  implicit class DocumentConverter(document: Document) {

    def toGroup : Option[Group] = {
      for {
        id <- document.get[BsonString]("_id").map(_.getValue())
      } yield Group(id.toLong)
    }

    private def stringToOption(s: String) =  if(s.nonEmpty) Some(s)  else None

    def toComic : Option[Comic] = {
      for {
        img <- document.get[BsonString]("img").map(_.getValue())
        title <- document.get[BsonString]("title").map(_.getValue())
        num <- document.get[BsonInt32]("num").map(_.intValue())
      } yield  {
        val alt   = document.get[BsonString]("alt").flatMap(x => stringToOption(x.getValue))
        val link  = document.get[BsonString]("link").flatMap(x => stringToOption(x.getValue))
        Comic(num, title, img, num, alt, link)
      }
    }
  }
}
