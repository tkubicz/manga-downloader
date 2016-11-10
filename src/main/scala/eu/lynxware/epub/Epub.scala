package eu.lynxware.epub

import java.io.FileOutputStream
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file.OpfManifestItemMediaType.OpfManifestItemMediaType
import eu.lynxware.epub.file._
import eu.lynxware.util.FileUtils

case class Resource(path: Path, id: String, mediaType: OpfManifestItemMediaType)

case class Epub(metadata: OpfMetadata = OpfMetadata(), resources: Seq[Resource] = Seq()) extends LazyLogging {

  val tmpFolder = FileUtils.getRandomTmpFolder()

  def withMetadata(newMetadata: OpfMetadata): Epub = copy(metadata = newMetadata)

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
    logger.debug("Writing to tmp folder: {}", tmpFolder.toString)

    createFolderStructure(tmpFolder)
    copyResourcesToTmp(tmpFolder)
    createMimetypeFile(tmpFolder)
    createContainerFile(tmpFolder)
    createPackageFile(tmpFolder, metadata)
  }

  private def createMimetypeFile(tmpFolder: Path): Unit = {
    FileUtils.createFile(tmpFolder.resolve(MimetypeFile.FileName)) match {
      case Right(path) => FileUtils.writeContentToFile(path, MimetypeFile().content.toString)
      case Left(e) => logger.error("", e)
    }
  }

  private def createContainerFile(tmpFolder: Path): Unit = {
    FileUtils.createFile(tmpFolder.resolve("META-INF").resolve(ContainerFile.FileName)) match {
      case Right(path) => FileUtils.writeXmlToFile(path, ContainerFile().toXml())
      case Left(e) => logger.error("", e)
    }
  }

  private def createPackageFile(tmpFolder: Path, metadata: OpfMetadata): Unit = {
    val opfFile = OpfFile().withMetadata(metadata)
    FileUtils.createFile(tmpFolder.resolve("EPUB").resolve("package.opf")) match {
      case Right(path) => FileUtils.writeXmlToFile(path, opfFile.toXml())
      case Left(e) => logger.error("", e)
    }
  }

  private def copyResourcesToTmp(tmpFolder: Path): Unit = {
    resources.filter(_.mediaType == OpfManifestItemMediaType.ImageJpeg).foreach(r => FileUtils.copy(r.path, tmpFolder.resolve("EPUB/img/").resolve(r.path.getFileName)))
    resources.filter(_.mediaType == OpfManifestItemMediaType.TextCss).foreach(r => FileUtils.copy(r.path, tmpFolder.resolve("EPUB/css/").resolve(r.path.getFileName)))
    resources.filter(_.mediaType == OpfManifestItemMediaType.ApplicationXhtmlXml).foreach(r => FileUtils.copy(r.path, tmpFolder.resolve("EPUB/xhtml/").resolve(r.path.getFileName)))
  }
}
