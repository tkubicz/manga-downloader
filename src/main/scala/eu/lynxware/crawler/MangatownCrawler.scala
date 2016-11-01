package eu.lynxware.crawler

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.crawler.MangatownCrawler.{Chapter, Manga, Part}

import scala.io.Source

class MangatownCrawler(mangaName: String) extends LazyLogging {

  private val mangaPageLink = s"http://www.mangatown.com/manga/$mangaName/"
  private val listOfChaptersPattern = s"""(http://www.mangatown.com/manga/$mangaName/?(v\\d{1,4})?/c(\\d{1,4}|\\d{1,4}.\\d{1,4})/)""".r
  private val listOfPartsInChapterPattern = s"""(http://www.mangatown.com/manga/$mangaName/?(v\\d{1,4})?/c\\d{1,4}/\\d{1,3}.html)""".r
  private val imageLinkPattern = s"""(http://h.mangatown.com/store/manga/(?:(?!").)*)""".r

  def getManga(): Manga = Manga(mangaName, getListOfChapters().map(ch => Chapter(ch, getListOfPartsInChapter(ch).map(p => Part(p, getImageLink(p))))))

  def getListOfChapters(): Seq[String] = {
    logger.debug("downloading webpage: {}", mangaPageLink)
    val html = Source.fromURL(mangaPageLink).mkString

    logger.debug("searching for matching links")
    listOfChaptersPattern.findAllIn(html).toSeq.sorted
  }

  def getListOfPartsInChapter(chapterLink: String): Seq[String] = {
    logger.debug("downloading webpage: {}", chapterLink)
    val html = Source.fromURL(chapterLink).mkString

    logger.debug("searching for parts")
    val parts = listOfPartsInChapterPattern.findAllIn(html).toSeq
    (parts :+ chapterLink).distinct.sorted
  }

  def getImageLink(partLink: String): Option[String] = {
    logger.debug("downloading page: {}", partLink)
    val html = Source.fromURL(partLink).mkString

    logger.debug("searching for images")
    imageLinkPattern.findFirstIn(html)
  }
}

object MangatownCrawler {
  def apply(mangaName: String): MangatownCrawler = new MangatownCrawler(mangaName)

  case class Manga(name: String, chapters: Seq[Chapter])
  case class Chapter(name: String, parts: Seq[Part])
  case class Part(name: String, imageLink: Option[String])
}