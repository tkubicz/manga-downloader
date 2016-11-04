package eu.lynxware.util

import java.io.{File, IOException}
import java.nio.file.{Files, Paths}

import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class FileUtilsSpec extends FunSuite with GivenWhenThen with Matchers {

  val tmp = System.getProperty("java.io.tmpdir")
  val separator = File.separator

  test("Create new directory") {
    Given("path where directory doesn't exist")
    val path = s"${tmp}${separator}test_dir"

    When("directory is created")
    val result = FileUtils.createDirectory(path)

    Then("result should be path to the new directory")
    result shouldEqual Left(Paths.get(path))
    Files.delete(result.left.get)
  }

  test("Failure while creating directory") {
    Given("path without sufficient permissions")
    val path = "/opt/test_dir"

    When("trying to create directory")
    val result = FileUtils.createDirectory("/opt/test_dir")

    Then("result should be exception")
    result shouldEqual Right(_: IOException)
  }
}
