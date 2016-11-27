package eu.lynxware.epub

import eu.lynxware.epub.file.OpfManifestItemProperty

object EpubValidator {

  private val exactlyOneNavNeeded: (Epub) => (Boolean, String) =
    e => (e.resources.exists(r => r.property.contains(OpfManifestItemProperty.Nav)), "Exactly one nav property is needed")

  private val atLeastOneSpineItemNeeded: Epub => (Boolean, String) =
    e => (e.resources.exists(r => r.isSpine), "At least one spine item is needed")

  private val validators = Seq(exactlyOneNavNeeded, atLeastOneSpineItemNeeded)

  def validate(epub: Epub): (Boolean, Seq[String]) = {
    val messages = validators.map(v => v(epub)).filter(!_._1).map(_._2)
    if (messages.nonEmpty) (false, messages)
    else (true, Seq.empty)
  }
}
