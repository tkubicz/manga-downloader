package eu.lynxware

import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Executors

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.crawler.MangatownCrawler
import eu.lynxware.epub.file.{OpfManifestItemProperty, OpfMetadata}
import eu.lynxware.epub.Epub
import eu.lynxware.epub.writer.EpubWriter
import eu.lynxware.util.{FileDownloader, FileUtils}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main extends App with LazyLogging {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))

  val chapterNamePattern = s"(c(\\d{1,4}.\\d{1,4}|\\d{1,4}))".r
  val partNamePattern = s"(\\d{1,4}.html)".r
  //val mangaName = "berserk"
  //val mangaPath = "/Users/tku/Downloads/berserk/"

  val mangaName = "eden_it_s_an_endless_world"
  val mangaPath = "/Users/tku/Downloads/eden_it_s_an_endless_world/"

  //val mangaPath = FileUtils.homeDirectory.resolve("Pobrane").resolve("berserk")
  //downloadManga(mangaName, mangaPath)
  //buildEpub()
  buildExampleEpub()

  def buildEpub(): Unit = {
    val metadata = OpfMetadata()
      .withTitle("Berserk")
      .withCreator("MangaDownloader")
      .withLanguage("en")

    val images = (1 to 90).map(i => (Paths.get(mangaPath + "c001/" + i + ".html.jpg"), "c001_" + i))

    val epub = new Epub()
      .withMetadata(metadata)
      .addSection(FileUtils.homeDirectory.resolve("Downloads").resolve("data").resolve("epub30-nav.xhtml"), "nav", false, Some(OpfManifestItemProperty.Nav))
      .addSection(FileUtils.homeDirectory.resolve("Downloads").resolve("data").resolve("content.xhtml"), "tt1", true)
      .addJpegImages(images)

    val epubWriter = new EpubWriter()
    epubWriter.write(epub, FileUtils.homeDirectory.resolve("Downloads").resolve("result.epub"))
  }

  def buildExampleEpub(): Unit = {
    val metadata = OpfMetadata()
      .withTitle("EPUB 3.0 Specification")
      .withCreator("EPUB 3 Working Group")
      .withLanguage("en")

    val epub = Epub()
      .withMetadata(metadata)
      .addNavigation(FileUtils.getResourcePath("/example/xhtml/epub30-nav.xhtml"), "nav")
      .addSection(FileUtils.getResourcePath("/example/xhtml/epub30-titlepage.xhtml"), "tt1")
      .addSection(FileUtils.getResourcePath("/example/xhtml/epub30-ocf.xhtml"), "ocf")
      .addSection(FileUtils.getResourcePath("/example/xhtml/epub30-acknowledgements.xhtml"), "ack")
      .addSection(FileUtils.getResourcePath("/example/xhtml/epub30-mediaoverlays.xhtml"), "mo")
      .addSection(FileUtils.getResourcePath("/example/xhtml/epub30-terminology.xhtml"), "term")
      .addSection(FileUtils.getResourcePath("/example/xhtml/epub30-overview.xhtml"), "ovw")
      .addCoverImage(FileUtils.getResourcePath("/example/img/epub_logo_color.jpg"), "ci")
      .addJpegImage(FileUtils.getResourcePath("/example/img/idpflogo_web_125.jpg"), "logo")
      .addStyle(FileUtils.getResourcePath("/example/css/epub-spec.css"), "css")

    val epubWriter = new EpubWriter()
    epubWriter.write(epub, FileUtils.homeDirectory.resolve("Downloads").resolve("epub30.epub"))
  }

  var currentProgress: Double = 0.0

  def downloadManga(mangaName: String, mangaPath: String): Unit = {
    val crawler = MangatownCrawler(mangaName)
    val chapters = crawler.getListOfChapters()

    val numberOfChapters = chapters.length
    val step: Double = (1.0 / numberOfChapters) * 100.0

    val result = chapters.slice(10, 13).map { ch =>
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
                case None => "1.html"
              }
              val file = new File(dir.getAbsolutePath + "/" + partName + ".jpg")
              FileDownloader.downloadFile(imageLink, file)
            case None => logger.error("No image link")
          }
        }
        ch
      }

      f.onComplete {
        case Success(e) => updateProgress(e, step)
        case Failure(e) => logger.error("Something went wrong", e)
      }

      f
    }

    Future.sequence(result).onComplete {
      case Success(_) => downloadFinished()
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

  def updateProgress(chapterFinished: String, step: Double): Unit = synchronized {
    currentProgress += step
    logger.info(s"Chapter $chapterFinished has been downloaded. Current progress: $currentProgress")
  }
}
