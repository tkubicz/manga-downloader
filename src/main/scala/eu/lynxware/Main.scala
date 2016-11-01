package eu.lynxware

import java.io.File
import java.util.concurrent.Executors

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.crawler.MangatownCrawler

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main extends App with LazyLogging {

  import eu.lynxware.util.FileDownloader._

  val chapterNamePattern = s"(c(\\d{1,4}|\\d{1,4}.\\d{1,4}))".r
  val partNamePattern = s"(\\d{1,4}.html)".r

  val mangaName = "berserk"

  val crawler = MangatownCrawler(mangaName)

  val mangaPath = "/home/tku/Pobrane/berserk/"

  val chapters = crawler.getListOfChapters

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

  val result = chapters.take(2).map { ch =>
    val chapterName = extractChapterName(ch) match {
      case Some(name) => name
      case None => throw new RuntimeException
    }

    logger.debug("Chapter path: " + mangaPath + chapterName)
    val dir = new File(mangaPath + chapterName)
    if (!dir.mkdirs()) {
      throw new RuntimeException("Could not create directory")
    }

    val f = Future {
      crawler.getListOfPartsInChapter(ch).foreach { part =>
        crawler.getImageLink(part) match {
          case Some(imageLink) =>
            logger.debug("part: {}, imageLink: {}", part, imageLink)
            val partName = extractPartName(part) match {
              case Some(p) => p
              case None => "1"
            }
            val file = new File(dir.getAbsolutePath + "/" + partName + ".jpg")
            downloadFile(imageLink, file)
          case None => logger.error("No image link")
        }
      }
      ch
    }
    f.onComplete {
      case Success(e) => updateProgress(e)
    }

    f
  }

  Future.sequence(result).onComplete {
    case Success(e) => downloadFinished()
    case Failure(e) => logger.error("Something went wrong", e)
  }

  def extractChapterName(link: String): Option[String] = {
    chapterNamePattern.findFirstIn(link)
  }


  def extractPartName(link: String): Option[String] = {
    partNamePattern.findFirstIn(link)
  }

  def downloadFinished(): Unit = {
    logger.info("Download has finished")
  }

  def updateProgress(chapterFinished: String): Unit = {
    logger.info("Chapter {} has been downloaded", chapterFinished)
  }
}
