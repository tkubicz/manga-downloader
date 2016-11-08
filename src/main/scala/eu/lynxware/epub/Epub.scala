package eu.lynxware.epub

import java.io.FileOutputStream
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file.{ContainerFile, MimetypeFile, OpfFile, OpfManifestItemMediaType}
import eu.lynxware.epub.file.OpfManifestItemMediaType.OpfManifestItemMediaType
import eu.lynxware.util.FileUtils

case class Resource(path: Path, id: String, mediaType: OpfManifestItemMediaType)

case class Epub(resources: Seq[Resource] = Seq()) extends LazyLogging {

  val tempFolder = FileUtils.getRandomTmpFolder()

  def addImage(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.ImageJpeg))
  }

  def addStyle(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.TextCss))
  }

  def addSection(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.ApplicationXhtmlXml))
  }

  def createFolderStructure(path: Path): Unit = {
    FileUtils.createDirectory(path.resolve("META-INF"))
    FileUtils.createDirectory(path.resolve("EPUB")).right.map { epubPath =>
      FileUtils.createDirectory(epubPath.resolve("css"))
      FileUtils.createDirectory(epubPath.resolve("xhtml"))
      FileUtils.createDirectory(epubPath.resolve("img"))
    }
  }

  def createPageFile(path: Path, title: String): Unit = {
    val filepath = FileUtils.createFile(path.resolve("titlepage.xhtml")).left.get
    val content = <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <meta charset="utf-8"/>
        <title>
          {title}
        </title>
      </head>
      <body>
        <h1 class="titlepage">
          {title}
        </h1>
        <p>Hello world!</p>
      </body>
    </html>
    //writeXmlToFile(content, filepath)
  }

  def write(fos: FileOutputStream): Unit = {
    logger.debug("Writing to tmp folder: {}", tempFolder.toString)

    createFolderStructure(tempFolder)

    resources.filter(_.mediaType == OpfManifestItemMediaType.ImageJpeg).foreach(r => FileUtils.copy(r.path, tempFolder.resolve("EPUB/img/").resolve(r.path.getFileName)))
    resources.filter(_.mediaType == OpfManifestItemMediaType.TextCss).foreach(r => FileUtils.copy(r.path, tempFolder.resolve("EPUB/css/").resolve(r.path.getFileName)))
    resources.filter(_.mediaType == OpfManifestItemMediaType.ApplicationXhtmlXml).foreach(r => FileUtils.copy(r.path, tempFolder.resolve("EPUB/xhtml/").resolve(r.path.getFileName)))

    val mimetype = FileUtils.createFile(tempFolder.resolve("mimetype")).right.get
    FileUtils.writeContentToFile(mimetype, MimetypeFile().toString())

    val containerFile = FileUtils.createFile(tempFolder.resolve("META-INF/container.xml")).right.get
    FileUtils.writeXmlToFile(containerFile, ContainerFile().toXml())

    val opfFile = OpfFile()
      .withTitle("Test Epub")
      //.withManife

  }
}
