package eu.lynxware

import java.nio.file.Paths
import java.util.concurrent.Executors

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.Epub

import scala.concurrent.ExecutionContext

object Main extends App with LazyLogging {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

  val chapterNamePattern = s"(c(\\d{1,4}|\\d{1,4}.\\d{1,4}))".r
  val partNamePattern = s"(\\d{1,4}.html)".r
  val mangaName = "berserk"
  val mangaPath = "/home/tku/Pobrane/berserk/"

  val epub = new Epub()
    .addImage(Paths.get(mangaPath).resolve("c001/1.html.jpg"), "c001_1")
    .addImage(Paths.get(mangaPath).resolve("c001/2.html.jpg"), "c001_2")
    .addImage(Paths.get(mangaPath).resolve("c001/3.html.jpg"), "c001_3")

  epub.write(null)


  //epub.createFolderStructure(Paths.get(mangaPath).resolve("test"))
  //epub.packToZip(Paths.get(mangaPath).resolve("c001"), Paths.get(mangaPath).resolve("test.epub"))

  /*epub.createMimetypeFile(Paths.get(mangaPath))
  epub.createContainerFile(Paths.get(mangaPath).resolve("META-INF"))
  epub.createOpfFile(Paths.get(mangaPath), mangaName, "manga-downloader-0.0.1-SNAPSHOT", "en")
  epub.createPageFile(Paths.get(mangaPath), "Hello World")*/

  /*val crawler = MangatownCrawler(mangaName)
  val chapters = crawler.getListOfChapters
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
  }*/

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
