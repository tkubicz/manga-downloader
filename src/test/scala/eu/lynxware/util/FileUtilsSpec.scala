package eu.lynxware.util

import java.io.File
import java.nio.file.Files

import org.scalatest.{FunSuite, GivenWhenThen, Matchers}

class FileUtilsSpec extends FunSuite with GivenWhenThen with Matchers {

  val tmp = System.getProperty("java.io.tmpdir")
  val separator = File.separator

  test("Create directory") {
    val path = s"${tmp}${separator}test_dir"
    val result = FileUtils.createDirectory(path)
    result shouldBe 'Left
    Files.delete(result.left.get)
  }

  test("Failure while creating directory") {
    val result = FileUtils.createDirectory("/opt/test_dir")
    result shouldBe 'Right
  }
}
