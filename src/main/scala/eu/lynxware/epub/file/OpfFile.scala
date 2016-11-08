package eu.lynxware.epub.file

import java.util.UUID

import eu.lynxware.epub.file.OpfManifestItemMediaType.OpfManifestItemMediaType
import eu.lynxware.epub.file.OpfManifestItemProperty.OpfManifestItemProperty

import scala.xml._

object OpfManifestItemProperty extends Enumeration {
  type OpfManifestItemProperty = Value
  val Nav = Value("nav")
  val CoverImage = Value("cover-image")
}

object OpfManifestItemMediaType extends Enumeration {
  type OpfManifestItemMediaType = Value
  val ApplicationXhtmlXml = Value("application/xhtml+xml")
  val TextCss = Value("text/css")
  val ImageJpeg = Value("image/jpeg")
}

case class OpfMetadata(title: String = "unknown",
                       creator: String = "unknown",
                       publisher: String = "unknown",
                       modified: String = "2000-03-24T00:00:00Z",
                       language: String = "en",
                       uuid: UUID = UUID.randomUUID()) {
  def toXml(): Elem = {
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
      <dc:identifier id="pub-id">urn:uuid:{uuid}</dc:identifier>
      <meta refines="#pub-id" property="identifier-type" scheme="xsd:string">uuid</meta>
      <dc:title>{title}</dc:title>
      <dc:creator>{creator}</dc:creator>
      <dc:language>{language}</dc:language>
      <meta property="dcterms:modified">{modified}</meta>
    </metadata>
  }
}

case class OpfManifestItem(href: String, id: String, mediaType: OpfManifestItemMediaType, property: Option[OpfManifestItemProperty] = None) {

  def toXml(): Elem = {
      val elem = <item href={href} id={id} media-type={mediaType.toString} />
      property match {
        case Some(p) => elem % Attribute(None, "properties", Text(p.toString), Null)
        case None => elem
      }
  }
}

case class OpfSpineItem(idref: String, linear: Option[String] = None) {
  def toXml(): Elem = {
    val elem = <itemref idref={idref} />
    linear match {
      case Some(l) => elem % Attribute(None, "linear", Text(l), Null)
      case None => elem
    }
  }
}

case class OpfFile(fileName: String = "package.opf", metadata: OpfMetadata = OpfMetadata(), manifestItems: Seq[OpfManifestItem] = Seq(), spineItems: Seq[OpfSpineItem] = Seq()) {
  def withFileName(newFileName: String): OpfFile = copy(fileName = newFileName)

  def withMetadata(newMetadata: OpfMetadata): OpfFile = copy(metadata = newMetadata)

  def withManifestItems(newManifestItems: Seq[OpfManifestItem]): OpfFile = copy(manifestItems = newManifestItems)

  def withSpineItems(newSpineItems: Seq[OpfSpineItem]): OpfFile = copy(spineItems = newSpineItems)

  def withManifestItem(manifestItem: OpfManifestItem): OpfFile = copy(manifestItems = manifestItems :+ manifestItem)

  def withManifestItem(href: String, id: String, mediaType: OpfManifestItemMediaType, property: Option[OpfManifestItemProperty]): OpfFile =
    copy(manifestItems = manifestItems :+ new OpfManifestItem(href, id, mediaType, property))

  def withSpineItem(spineItem: OpfSpineItem): OpfFile = copy(spineItems = spineItems :+ spineItem)

  def withSpineItem(idref: String, linear: Option[String]): OpfFile =
    copy(spineItems = spineItems :+ new OpfSpineItem(idref, linear))

  def withTitle(newTitle: String): OpfFile = copy(metadata = metadata.copy(title = newTitle))

  def withPublisher(newPublisher: String): OpfFile = copy(metadata = metadata.copy(publisher = newPublisher))

  def withModified(newModified: String): OpfFile = copy(metadata = metadata.copy(modified = newModified))

  def withCreator(newCreator: String): OpfFile = copy(metadata = metadata.copy(creator = newCreator))

  def withLanguage(newLanguage: String): OpfFile = copy(metadata = metadata.copy(language = newLanguage))

  def withUUID(newUUID: UUID): OpfFile = copy(metadata = metadata.copy(uuid = newUUID))

  def toXml(): Seq[Node] = {
    val metaXml = metadata.toXml
    val manifestXml = <manifest>{manifestItems.map(_.toXml)}</manifest>
    val spineXml = <spine>{spineItems.map(_.toXml)}</spine>
    val result = <package version="3.0" xml:lang="en" xmlns="http://www.idpf.org/2007/opf" unique-identifier="pub-id">{metaXml ++ manifestXml ++ spineXml}</package>
    result
  }
}

