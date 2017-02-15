package me.ex0ns.inlinexkcd.models

import info.mukel.telegrambot4s.api.RequestHandler
import me.ex0ns.inlinexkcd.database.Groups

import scala.concurrent.ExecutionContext.Implicits.global

trait Notification {

  val MESSAGES_LIMIT        = 30
  val MESSAGE_ORDER_DELAY   = 200
  val MESSAGES_LIMIT_TIME   = 1000

  def notify(group: Group)(implicit request: RequestHandler): Unit
  def notifyAllGroups(implicit request: RequestHandler) : Unit =
    Groups.all.map((groups) => {
      groups
        .grouped(MESSAGES_LIMIT)
        .foreach((groupCluster) => {
          groupCluster.foreach(notify)
          Thread.sleep(MESSAGES_LIMIT_TIME)
        })
    })

}
