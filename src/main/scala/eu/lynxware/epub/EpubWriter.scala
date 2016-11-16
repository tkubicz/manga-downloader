package eu.lynxware.epub

import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file._
import eu.lynxware.util.FileUtils

class EpubWriter extends LazyLogging {

  def write(book: Epub, output: Path): Unit = {
    val tmpFolder = FileUtils.randomTmpDirectory
    write(book, output, tmpFolder)
  }

  def write(book: Epub, output: Path, tmpFolder: Path): Unit = {
    logger.debug("Writing to tmp folder: {}", tmpFolder.toString)
    createFolderStructure(tmpFolder)
    copyResourcesToTmp(tmpFolder, book.resources)
    createMimetypeFile(tmpFolder)
    createContainerFile(tmpFolder)
    createPackageFile(tmpFolder, book.metadata)
    packToZipFile(tmpFolder, output)
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

  private def packToZipFile(input: Path, output: Path): Unit = {
    FileUtils.packSingleFileToZip(input.resolve(MimetypeFile.FileName), output, 0)
    FileUtils.packToZip(input.resolve("META-INF"), output, 9)
  }
}
