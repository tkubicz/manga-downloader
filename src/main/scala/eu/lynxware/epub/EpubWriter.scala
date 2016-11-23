package eu.lynxware.epub

import java.io.FileOutputStream
import java.nio.file.{Files, Path}
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file._

import scala.xml.PrettyPrinter

class EpubWriter extends LazyLogging {

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

    book.resources
      .filter(_.mediaType == OpfManifestItemMediaType.ApplicationXhtmlXml)
      .foreach { content =>
        zos.putNextEntry(new ZipEntry(s"EPUB/xhtml/" + content.path.getFileName.toString))
        Files.copy(content.path, zos)
        zos.closeEntry()
      }

    val content = book.resources
      .filter(_.mediaType == OpfManifestItemMediaType.ApplicationXhtmlXml)
      .map(r => OpfManifestItem("xhtml/" + r.path.getFileName.toString, r.id, r.mediaType, r.property))

    // Spine items
    val spineItems = book.resources.filter(_.isSpine).map(r => OpfSpineItem(r.id, None))

    val manifestItems = images ++ csses ++ content
    val opfFile = OpfFile()
      .withMetadata(book.metadata)
      .withManifestItems(manifestItems)
      .withSpineItems(spineItems)

    val pp = new PrettyPrinter(120, 2)

    zos.putNextEntry(new ZipEntry("EPUB/package.opf"))
    zos.write(pp.formatNodes(opfFile.toXml()).getBytes)
    zos.closeEntry()

    zos.close()
  }
}
