package eu.lynxware.epub

import java.io.FileOutputStream
import java.nio.file.{Files, Path}
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file.OpfManifestItemMediaType.OpfManifestItemMediaType
import eu.lynxware.epub.file._

import scala.xml.PrettyPrinter

class EpubWriter extends LazyLogging {

  private val defaultResourceLocation: Map[OpfManifestItemMediaType, String] = Map(
    OpfManifestItemMediaType.ImageJpeg -> "img/",
    OpfManifestItemMediaType.TextCss -> "css/",
    OpfManifestItemMediaType.ApplicationXhtmlXml -> "xhtml/"
  )

  def write(book: Epub, output: Path): Unit = {
    packToZipFile(book, output)
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

    val images = filterImage(book)
    val css = filterCss(book)
    val content = filterContent(book)

    addManifestItemsToZipStream(zos, images ++ css ++ content)

    val spineItems = filterSpine(book)
    val manifestItems = (images ++ css ++ content).map(_._1)
    val opfFile = buildOpfFile(book.metadata, manifestItems, spineItems)

    val pp = new PrettyPrinter(120, 2)
    zos.putNextEntry(new ZipEntry("EPUB/package.opf"))
    zos.write(pp.formatNodes(opfFile.toXml()).getBytes)
    zos.closeEntry()

    zos.close()
  }

  private def filterMediaType(book: Epub, mediaType: OpfManifestItemMediaType, folder: String) = book.resources
    .filter(_.mediaType == mediaType)
    .map(r => (OpfManifestItem(folder + r.path.getFileName.toString, r.id, r.mediaType), r))

  private def filterImage(book: Epub) = filterMediaType(book, OpfManifestItemMediaType.ImageJpeg, defaultResourceLocation(OpfManifestItemMediaType.ImageJpeg))
  private def filterCss(book: Epub) = filterMediaType(book, OpfManifestItemMediaType.TextCss, defaultResourceLocation(OpfManifestItemMediaType.TextCss))
  private def filterContent(book: Epub) = filterMediaType(book, OpfManifestItemMediaType.ApplicationXhtmlXml, defaultResourceLocation(OpfManifestItemMediaType.ApplicationXhtmlXml))
  private def filterSpine(book: Epub): Seq[OpfSpineItem] = book.resources.filter(_.isSpine).map(r => OpfSpineItem(r.id, None))

  private def addManifestItemsToZipStream(zos: ZipOutputStream, items: Seq[(OpfManifestItem, Resource)]): Unit =
    items.foreach(item => addNextEntry(zos, item._2))

  private def addNextEntry(zos: ZipOutputStream, resource: Resource): Unit = {
    zos.putNextEntry(new ZipEntry(s"EPUB/" + defaultResourceLocation.get(resource.mediaType) + resource.path.getFileName.toString))
    Files.copy(resource.path, zos)
    zos.closeEntry()
  }

  private def buildOpfFile(metadata: OpfMetadata, manifestItems: Seq[OpfManifestItem], spineItems: Seq[OpfSpineItem]): OpfFile =
    OpfFile().withMetadata(metadata).withManifestItems(manifestItems).withSpineItems(spineItems)
}
