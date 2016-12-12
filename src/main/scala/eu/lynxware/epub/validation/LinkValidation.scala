package eu.lynxware.epub.validation

import eu.lynxware.epub.file.MediaTypes
import eu.lynxware.epub.{Epub, Resource}

import scala.io.Source

case class ResourceWithLinks(resource: Resource, links: Seq[String] = Seq.empty, anchors: Seq[String] = Seq.empty)

private object LinkValidation {
  private val linkRegexp = "(?:href=\")([^\\\"\\r\\n]+)".r
  private val nameOrIdRegexp = "(?:name=\"|id=\")([^\\\"\\r\\n]+)".r
  private val protocols = Seq("http://", "https://", "ftp://", "mailto:")

  private val findInterestingLinks = findAllLinks _ andThen filterOutLinksWithProtocols
  private val localAnchorLinks = findInterestingLinks andThen filterLocalAnchorLinks
  private val remoteAnchorLinks = findInterestingLinks andThen filterOutLocalAnchorLinks andThen filterRemoteAnchorLinks

  // TODO: Change result type to return also messages
  private[validation] def validateLinks(epub: Epub): (Boolean, String) = {
    val res = localAnchorLinks(epub).map(r => findInvalidLocalAnchorLinks(r.links, r.anchors))
    val messages = res.flatten

    if (messages.isEmpty) (true, "")
    else (false, messages.mkString("\n"))
  }

  private def findAllLinks(epub: Epub): Seq[ResourceWithLinks] = epub.resources
    .filter(_.mediaType == MediaTypes.Application.XhtmlXml)
    .map(r => ResourceWithLinks(r, findAllLinksUsingRegexp(r), findAllAnchorsUsingRegexp(r)))

  private def findAllLinksUsingRegexp(resource: Resource): Seq[String] =
    linkRegexp.findAllIn(Source.fromFile(resource.path.toFile).getLines().mkString).matchData.map(_.group(1)).toSeq

  private def findAllAnchorsUsingRegexp(resource: Resource): Seq[String] =
    nameOrIdRegexp.findAllIn(Source.fromFile(resource.path.toFile).getLines().mkString).matchData.map(_.group(1)).toSeq

  private def filterOutLinksWithProtocols(resourcesWithLinks: Seq[ResourceWithLinks]): Seq[ResourceWithLinks] =
    resourcesWithLinks.map(r => r.copy(links = r.links.filterNot(isExternalLink)))

  private def filterLocalAnchorLinks(resourceWithLinks: Seq[ResourceWithLinks]): Seq[ResourceWithLinks] =
    resourceWithLinks.map(r => r.copy(links = r.links.filter(isLocalAnchorLink).map(_.drop(1))))

  private def filterOutLocalAnchorLinks(resourcesWithLinks: Seq[ResourceWithLinks]): Seq[ResourceWithLinks] =
    resourcesWithLinks.map(r => r.copy(links = r.links.filterNot(isLocalAnchorLink)))

  private def filterRemoteAnchorLinks(resourcesWithLinks: Seq[ResourceWithLinks]): Seq[ResourceWithLinks] =
    resourcesWithLinks.map(r => r.copy(links = r.links.filter(isRemoteAnchorLink)))

  private def findInvalidLocalAnchorLinks(links: Seq[String], anchors: Seq[String]): Seq[String] =
    links.filterNot(anchors.toSet)

  private def isExternalLink(link: String): Boolean = protocols.exists(p => link.startsWith(p))

  private def isLocalAnchorLink(link: String): Boolean = link.startsWith("#")

  private def isRemoteAnchorLink(link: String): Boolean = !link.startsWith("#") && link.contains("#")
}
