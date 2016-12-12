package eu.lynxware.epub.writer

import eu.lynxware.epub.file.MediaTypes

object FileStructure {
  private[writer] val MetaInfFolder = "META-INF/"
  private[writer] val ContentFolder = "EPUB/"

  def getDefaultResourceLocation[T <: MediaTypes.MediaType](mediaType: T): String = mediaType match {
    case _: MediaTypes.Application.XhtmlXml.type => "xhtml/"
    case _: MediaTypes.Text.Css.type => "css/"
    case _: MediaTypes.Text.Javascript.type => "js/"
    case _: MediaTypes.MediaTypeImage => "img/"
    case _: MediaTypes.MediaTypeAudio => "audio/"
  }
}
