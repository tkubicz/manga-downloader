package eu.lynxware.epub

import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class OpfFileTest extends FunSuite with Matchers with GivenWhenThen {
  test("Creating Opf file using builder") {
    val opf = OpfFile()
      .withFileName("test.opf")
      .withAuthor("TK")
      .withTitle("Test Title")
      .withCreator("manga-downloader")
      .withManifestItem("xhtml/test1.xhtml", "tt1", "application/xhtml+xml", Some(OpfManifestItemProperty.NAV))
      .withManifestItem(OpfManifestItem("xhtml/test2.xhtml", "tt2", "application/xhtml+xml", None))
      .withManifestItem(OpfManifestItem("xhtml/test3.xhtml", "tt3", "application/xhtml+xml", None))
      .withSpineItem(OpfSpineItem("tt1", ""))
      .withSpineItem(OpfSpineItem("tt2", ""))
      .withSpineItem(OpfSpineItem("tt3", ""))

    println(opf)
  }
}
