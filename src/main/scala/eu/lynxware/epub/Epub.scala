package eu.lynxware.epub

import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file.OpfManifestItemProperty.OpfManifestItemProperty
import eu.lynxware.epub.file._

case class Resource(path: Path, id: String, mediaType: MediaTypes.MediaType, isSpine: Boolean = false, property: Option[OpfManifestItemProperty] = None)

case class Epub(metadata: OpfMetadata = OpfMetadata(), resources: Seq[Resource] = Seq()) extends LazyLogging {

  def withMetadata(newMetadata: OpfMetadata): Epub = copy(metadata = newMetadata)

  def addImage(path: Path, id: String, mediaType: MediaTypes.MediaType) = copy(resources = resources :+ Resource(path, id, mediaType))
  def addImages(images: Seq[(Path, String, MediaTypes.MediaType)]) = copy(resources = resources ++ images.map(i => Resource(i._1, i._2, i._3)))
  def addJpegImage(path: Path, id: String) = addImage(path, id, MediaTypes.Image.Jpeg)
  def addJpegImages(images: Seq[(Path, String)]) = addImages(images.map(i => (i._1, i._2, MediaTypes.Image.Jpeg)))
  def addStyle(path: Path, id: String) = copy(resources = resources :+ Resource(path, id, MediaTypes.Text.Css))
  def addCoverImage(path: Path, id: String) = copy(resources = resources :+ Resource(path, id, MediaTypes.Image.Jpeg, false, Some(OpfManifestItemProperty.CoverImage)))
  def addNavigation(path: Path, id: String) = copy(resources = resources :+ Resource(path, id, MediaTypes.Application.XhtmlXml, true, Some(OpfManifestItemProperty.Nav)))
  def addSection(path: Path, id: String, isSpine: Boolean = true, property: Option[OpfManifestItemProperty] = None) =
    copy(resources = resources :+ Resource(path, id, MediaTypes.Application.XhtmlXml, isSpine, property))
}
