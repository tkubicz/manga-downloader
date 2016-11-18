package eu.lynxware.epub

import java.io.{ByteArrayOutputStream, FileOutputStream}
import java.nio.file.{Files, Path}
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file._
import eu.lynxware.util.FileUtils

import scala.xml.PrettyPrinter

class EpubWriter extends LazyLogging {

  def write(book: Epub, output: Path): Unit = {
    val tmpFolder = FileUtils.randomTmpDirectory
    write(book, output, tmpFolder)
  }

  def write(book: Epub, output: Path, tmpFolder: Path): Unit = {
    //logger.debug("Writing to tmp folder: {}", tmpFolder.toString)
    //createFolderStructure(tmpFolder)
    //copyResourcesToTmp(tmpFolder, book.resources)
    //createMimetypeFile(tmpFolder)
    //createContainerFile(tmpFolder)
    //createPackageFile(tmpFolder, book.metadata)
    packToZipFile(book, output)
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

  private def createBinaryMimetype(): ByteArrayOutputStream = {
    val bytes = MimetypeFile().content.getBytes()
    val stream = new ByteArrayOutputStream(bytes.size)
    stream.write(bytes)
    stream
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

  private def packToZipFile(book: Epub, output: Path): Unit = {
    val fos = new FileOutputStream(output.toFile)
    val zos = new ZipOutputStream(fos)

    zos.setLevel(0)
    zos.putNextEntry(new ZipEntry(MimetypeFile.FileName))
    zos.write(MimetypeFile().content.getBytes)
    zos.closeEntry()

    zos.setLevel(9)
    zos.putNextEntry(new ZipEntry(s"META-INF/${ContainerFile.FileName}"))
    zos.write(ContainerFile().toXml().toString.getBytes)
    zos.closeEntry()

    val images = book.resources
      .filter(_.mediaType == OpfManifestItemMediaType.ImageJpeg)
      .map(r => OpfManifestItem("img/" + r.path.getFileName.toString, r.id, r.mediaType))

    book.resources
      .filter(_.mediaType == OpfManifestItemMediaType.ImageJpeg)
      .foreach { img =>
        zos.putNextEntry(new ZipEntry(s"EPUB/img/" + img.path.getFileName.toString))
        Files.copy(img.path, zos)
        zos.closeEntry()
      }

    val csses = book.resources
      .filter(_.mediaType == OpfManifestItemMediaType.TextCss)
      .map(r => OpfManifestItem("EPUB/css/" + r.path.getFileName.toString, r.id, r.mediaType))

    val content = book.resources
      .filter(_.mediaType == OpfManifestItemMediaType.ApplicationXhtmlXml)
      .map(r => OpfManifestItem("EPUB/xhtml/" + r.path.getFileName.toString, r.id, r.mediaType))

    val manifestItems = images ++ csses ++ content
    val opfFile = OpfFile()
      .withMetadata(book.metadata)
      .withManifestItems(manifestItems)

    val pp = new PrettyPrinter(120, 2)

    zos.putNextEntry(new ZipEntry("EPUB/package.opf"))
    zos.write(pp.formatNodes(opfFile.toXml()).getBytes)
    zos.closeEntry()

    zos.close()
  }
}
