package example

import bot.handler.MessageHandler
import bot.memory.{BotMemory, Person}
import bot.trie.{Attribute, Trie}
import example.brain.Manager
import example.brain.modules.{AgeAttr, JobAttr, NameAttr, PassionAttr}


class Bot extends Manager with MessageHandler with BotMemory {
  def startDemo(): Unit = {
    def go(botLog: List[String] = List.empty,
           humanLog: List[String] = List.empty): Unit = {
      val message = scala.io.StdIn.readLine()
      if (message == "QUIT") {

      }
      else {
        val updatedHumanLog = humanLog :+ message
        val updatedBotLog = handle(masterBrain, message, updatedHumanLog, botLog)
        println(updatedBotLog.last)

        go(botLog, humanLog)
      }
    }

    val peopleXML = remember("out.xml")

    val people = peopleXML map translate map(e => e.flatten.toMap)

    println(people)

    /*println(tryMatch(
      List(Map(Attribute(AgeAttr, 10) -> "123",
        Attribute(AgeAttr, 15) -> "12",
        Attribute(AgeAttr, 14) -> "14"),
        Map(Attribute(AgeAttr, 10) -> "123",
          Attribute(AgeAttr, 15) -> "12",
          Attribute(AgeAttr, 14) -> "13")),
      Map(Attribute(AgeAttr, 15) -> "12",
        Attribute(AgeAttr, 14) -> "13").toList,
      15
    ))*/
    /*breakable {
      while (true) {

      }
    }*/
  }

  /** The triple represents:
    * _1 : Attribute name
    * _2 : Attribute weight
    * _3 : Attribute value
    *
    * @param people - a list where every single element represent a person with all their traits
    * @return - every item from the list converted to a map of Attribute, String
    */
  override def translate(people: List[(String, String, String)]): List[Map[Attribute, String]] = {
    val applier: PartialFunction[(String, String, String), Map[Attribute, String]] = {
      case ("AgeAttr", weight, ageValue)  => Map(Attribute(AgeAttr, weight.toInt) -> ageValue)
      case ("NameAttr", weight, nameValue) => Map(Attribute(NameAttr, weight.toInt) -> nameValue)
      case ("PassionAttr", weight, passionValue) => Map(Attribute(PassionAttr, weight.toInt) -> passionValue)
      case ("Job", weight, jobValue) => Map(Attribute(JobAttr, weight.toInt) -> jobValue)
    }

    people filter applier.isDefinedAt map applier
  }

  override def disapprovalMessages: Set[String] = Set("", "", "Changed the subject...")

  override def unknownHumanMessages: Set[String] = Set("")
}
