package eu.lynxware.epub.file

import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

import scala.xml.PrettyPrinter

class ContainerFileSpec extends FunSuite with GivenWhenThen with Matchers {
  test("ContainerFile serialized to xml") {
    Given("sample container file")
    val rootFiles = Seq(ContainerRootFile("EPUB/package.opf", "application/oebps-package+xml"))
    val containerFile = ContainerFile(rootFiles)

    When("container file is serialized to xml")
    val xml = containerFile.toXml

    Then("container file is a valid xml")
    val pp = new PrettyPrinter(120, 2)
    println(pp.format(xml))
  }
}
