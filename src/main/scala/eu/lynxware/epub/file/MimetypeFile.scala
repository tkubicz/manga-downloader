package eu.lynxware.epub.file

case class MimetypeFile(content: String = "application/epub+zip")

object MimetypeFile {
  val FileName: String = "mimetype"
}