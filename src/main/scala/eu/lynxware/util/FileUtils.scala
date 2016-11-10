package eu.lynxware.util

import java.io._
import java.nio.channels.Channels
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.{ZipEntry, ZipOutputStream}

import eu.lynxware.util.Helpers._

import scala.util.Random
import scala.xml.{Node, PrettyPrinter}

object FileUtils {

  private val tmp = System.getProperty("java.io.tmpdir")

  private val xmlEncoding = """<?xml version="1.0" encoding="UTF-8"?>"""

  def getTmpFolder(): Path = Paths.get(tmp)

  def getRandomTmpFolder(): Path = Paths.get(tmp).resolve(Random.alphanumeric.take(10).mkString(""))

  def createDirectory(path: Path): Either[IOException, Path] =
    handling(classOf[IOException])(Files.createDirectories(path))

  def createFile(path: Path): Either[IOException, Path] =
    handling(classOf[IOException])(Files.createFile(path))

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
    writeXmlToFile(path, () => {new PrettyPrinter(120, 2).format(node)})

  def writeXmlToFile(path: Path, nodes: Seq[Node]): Unit =
    writeXmlToFile(path, () => {new PrettyPrinter(120, 2).formatNodes(nodes)})

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

  def packToZip(input: Path, output: Path): Unit = {
    val fos = newFileOutputStream(output)
    val zos = new ZipOutputStream(fos)
    try {
      Files.walkFileTree(input, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult = {
          zos.putNextEntry(new ZipEntry(input.relativize(file).toString))
          Files.copy(file, zos)
          zos.closeEntry()
          FileVisitResult.CONTINUE
        }

        override def preVisitDirectory(dir: Path, basicFileAttributes: BasicFileAttributes): FileVisitResult = {
          zos.putNextEntry(new ZipEntry(input.relativize(dir).toString + "/"))
          zos.closeEntry()
          FileVisitResult.CONTINUE
        }
      })
    } finally {
      zos.close()
    }
  }

  def copy(in: Path, out: Path): Unit = {
    val fis = newFileInputStream(in)
    val fos = newFileOutputStream(out)
    copy(fis, fos)
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
