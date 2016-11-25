package eu.lynxware.epub

import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file.OpfManifestItemMediaType.OpfManifestItemMediaType
import eu.lynxware.epub.file.OpfManifestItemProperty.OpfManifestItemProperty
import eu.lynxware.epub.file._

case class Resource(path: Path, id: String, mediaType: OpfManifestItemMediaType, isSpine: Boolean = false, property: Option[OpfManifestItemProperty] = None)

case class Epub(metadata: OpfMetadata = OpfMetadata(), resources: Seq[Resource] = Seq()) extends LazyLogging {

  def withMetadata(newMetadata: OpfMetadata): Epub = copy(metadata = newMetadata)

  def addJpegImage(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.ImageJpeg))
  }

  def addJpegImages(images: Seq[(Path, String)]) = {
    copy(resources = resources ++ images.map(i => Resource(i._1, i._2, OpfManifestItemMediaType.ImageJpeg)))
  }

  def addStyle(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.TextCss))
  }

  def addSection(path: Path, id: String, isSpine: Boolean = false, property: Option[OpfManifestItemProperty] = None) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.ApplicationXhtmlXml, isSpine, property))
  }
}
