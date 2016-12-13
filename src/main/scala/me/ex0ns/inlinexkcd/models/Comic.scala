package me.ex0ns.inlinexkcd.models

/**
  * Created by ex0ns on 12/13/16.
  */
final case class Comic(_id: Int, title: String, img: String, num: Int, alt: Option[String], link: Option[String]) {
  def getText : String = alt.getOrElse("") + link.map(x => s"\n\n$x").getOrElse("")
}
