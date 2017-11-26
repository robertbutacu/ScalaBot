package example

import bot.handler.MessageHandler
import bot.memory.{BotMemory, Person}
import bot.trie.Attribute

import util.control.Breaks._
import example.brain.Manager
import example.brain.modules.AgeAttr

class Bot extends Manager with MessageHandler with BotMemory {
  def startDemo(): Unit = {
    disapprovalMessages = Set("", "", "Changed the subject...")

    println(new Person(Map(Attribute(AgeAttr, 10) -> "123",
      Attribute(AgeAttr, 15) -> "12")).toXml)

    /*breakable {
      while (true) {
        val message = scala.io.StdIn.readLine()
        if (message == "QUIT") break()
        else println(handle(masterBrain, message))
      }
    }*/
  }
}
