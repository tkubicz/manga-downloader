package eu.lynxware.epub.file

import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

import scala.xml.PrettyPrinter

class OpfFileSpec extends FunSuite with Matchers with GivenWhenThen {
  test("Creating Opf file using builder") {
    Given("empty OpfFile")
    val opf = OpfFile()

    When("builder is used")
    val fileName = "test.opf"
    val title = "Test Title"
    val creator = "manga-downloader"

    val metadata = OpfMetadata().withTitle(title).withCreator(creator)
    val manifestItems = Seq(
      OpfManifestItem("xhtml/test1.xhtml", "tt1", MediaTypes.Application.XhtmlXml, Some(OpfManifestItemProperty.Nav)),
      OpfManifestItem("xhtml/test2.xhtml", "tt2", MediaTypes.Application.XhtmlXml, None),
      OpfManifestItem("xhtml/test3.xhtml", "tt3", MediaTypes.Application.XhtmlXml, None)
    )
    val spineItems = Seq(OpfSpineItem("tt1"), OpfSpineItem("tt2"), OpfSpineItem("tt3"))

    val newOpf = opf
      .withFileName(fileName)
      .withMetadata(metadata)
      .withManifestItems(manifestItems)
      .withSpineItems(spineItems)

    Then("OpfFile is builded")
    newOpf should have(
      'fileName (fileName),
      'manifestItems (manifestItems),
      'spineItems (spineItems)
    )
    newOpf.metadata should have(
      'title (title),
      'creator (creator)
    )

    val xmlNodes = newOpf.toXml()
    val pp = new PrettyPrinter(120, 2)
    println(pp.formatNodes(xmlNodes))
  }

  test("Converting OpfManifestItem to XML element without properties") {
    Given("sample OpfManifestItem")
    val href = "xhtml/test.xhtml"
    val id = "tt1"
    val mediaType = MediaTypes.Application.XhtmlXml
    val item = OpfManifestItem(href, id, mediaType)

    When("OpfManifestItem is serialized to xml")
    val xml = item.toXml()

    Then("xml should be valid")
    xml.attribute("href").get.head.text should equal(href)
    xml.attribute("id").get.head.text should equal(id)
    xml.attribute("media-type").get.head.text should equal(mediaType.toString)
    xml.attribute("properties") should equal(None)
  }

  test("Converting OpfManifestItem to xml elem with properties") {
    Given("sample OpfManifestElement with properties")
    val href = "xhtml/test.xhtml"
    val id = "tt1"
    val mediaType = MediaTypes.Application.XhtmlXml
    val property = Some(OpfManifestItemProperty.CoverImage)
    val item = OpfManifestItem(href, id, mediaType, property)

    When("OpfManifestItem is serialized to xml")
    val xml = item.toXml()

    Then("xml should be valid")
    xml.attribute("href").get.head.text should equal(href)
    xml.attribute("id").get.head.text should equal(id)
    xml.attribute("media-type").get.head.text should equal(mediaType.toString)
    xml.attribute("properties").get.head.text should equal(property.get.toString)
  }
}
