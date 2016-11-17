package eu.lynxware.epub

import java.io.FileOutputStream
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file.OpfManifestItemMediaType.OpfManifestItemMediaType
import eu.lynxware.epub.file._
import eu.lynxware.util.FileUtils

case class Resource(path: Path, id: String, mediaType: OpfManifestItemMediaType)

case class Epub(metadata: OpfMetadata = OpfMetadata(), resources: Seq[Resource] = Seq()) extends LazyLogging {

  def withMetadata(newMetadata: OpfMetadata): Epub = copy(metadata = newMetadata)

  def addJpegImage(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.ImageJpeg))
  }

  def addStyle(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.TextCss))
  }

  def addSection(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.ApplicationXhtmlXml))
  }
}
