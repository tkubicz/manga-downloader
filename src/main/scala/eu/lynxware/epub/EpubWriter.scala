package eu.lynxware.epub

import java.io.FileOutputStream
import java.nio.file.{Files, Path}
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file.OpfManifestItemMediaType.OpfManifestItemMediaType
import eu.lynxware.epub.file._
import eu.lynxware.epub.validation.EpubValidator

import scala.xml.PrettyPrinter

class EpubWriter extends LazyLogging {

  private val defaultResourceLocation: Map[OpfManifestItemMediaType, String] = Map(
    OpfManifestItemMediaType.ImageJpeg -> "img/",
    OpfManifestItemMediaType.TextCss -> "css/",
    OpfManifestItemMediaType.ApplicationXhtmlXml -> "xhtml/"
  )

  private val metaInfFolder = "META-INF/"
  private val contentFolder = "EPUB/"

  def write(book: Epub, output: Path): Unit = {
    val validationResult = EpubValidator.validate(book)
    if (validationResult._1) {
      packToZipFile(book, output)
    }
    else {
      val errors = validationResult._2.mkString("\n")
      logger.error("Validation failed:\n{}", errors)
    }
  }

  private def packToZipFile(book: Epub, output: Path): Unit = {
    val fos = new FileOutputStream(output.toFile)
    val zos = new ZipOutputStream(fos)

    val images = filterImage(book)
    val css = filterCss(book)
    val content = filterContent(book)
    val spineItems = filterSpine(book)
    val manifestItems = (images ++ css ++ content).map(_._1)
    val opfFile = buildOpfFile(book.metadata, manifestItems, spineItems)

    addMimetype(zos)
    addContainer(zos)
    addManifestItemsToZipStream(zos, images ++ css ++ content)
    addPackage(zos, opfFile)

    zos.close()
  }

  private def addMimetype(zos: ZipOutputStream): Unit = {
    zos.setLevel(0)
    addNextEntry(zos, MimetypeFile.FileName, MimetypeFile().content.getBytes())
    zos.setLevel(9)
  }

  private def addContainer(zos: ZipOutputStream): Unit =
    addNextEntry(zos, metaInfFolder + ContainerFile.FileName, ContainerFile().toXml().toString().getBytes())

  private def addPackage(zos: ZipOutputStream, opfFile: OpfFile): Unit = {
    val pp = new PrettyPrinter(120, 2)
    addNextEntry(zos, contentFolder + opfFile.fileName, pp.formatNodes(opfFile.toXml()).getBytes())
  }

  private def filterImage(book: Epub) = filterMediaType(book, OpfManifestItemMediaType.ImageJpeg, defaultResourceLocation(OpfManifestItemMediaType.ImageJpeg))

  private def filterCss(book: Epub) = filterMediaType(book, OpfManifestItemMediaType.TextCss, defaultResourceLocation(OpfManifestItemMediaType.TextCss))

  private def filterContent(book: Epub) = filterMediaType(book, OpfManifestItemMediaType.ApplicationXhtmlXml, defaultResourceLocation(OpfManifestItemMediaType.ApplicationXhtmlXml))

  private def filterSpine(book: Epub): Seq[OpfSpineItem] = book.resources.filter(_.isSpine).map(r => OpfSpineItem(r.id, None))

  private def filterMediaType(book: Epub, mediaType: OpfManifestItemMediaType, folder: String) = book.resources
    .filter(_.mediaType == mediaType)
    .map(r => (OpfManifestItem(folder + r.path.getFileName.toString, r.id, r.mediaType, r.property), r))

  private def addManifestItemsToZipStream(zos: ZipOutputStream, items: Seq[(OpfManifestItem, Resource)]): Unit =
    items.foreach(item => addNextResourceEntry(zos, item._2))

  private def addNextResourceEntry(zos: ZipOutputStream, resource: Resource): Unit = {
    zos.putNextEntry(new ZipEntry(contentFolder + defaultResourceLocation(resource.mediaType) + resource.path.getFileName.toString))
    Files.copy(resource.path, zos)
    zos.closeEntry()
  }

  private def addNextEntry(zos: ZipOutputStream, filename: String, content: Array[Byte]): Unit = {
    zos.putNextEntry(new ZipEntry(filename))
    zos.write(content)
    zos.closeEntry()
  }

  private def buildOpfFile(metadata: OpfMetadata, manifestItems: Seq[OpfManifestItem], spineItems: Seq[OpfSpineItem]): OpfFile =
    OpfFile().withMetadata(metadata).withManifestItems(manifestItems).withSpineItems(spineItems)
}
