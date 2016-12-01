package eu.lynxware.epub.validation

import eu.lynxware.epub.file.MediaTypes
import eu.lynxware.epub.{Epub, Resource}

import scala.io.Source

case class ResourceWithLinks(resource: Resource, links: Seq[String])

private object LinkValidation {
  private val linkRegexp = "(?:href=\")([^\\\"\\r\\n]+)".r
  private val protocols = Seq("http://", "https://", "ftp://", "mailto:")

  private[validation] def findAllLinks(epub: Epub): Seq[ResourceWithLinks] = epub.resources
    .filter(_.mediaType == MediaTypes.Application.XhtmlXml)
    .map(r => ResourceWithLinks(r, findAllLinksUsingRegexp(r)))

  private[validation] def findAllLinksUsingRegexp(resource: Resource): Seq[String] =
    linkRegexp.findAllIn(Source.fromFile(resource.path.toFile).getLines().mkString).matchData.map(_.group(1)).toSeq

  private[validation] def filterOutLinksWithProtocols(resourcesWithLinks: Seq[ResourceWithLinks]): Seq[ResourceWithLinks] =
    resourcesWithLinks.map(r => r.copy(links = r.links.filterNot(isExternalLink)))

  private[validation] def isExternalLink(link: String): Boolean = protocols.exists(p => link.startsWith(p))

  private[validation] def filterOutLinksToTheSameDocument(resourcesWithLinks: Seq[ResourceWithLinks]) =
    resourcesWithLinks.map(r => r.copy(links = r.links.filterNot(_.startsWith("#"))))
}
