package bot.trie

import bot.learn.{PossibleReply, SearchResponses}

import scala.annotation.tailrec
import scala.util.matching.Regex

/**
  *
  * @param curr     - current node, containing a word( or a regex), and an optional attribute
  *                 which is to be stored in map if necessary
  *                 Regex             - current word , part of the message
  *                 Option[Attribute] - in case it is something to be remembered
  * @param children - next parts of a possible message from a client.
  * @param replies  - replies to the message starting from the top of the trie all the way down to curr.
  *                 Option[() => Set[String]    - a function returning a possible last bot message
  *                 Set[f: Any => Set[String] ] - a set of functions returning a set of possible replies
  *                 => It is done that way so that the replies are generated dynamically,
  *                 depending on the already existing/non-existing attributes.
  **/
class SpeakingKnowledge(val curr: (Regex, Option[Attribute]) = ("".r, None),
                        val children: Set[SpeakingKnowledge] = Set[SpeakingKnowledge]().empty,
                        val replies: Set[PossibleReply] = Set.empty)
  extends TrieOperations {

  /**
    * For a current Trie, the algorithm returns another trie with the message added.
    * The pattern matching does the following:
    *   1. in case of None => current word isn't in the set => add it, and call the function with the same node
    *   2. in case of Some(next) => next node has been found => remove it from the Set, since its gonna be different 
    * it would double stack otherwise
    *
    * @param message - list of words to be added into the trie
    * @param replies - a set of functions which return a set of possible replies
    * @return - a new trie with the new message included
    */
  final def add(message: List[Word],
                replies: PossibleReply): SpeakingKnowledge = {

    def go(curr: SpeakingKnowledge, words: List[Word]): SpeakingKnowledge = {
      if (words.isEmpty) //went through all the list
        curr.addReplies(replies) // adding the replies to the Set
      else {
        curr.children.find(n => isMatching(n.curr, words.head)) match {
          case None => go(curr.addValue(SpeakingKnowledge(words.head)), words)
          case Some(next) => SpeakingKnowledge(curr.curr,
            curr.children - next ++ Set(go(next, words.tail)),
            curr.replies)
        }
      }
    }

    go(this, message)
  }


  /**
    * The algorithm describes the search of a message in a trie, by parsing every word and matching it,
    * thus returning a Set of possible replies depending on bot's previous replies.
    *
    * @param message - the sentence that is to be found, or not
    * @return - returns a Set of (previousMessageFromBot, Set[functions returning possible replies]),
    *         from which another algorithm will pick the best choice.
    */
  final def search(message: List[Word]): SearchResponses = {
    @tailrec
    def go(message: List[Word], trie: SpeakingKnowledge,
           attributes: Map[Attribute, String]): SearchResponses = {
      if (message.isEmpty)
        SearchResponses(attributes, trie.replies) //completely ran over all the words
      else {
        val head = message.head
        val next = trie.children.find(t => isMatching(t, head))
        next match {
          case None           => SearchResponses(attributes, Set()) //word wasn't found in the trie
          case Some(nextNode) => go(message.tail, nextNode,
            nextNode.curr._2 match {
              case None       => attributes
              case Some(attr) => attributes + (attr -> head._1.regex)
            })
        }
      }
    }

    go(message, this, Map[Attribute, String]().empty)
  }

  def print(): Unit = {
    def go(trie: SpeakingKnowledge, tabs: Int): Unit = {
      println("\t" * tabs + "Node " + trie.curr + "   ")
      trie.replies.foreach(r => println("\t" * (tabs + 1) + " Leaf " + r))
      trie.children.foreach(t => go(t, tabs + 1))
    }

    go(this, 0)
  }

  /**
    * @param replies - replies that are to be added
    * @return     - new leafs which also contain the new replies
    *             There are 2 cases:
    *             1. when they depend on a previous bot message ( or lack of) also stored:
    *             the replies are appended to the already existing replies.
    *             2. when they aren't stored at all:
    *             they are registered as new replies with their attribute.
    *
    */
  private def addReplies(replies: PossibleReply): SpeakingKnowledge =
    this.replies.find(l => l.previousBotMessage == replies.previousBotMessage) match {
      case None      => SpeakingKnowledge(this.curr, this.children, this.replies ++ Set(replies))
      case Some(rep) => addReplies(rep, replies)
    }


  private def addReplies(to: PossibleReply, newReplies: PossibleReply) =
    SpeakingKnowledge(this.curr, this.children,
      this.replies -- Set(to) + PossibleReply(to.previousBotMessage, to.possibleReply ++ newReplies.possibleReply))

  private def addValue(node: SpeakingKnowledge): SpeakingKnowledge =
    SpeakingKnowledge(curr, children ++ Set(node), replies)
}

object SpeakingKnowledge {
  def apply(curr: (Regex, Option[Attribute]) = ("".r, None),
            children: Set[SpeakingKnowledge] = Set[SpeakingKnowledge]().empty,
            replies: Set[PossibleReply] = Set.empty): SpeakingKnowledge =
    new SpeakingKnowledge(curr, children, replies)
}

