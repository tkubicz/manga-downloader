package eu.lynxware.util

import java.io.{FileOutputStream, IOException}
import java.nio.channels.Channels
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.util.zip.{ZipEntry, ZipOutputStream}

import eu.lynxware.util.Helpers._

import scala.xml.{Node, PrettyPrinter}

object FileUtils {

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

  def writeXmlToFile(path: Path, node: Node): Unit = {
    val pp = new PrettyPrinter(120, 2)
    val fos = newFileOutputStream(path)
    val writer = newWriter(fos)
    try {
      writer.write(s"""<?xml version="1.0" encoding="UTF-8"?>\n""")
      writer.write(pp.format(node))
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

  private def newFileOutputStream(path: Path) = new FileOutputStream(path.toFile)
  private def newWriter(fos: FileOutputStream, encoding: String = "UTF-8") = Channels.newWriter(fos.getChannel(), encoding)
}
