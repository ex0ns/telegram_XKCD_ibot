package me.ex0ns.inlinexkcd.helpers

import me.ex0ns.inlinexkcd.models.Comic
import org.mongodb.scala._
import org.mongodb.scala.bson.{Document => _, _}

object DocumentHelpers {
  implicit class DocumentConverter(document: Document) {

    private def stringToOption(s: String) = if (s.nonEmpty) Some(s) else None

    def toComic: Option[Comic] = {
      for {
        img <- document.get[BsonString]("img").map(_.getValue())
        title <- document.get[BsonString]("title").map(_.getValue())
        num <- document.get[BsonInt32]("num").map(_.intValue())
      } yield {
        val alt = document
          .get[BsonString]("alt")
          .flatMap(x => stringToOption(x.getValue))
        val link = document
          .get[BsonString]("link")
          .flatMap(x => stringToOption(x.getValue))
        val views = document
          .get[BsonNumber]("views")
          .flatMap(x => { Some(x.intValue()) })
        Comic(num, title, img, num, alt, link, views)
      }
    }
  }
}
