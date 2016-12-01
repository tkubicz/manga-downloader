package eu.lynxware.epub.file

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
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


object MediaTypes {
  sealed abstract class MediaType(val name: String) {
    override def toString: String = name
  }

  sealed abstract class MediaTypeImage(name: String) extends MediaType(name)
  sealed abstract class MediaTypeApplication(name: String) extends MediaType(name)
  sealed abstract class MediaTypeAudio(name: String) extends MediaType(name)
  sealed abstract class MediaTypeText(name: String) extends MediaType(name)

  object Image {
    case object Jpeg extends MediaTypeImage("image/jpeg")
    case object Gif extends MediaTypeImage("image/gif")
    case object Png extends MediaTypeImage("image/png")
    case object SvgXml extends MediaTypeImage("image/svg+xml")
  }

  object Application {
    case object XhtmlXml extends MediaTypeApplication("application/xhtml+xml")
    case object XDtbncxXml extends MediaTypeApplication("application/x-dtbncx+xml")
    case object VndOpentype extends MediaTypeApplication("application/vnd.ms-opentype")
    case object FontWoff extends MediaTypeApplication("application/font-woff")
    case object SmilXml extends MediaTypeApplication("application/smil+xml")
    case object PlsXml extends MediaTypeApplication("application/pls+xml")
  }

  object Audio {
    case object Mpeg extends MediaTypeAudio("audio/mpeg")
    case object Mp4 extends MediaTypeAudio("audio/mp4")
  }

  object Text {
    case object Css extends MediaTypeText("text/css")
    case object Javascript extends MediaTypeText("text/javascript")
  }
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

case class OpfManifestItem(href: String, id: String, mediaType: MediaTypes.MediaType, property: Option[OpfManifestItemProperty] = None) {
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

case class OpfFile(fileName: String = "package.opf",
                   metadata: OpfMetadata = OpfMetadata(),
                   manifestItems: Seq[OpfManifestItem] = Seq(),
                   spineItems: Seq[OpfSpineItem] = Seq()) {
  def withFileName(newFileName: String): OpfFile = copy(fileName = newFileName)

  def withMetadata(newMetadata: OpfMetadata): OpfFile = copy(metadata = newMetadata)

  def withManifestItems(newManifestItems: Seq[OpfManifestItem]): OpfFile = copy(manifestItems = newManifestItems)

  def withSpineItems(newSpineItems: Seq[OpfSpineItem]): OpfFile = copy(spineItems = newSpineItems)

  def toXml(): Seq[Node] = {
    val metaXml = metadata.toXml
    val manifestXml = <manifest>{manifestItems.map(_.toXml)}</manifest>
    val spineXml = <spine>{spineItems.map(_.toXml)}</spine>
    val result = <package version="3.0" xml:lang="en" xmlns="http://www.idpf.org/2007/opf" unique-identifier="pub-id">
      <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        {metaXml}
      </metadata>
      {manifestXml ++ spineXml}
    </package>
    result
  }
}

