package eu.lynxware.epub.writer

import java.io.FileOutputStream
import java.nio.file.{Files, Path}
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file._
import eu.lynxware.epub.validation.EpubValidator
import eu.lynxware.epub.{Epub, Resource}

import scala.xml.PrettyPrinter

class EpubWriter extends LazyLogging {

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
    addNextEntry(zos, FileStructure.MetaInfFolder + ContainerFile.FileName, ContainerFile().toXml().toString().getBytes())

  private def addPackage(zos: ZipOutputStream, opfFile: OpfFile): Unit = {
    val pp = new PrettyPrinter(120, 2)
    addNextEntry(zos, FileStructure.ContentFolder + opfFile.fileName, pp.formatNodes(opfFile.toXml()).getBytes())
  }

  private def filterImage(book: Epub) = filterMediaType[MediaTypes.MediaTypeImage](book, FileStructure.getDefaultResourceLocation(MediaTypes.Image.Jpeg))

  private def filterCss(book: Epub) = filterMediaType[MediaTypes.Text.Css.type](book, FileStructure.getDefaultResourceLocation(MediaTypes.Text.Css))

  private def filterContent(book: Epub) = filterMediaType[MediaTypes.Application.XhtmlXml.type](book, FileStructure.getDefaultResourceLocation(MediaTypes.Application.XhtmlXml))

  private def filterSpine(book: Epub): Seq[OpfSpineItem] = book.resources.filter(_.isSpine).map(r => OpfSpineItem(r.id, None))

  private def filterMediaType[T <: MediaTypes.MediaType](book: Epub, folder: String) = book.resources
    .filter(_.mediaType.isInstanceOf[T])
    .map(r => (OpfManifestItem(folder + r.path.getFileName.toString, r.id, r.mediaType, r.property), r))

  private def addManifestItemsToZipStream(zos: ZipOutputStream, items: Seq[(OpfManifestItem, Resource)]): Unit =
    items.foreach(item => addNextResourceEntry(zos, item._2))

  private def addNextResourceEntry(zos: ZipOutputStream, resource: Resource): Unit = {
    zos.putNextEntry(new ZipEntry(FileStructure.ContentFolder + FileStructure.getDefaultResourceLocation(resource.mediaType) + resource.path.getFileName.toString))
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
