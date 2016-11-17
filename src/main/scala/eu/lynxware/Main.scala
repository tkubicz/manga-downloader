package eu.lynxware

import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Executors

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.crawler.MangatownCrawler
import eu.lynxware.epub.file.OpfMetadata
import eu.lynxware.epub.{Epub, EpubWriter}
import eu.lynxware.util.{FileDownloader, FileUtils}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main extends App with LazyLogging {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))

  val chapterNamePattern = s"(c(\\d{1,4}|\\d{1,4}.\\d{1,4}))".r
  val partNamePattern = s"(\\d{1,4}.html)".r
  val mangaName = "berserk"
  val mangaPath = "/Users/tku/Downloads/berserk/"

  val metadata = OpfMetadata()
    .withTitle("Berserk")
    .withCreator("MangaDownloader")
    .withLanguage("en")

  val epub = new Epub()
    .withMetadata(metadata)
    //.addJpegImage(Paths.get(mangaPath).resolve("c001/1.html.jpg"), "c001_1")
    //.addJpegImage(Paths.get(mangaPath).resolve("c001/2.html.jpg"), "c001_2")
    //.addJpegImage(Paths.get(mangaPath).resolve("c001/3.html.jpg"), "c001_3")

  val epubWriter = new EpubWriter()
  epubWriter.write(epub,
    FileUtils.homeDirectory.resolve("Pobrane").resolve("result.epub"),
    FileUtils.homeDirectory.resolve("Pobrane").resolve("test"))

  def downloadManga(mangaName: String): Unit = {
    val crawler = MangatownCrawler(mangaName)
    val chapters = crawler.getListOfChapters
    val result = chapters.drop(182).take(20).map { ch =>
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
              FileDownloader.downloadFile(imageLink, file)
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
