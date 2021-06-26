package me.ex0ns.inlinexkcd.models

import org.mongodb.scala.bson.ObjectId

/**
  * Created by ex0ns on 12/13/16.
  */
object Group {
  def apply(id: Long): Group = Group(new ObjectId(), id)
}
final case class Group(_id: ObjectId, id: Long)
