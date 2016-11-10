package eu.lynxware.epub.file

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import eu.lynxware.epub.file.OpfManifestItemMediaType.OpfManifestItemMediaType
import eu.lynxware.epub.file.OpfManifestItemProperty.OpfManifestItemProperty

import scala.xml._

object OpfManifestItemProperty extends Enumeration {
  type OpfManifestItemProperty = Value
  val Nav = Value("nav")
  val CoverImage = Value("cover-image")
  val Mathml = Value("mathml")
  val RemoteResources = Value("remote-resources")
  val Scripted = Value("scripted")
  val Svg = Value("svg")
  val Switch = Value("switch")
}

object OpfManifestItemMediaType extends Enumeration {
  type OpfManifestItemMediaType = Value

  val ImageGif = Value("image/gif")
  val ImageJpeg = Value("image/jpeg")
  val ImagePng = Value("image/png")
  val ImageSvgXml = Value("image/svg+xml")

  val ApplicationXhtmlXml = Value("application/xhtml+xml")
  val ApplicationXDtbncxXml = Value("application/x-dtbncx+xml")
  val ApplicationVndOpentype = Value("application/vnd.ms-opentype")
  val ApplicationFontWoff = Value("application/font-woff")
  val ApplicationSmilXml = Value("application/smil+xml")
  val ApplicationPlsXml = Value("application/pls+xml")

  val AudioMpeg = Value("audio/mpeg")
  val AudioMp4 = Value("audio/mp4")

  val TextCss = Value("text/css")
  val TextJavascript = Value("text/javascript")
}

case class OpfMetadata(title: String = "unknown",
                       creator: String = "unknown",
                       publisher: String = "unknown",
                       modified: LocalDateTime = LocalDateTime.now(),
                       language: String = "en",
                       uuid: UUID = UUID.randomUUID()) {

  def withTitle(newTitle: String): OpfMetadata = copy(title = newTitle)

  def withCreator(newCreator: String): OpfMetadata = copy(creator = newCreator)

  def withPublisher(newPublisher: String): OpfMetadata = copy(publisher = newPublisher)

  def withModified(newModified: LocalDateTime): OpfMetadata = copy(modified = newModified)

  def withLanguage(newLanguage: String): OpfMetadata = copy(language = newLanguage)

  def withUUID(newUUID: UUID): OpfMetadata = copy(uuid = newUUID)

  def toXml(): Seq[Node] = {
      <dc:identifier id="pub-id">urn:uuid:{uuid}</dc:identifier>
      <meta refines="#pub-id" property="identifier-type" scheme="xsd:string">uuid</meta>
      <dc:title>{title}</dc:title>
      <dc:creator>{creator}</dc:creator>
      <dc:language>{language}</dc:language>
      <meta property="dcterms:modified">{modified.format(OpfMetadata.DateFormatter)}</meta>
  }
}

object OpfMetadata {
  private val ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
  val DateFormatter = DateTimeFormatter.ofPattern(ISO_8601_DATE_FORMAT)
}

case class OpfManifestItem(href: String, id: String, mediaType: OpfManifestItemMediaType, property: Option[OpfManifestItemProperty] = None) {

  def toXml(): Elem = {
    val elem = <item href={href} id={id} media-type={mediaType.toString}/>
    property match {
      case Some(p) => elem % Attribute(None, "properties", Text(p.toString), Null)
      case None => elem
    }
  }
}

case class OpfSpineItem(idref: String, linear: Option[String] = None) {
  def toXml(): Elem = {
    val elem = <itemref idref={idref}/>
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

  def withModified(newModified: LocalDateTime): OpfFile = copy(metadata = metadata.copy(modified = newModified))

  def withCreator(newCreator: String): OpfFile = copy(metadata = metadata.copy(creator = newCreator))

  def withLanguage(newLanguage: String): OpfFile = copy(metadata = metadata.copy(language = newLanguage))

  def withUUID(newUUID: UUID): OpfFile = copy(metadata = metadata.copy(uuid = newUUID))

  def toXml(): Seq[Node] = {
    val metaXml = metadata.toXml
    val manifestXml = <manifest>
      {manifestItems.map(_.toXml)}
    </manifest>
    val spineXml = <spine>
      {spineItems.map(_.toXml)}
    </spine>
    val result = <package version="3.0" xml:lang="en" xmlns="http://www.idpf.org/2007/opf" unique-identifier="pub-id">
      <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        {metaXml}
      </metadata>
      {manifestXml ++ spineXml}
    </package>
    result
  }
}

