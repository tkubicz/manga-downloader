package eu.lynxware.epub

import eu.lynxware.epub.file.{OpfManifestItemMediaType, OpfManifestItemProperty}

import scala.io.Source

case class ResourceWithLinks(resource: Resource, links: Seq[String])

object EpubValidator {

  private val exactlyOneNavNeeded: (Epub) => (Boolean, String) =
    e => (e.resources.exists(r => r.property.contains(OpfManifestItemProperty.Nav)), "Exactly one nav property is needed")

  private val atLeastOneSpineItemNeeded: Epub => (Boolean, String) =
    e => (e.resources.exists(r => r.isSpine), "At least one spine item is needed")

  private val linkRegexp = "(?:href=\")([^\\\"\\r\\n]+)".r

  private val protocols = Seq("http://", "https://", "ftp://", "mailto:")

  private val validators = Seq(exactlyOneNavNeeded, atLeastOneSpineItemNeeded)

  def validate(epub: Epub): (Boolean, Seq[String]) = {

    val test = findAllLinks _ andThen filterLinksWithProtocols

    test(epub).slice(2, 3).foreach { r =>
      println(r.resource)
      r.links.foreach(println)
    }

    val messages = validators.map(v => v(epub)).filter(!_._1).map(_._2)
    if (messages.nonEmpty) (false, messages)
    else (true, Seq.empty)
  }

  private def findAllLinks(epub: Epub): Seq[ResourceWithLinks] = epub.resources
    .filter(_.mediaType == OpfManifestItemMediaType.ApplicationXhtmlXml)
    .map(r => ResourceWithLinks(r, findAllLinksUsingRegexp(r)))

  private def findAllLinksUsingRegexp(resource: Resource): Seq[String] =
    linkRegexp.findAllIn(Source.fromFile(resource.path.toFile).getLines().mkString).matchData.map(_.group(1)).toSeq

  private def filterLinksWithProtocols(resourcesWithLinks: Seq[ResourceWithLinks]): Seq[ResourceWithLinks] =
    resourcesWithLinks.map(r => r.copy(links = r.links.filterNot(isExternalLink)))

  private def isExternalLink(link: String): Boolean = protocols.exists(p => link.startsWith(p))

  private def filterLinksToTheSameDocument(resourceWithLinks: ResourceWithLinks) =
    resourceWithLinks.copy(links = resourceWithLinks.links.filterNot(_.startsWith("#")))
}
