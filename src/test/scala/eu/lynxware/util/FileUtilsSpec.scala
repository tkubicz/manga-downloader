package eu.lynxware.util

import java.io.{File, IOException}
import java.nio.file.{Files, Paths}

import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class FileUtilsSpec extends FunSuite with GivenWhenThen with Matchers {

  val tmp = System.getProperty("java.io.tmpdir")
  val separator = File.separator

  test("Get tmp folder") {
    When("getTmpFolder() function is executed")
    val tmpFolder = FileUtils.getTmpFolder()

    Then("tmp folder is /tmp")
    tmpFolder.toString should equal("/tmp")
  }

  test("Get random tmp folder") {
    When("random tmp folder is requested")
    val tmpFolder = FileUtils.getRandomTmpFolder()

    println(tmpFolder.toString)
  }

  test("Create new directory") {
    Given("path where directory doesn't exist")
    val path = s"${tmp}${separator}test_dir"

    When("directory is created")
    val result = FileUtils.createDirectory(Paths.get(path))

    Then("result should be path to the new directory")
    result shouldEqual Right(Paths.get(path))
    Files.delete(result.right.get)
  }

  test("Failure while creating directory") {
    Given("path without sufficient permissions")
    val path = "/opt/test_dir"

    When("trying to create directory")
    val result = FileUtils.createDirectory(Paths.get("/opt/test_dir"))

    Then("result should be exception")
    result shouldEqual Left(_: IOException)
  }


}
