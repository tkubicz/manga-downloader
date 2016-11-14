package eu.lynxware.epub

import java.io.FileOutputStream
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file._
import eu.lynxware.util.FileUtils

class EpubWriter extends LazyLogging {

  def write(book: Epub, fos: FileOutputStream): Unit = {
    val tmpFolder = FileUtils.getRandomTmpFolder()
    write(book, fos, tmpFolder)
  }

  def write(book: Epub, fos: FileOutputStream, tmpFolder: Path): Unit = {
    logger.debug("Writing to tmp folder: {}", tmpFolder.toString)
    createFolderStructure(tmpFolder)
    copyResourcesToTmp(tmpFolder, book.resources)
    createMimetypeFile(tmpFolder)
    createContainerFile(tmpFolder)
    createPackageFile(tmpFolder, book.metadata)
  }

  private def createFolderStructure(path: Path): Unit = {
    FileUtils.createDirectory(path.resolve("META-INF"))
    FileUtils.createDirectory(path.resolve("EPUB")).right.map { epubPath =>
      FileUtils.createDirectory(epubPath.resolve("css"))
      FileUtils.createDirectory(epubPath.resolve("xhtml"))
      FileUtils.createDirectory(epubPath.resolve("img"))
    }
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

  private def copyResourcesToTmp(tmpFolder: Path, resources: Seq[Resource]): Unit = {
    resources.filter(_.mediaType == OpfManifestItemMediaType.ImageJpeg).foreach(r => FileUtils.copy(r.path, tmpFolder.resolve("EPUB/img/").resolve(r.path.getFileName)))
    resources.filter(_.mediaType == OpfManifestItemMediaType.TextCss).foreach(r => FileUtils.copy(r.path, tmpFolder.resolve("EPUB/css/").resolve(r.path.getFileName)))
    resources.filter(_.mediaType == OpfManifestItemMediaType.ApplicationXhtmlXml).foreach(r => FileUtils.copy(r.path, tmpFolder.resolve("EPUB/xhtml/").resolve(r.path.getFileName)))
  }
}