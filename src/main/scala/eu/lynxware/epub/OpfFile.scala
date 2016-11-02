package eu.lynxware.epub

import java.util.UUID

import eu.lynxware.epub.OpfManifestItemProperty.OpfManifestItemProperty

object OpfManifestItemProperty extends Enumeration {
  type OpfManifestItemProperty = Value
  val NAV = Value("nav")
  val COVER_IMAGE = Value("cover-image")
}

case class OpfMetadata(author: String = "unknown",
                       title: String = "unknown",
                       creator: String = "unknown",
                       language: String = "en",
                       uuid: UUID = UUID.randomUUID())

case class OpfManifestItem(href: String, id: String, mediaType: String, property: Option[OpfManifestItemProperty])

case class OpfSpineItem(idref: String, linear: String)

case class OpfFile(fileName: String = "package.opf", metadata: OpfMetadata = OpfMetadata(), manifestItems: Seq[OpfManifestItem] = Seq(), spineItems: Seq[OpfSpineItem] = Seq()) {
  def withFileName(newFileName: String): OpfFile = copy(fileName = newFileName)

  def withMetadata(newMetadata: OpfMetadata): OpfFile = copy(metadata = newMetadata)

  def withManifestItems(newManifestItems: Seq[OpfManifestItem]): OpfFile = copy(manifestItems = newManifestItems)

  def withSpineItems(newSpineItems: Seq[OpfSpineItem]): OpfFile = copy(spineItems = newSpineItems)

  def withManifestItem(manifestItem: OpfManifestItem): OpfFile = copy(manifestItems = manifestItems :+ manifestItem)

  def withManifestItem(href: String, id: String, mediaType: String, property: Option[OpfManifestItemProperty]): OpfFile =
    copy(manifestItems = manifestItems :+ new OpfManifestItem(href, id, mediaType, property))

  def withSpineItem(spineItem: OpfSpineItem): OpfFile = copy(spineItems = spineItems :+ spineItem)

  def withSpineItem(idref: String, linear: String): OpfFile =
    copy(spineItems = spineItems :+ new OpfSpineItem(idref, linear))

  def withAuthor(newAuthor: String): OpfFile = copy(metadata = metadata.copy(author = newAuthor))

  def withTitle(newTitle: String): OpfFile = copy(metadata = metadata.copy(title = newTitle))

  def withCreator(newCreator: String): OpfFile = copy(metadata = metadata.copy(creator = newCreator))

  def withLanguage(newLanguage: String): OpfFile = copy(metadata = metadata.copy(language = newLanguage))

  def withUUID(newUUID: UUID): OpfFile = copy(metadata = metadata.copy(uuid = newUUID))
}

