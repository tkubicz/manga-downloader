package eu.lynxware

import java.io.File
import java.util.concurrent.{ExecutorService, Executors, ThreadPoolExecutor}

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

object Main extends App with LazyLogging {

  import FileDownloader._

  val chapterNamePattern = s"(c(\\d{1,4}|\\d{1,4}.\\d{1,4}))".r
  val partNamePattern = s"(\\d{1,4}.html)".r

  val mangaName = "berserk"
  val crawler = MangatownCrawler(mangaName)

  //val manga = crawler.getManga()

  val mangaPath = "/home/tku/Pobrane/berserk/"

  val chapters = crawler.getListOfChapters
  val parts = crawler.getListOfPartsInChapter(chapters(0))
  val maybeImageLink = crawler.getImageLink(parts(0))


  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

  chapters.take(10).foreach { ch =>
    val chapterName = extractChapterName(ch) match {
      case Some(name) => name
      case None => throw new RuntimeException
    }

    logger.debug("Chapter path: " + mangaPath + chapterName )
    val dir = new File(mangaPath + chapterName)
    if (!dir.mkdirs()) {
      throw new RuntimeException("Could not create directory")
    }

    Future {
      crawler.getListOfPartsInChapter(ch).foreach { part =>
        crawler.getImageLink(part) match {
          case Some(imageLink) =>
            logger.info("part: {}, imageLink: {}", part, imageLink)
            val partName = extractPartName(part) match {
              case Some(p) => p
              case None => "1"
            }
            val file = new File(dir.getAbsolutePath + "/" + partName + ".jpg")
            downloadFile(imageLink, file)
          case None => logger.error("No image link")
        }
      }
    }
  }

  def extractChapterName(link: String): Option[String] = {
    chapterNamePattern.findFirstIn(link)
  }


  def extractPartName(link: String): Option[String] = {
    partNamePattern.findFirstIn(link)
  }

  /*maybeImageLink match {
    case Some(imageLink) => {
      logger.debug("image link: " + imageLink)
      logger.debug("downloading file")
      val file = new File("/home/tku/Pobrane/test.jpg")
      downloadFile(imageLink, file)
      logger.debug("file downloaded")
    }

    case None => logger.error("Could not find image link")
  }*/
}
