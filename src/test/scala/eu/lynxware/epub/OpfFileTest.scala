package eu.lynxware.epub

import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

import scala.xml.PrettyPrinter

class OpfFileTest extends FunSuite with Matchers with GivenWhenThen {
  test("Creating Opf file using builder") {
    val opf = OpfFile()
      .withFileName("test.opf")
      .withTitle("Test Title")
      .withCreator("manga-downloader")
      .withManifestItem("xhtml/test1.xhtml", "tt1", "application/xhtml+xml", Some(OpfManifestItemProperty.Nav))
      .withManifestItem(OpfManifestItem("xhtml/test2.xhtml", "tt2", "application/xhtml+xml", None))
      .withManifestItem(OpfManifestItem("xhtml/test3.xhtml", "tt3", "application/xhtml+xml", None))
      .withSpineItem(OpfSpineItem("tt1"))
      .withSpineItem(OpfSpineItem("tt2"))
      .withSpineItem(OpfSpineItem("tt3"))

    println(opf)

    val xmlNodes = opf.toXml
    val pp = new PrettyPrinter(120, 2)
    println(pp.formatNodes(xmlNodes))
  }

  test("Converting OpfManifestItem to XML element without properties") {
    Given("sample OpfManifestItem")
    val href = "xhtml/test.xhtml"
    val id = "tt1"
    val mediaType = "application/xhtml+xml"
    val item = OpfManifestItem(href, id, mediaType)

    When("OpfManifestItem is serialized to xml")
    val xml = item.toXml()

    Then("xml should be valid")
    xml.attribute("href").get.head.text should equal(href)
    xml.attribute("id").get.head.text should equal(id)
    xml.attribute("media-type").get.head.text should equal(mediaType)
    xml.attribute("properties") should equal(None)
  }

  test("Converting OpfManifestItem to xml elem with properties") {
    Given("sample OpfManifestElement with properties")
    val href = "xhtml/test.xhtml"
    val id = "tt1"
    val mediaType = "application/xhtml+xml"
    val property = Some(OpfManifestItemProperty.CoverImage)
    val item = OpfManifestItem(href, id, mediaType, property)

    When("OpfManifestItem is serialized to xml")
    val xml = item.toXml()

    Then("xml should be valid")
    xml.attribute("href").get.head.text should equal(href)
    xml.attribute("id").get.head.text should equal(id)
    xml.attribute("media-type").get.head.text should equal(mediaType)
    xml.attribute("properties").get.head.text should equal(property.get.toString)
  }
}
