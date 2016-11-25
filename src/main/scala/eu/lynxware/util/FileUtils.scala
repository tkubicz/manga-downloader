package eu.lynxware.util

import java.io._
import java.nio.channels.Channels
import java.nio.file._
import java.nio.file.attribute.{BasicFileAttributes, FileAttribute}
import java.util.zip.{ZipEntry, ZipOutputStream}

import eu.lynxware.util.Helpers._

import scala.util.{Random, Try}
import scala.xml.{Node, PrettyPrinter}

object FileUtils {

  private val tmp = System.getProperty("java.io.tmpdir")

  private val home = System.getProperty("user.home")

  private val xmlEncoding = """<?xml version="1.0" encoding="UTF-8"?>"""

  def tmpDirectory: Path = Paths.get(tmp)

  def randomTmpDirectory: Path = Paths.get(tmp).resolve(Random.alphanumeric.take(10).mkString(""))

  def homeDirectory: Path = Paths.get(home)

  def createDirectory(path: Path): Either[IOException, Path] =
    handling(classOf[IOException])({Files.deleteIfExists(path); Files.createDirectories(path)})

  def createFile(path: Path): Either[IOException, Path] =
    handling(classOf[IOException])({Files.deleteIfExists(path); Files.createFile(path)})

  def getResource(path: String): InputStream = getClass.getResourceAsStream(path)

  def getResourcePath(path: String): Path = Paths.get(getClass.getResource(path).toURI)

  def writeContentToFile(path: Path, content: String): Unit = {
    val fos = newFileOutputStream(path)
    val writer = newWriter(fos)
    try {
      writer.write(content)
    }
    finally {
      writer.close()
    }
  }

  def writeXmlToFile(path: Path, node: Node): Unit =
    writeXmlToFile(path, () => {
      new PrettyPrinter(120, 2).format(node)
    })

  def writeXmlToFile(path: Path, nodes: Seq[Node]): Unit =
    writeXmlToFile(path, () => {
      new PrettyPrinter(120, 2).formatNodes(nodes)
    })

  private def writeXmlToFile(path: Path, formatter: () => String): Unit = {
    val fos = newFileOutputStream(path)
    val writer = newWriter(fos)
    try {
      writer.write(xmlEncoding)
      writer.write("\n")
      writer.write(formatter())
    } finally {
      writer.close()
    }
  }

  def packToZip(input: Path, output: Path, compressionLevel: Int): Unit = {
    val fos = newFileOutputStream(output)
    val zos = new ZipOutputStream(fos)
    zos.setLevel(compressionLevel)
    try {
      Files.walkFileTree(input, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult = {
          zos.putNextEntry(new ZipEntry(input.relativize(file).toString))
          Files.copy(file, zos)
          zos.closeEntry()
          FileVisitResult.CONTINUE
        }

        override def preVisitDirectory(dir: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult = {
          zos.putNextEntry(new ZipEntry(input.relativize(dir).toString + "/")) // TODO: Check if this works on windows
          zos.closeEntry()
          FileVisitResult.CONTINUE
        }
      })
    } finally {
      zos.close()
    }
  }

  def packSingleFileToZip(input: Path, output: Path, compressionLevel: Int): Unit = {
    val fos = newFileOutputStream(output)
    val zos = new ZipOutputStream(fos)
    zos.setLevel(compressionLevel)
    try {
      zos.putNextEntry(new ZipEntry(input.getFileName.toString))
      Files.copy(input, zos)
      zos.closeEntry()
    } finally {
      zos.close()
    }
  }

  def copy(in: Path, out: Path): Unit = {
    val fis = newFileInputStream(in)
    val fos = newFileOutputStream(out)
    Try(Files.copy(fis, out, StandardCopyOption.REPLACE_EXISTING)).recover{case e => println("Error while copping")}
    //copy(fis, fos)
  }

  def copy(in: InputStream, out: OutputStream): Unit = {
    val buf = new Array[Byte](1 << 20) // 1 MB
    while (true) {
      val read = in.read(buf)
      if (read == -1) {
        out.flush
        return
      }
      out.write(buf, 0, read)
    }
  }

  private def newFileOutputStream(path: Path) = new FileOutputStream(path.toFile)

  private def newFileInputStream(path: Path) = new FileInputStream(path.toFile)

  private def newWriter(fos: FileOutputStream, encoding: String = "UTF-8") = Channels.newWriter(fos.getChannel(), encoding)
}
