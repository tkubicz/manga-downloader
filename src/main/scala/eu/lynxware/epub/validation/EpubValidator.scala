package eu.lynxware.epub.validation

import eu.lynxware.epub.Epub
import eu.lynxware.epub.file.OpfManifestItemProperty
import eu.lynxware.epub.validation.LinkValidation._

object EpubValidator {

  private val exactlyOneNavNeeded: (Epub) => (Boolean, String) =
    e => (e.resources.exists(r => r.property.contains(OpfManifestItemProperty.Nav)), "Exactly one nav property is needed")

  private val atLeastOneSpineItemNeeded: Epub => (Boolean, String) =
    e => (e.resources.exists(r => r.isSpine), "At least one spine item is needed")

  private val validators = Seq(exactlyOneNavNeeded, atLeastOneSpineItemNeeded)

  def validate(epub: Epub): (Boolean, Seq[String]) = {

    val test = findAllLinks _ andThen filterOutLinksWithProtocols andThen filterOutLinksToTheSameDocument

    test(epub).foreach { r =>
      println(r.resource)
      r.links.foreach(println)
    }

    val messages = validators.map(v => v(epub)).filter(!_._1).map(_._2)
    if (messages.nonEmpty) (false, messages)
    else (true, Seq.empty)
  }
}
