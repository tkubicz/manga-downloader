package eu.lynxware.epub

import java.io.{BufferedWriter, FileWriter}
import java.nio.file.Path

import eu.lynxware.util.FileUtils

class Epub {
  def createBasicFileStructure(path: String): Unit = {
    FileUtils.createDirectory(path) match {
      case Left(dir) =>
      case Right(e) =>
    }
  }

  def createMimetypeFile(path: Path): Unit = {
    val filepath = FileUtils.createFile(path.resolve("mimetype")).left.get
    val file = filepath.toFile
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write("application/epub+zip")
    bw.close()
  }
}
