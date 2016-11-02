package eu.lynxware.epub

import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.file.Path
import java.util.UUID

import eu.lynxware.util.FileUtils

import scala.xml.{Node, PrettyPrinter}

class Epub {
  val encoding = "UTF-8"

  def createBasicFileStructure(path: String): Unit = {
    FileUtils.createDirectory(path) match {
      case Left(dir) =>
      case Right(e) =>
    }
  }

  def createMimetypeFile(path: Path): Unit = {
    val filepath = FileUtils.createFile(path.resolve("mimetype")).left.get
    writeContentToFile(filepath, "application/epub+zip")
  }

  def createContainerFile(path: Path): Unit = {
    val filepath = FileUtils.createFile(path.resolve("container.xml")).left.get
    val content = <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
      <rootfiles>
        <rootfile full-path="EPUB/package.opf" media-type="application/oebps-package+xml"/>
      </rootfiles>
    </container>
    writeXmlToFile(content, filepath)
  }

  def createOpfFile(path: Path, title: String, creator: String, language: String): Unit = {
    val filepath = FileUtils.createFile(path.resolve("package.opf")).left.get
    val content = <package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="pub-id">
      <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:identifier id="pub-id">urn:uuid:{UUID.randomUUID()}</dc:identifier>
        <meta refines="#pub-id" property="identifier-type" scheme="xsd:string">uuid</meta>
        <dc:title>{title}</dc:title>
        <dc:creator>{creator}</dc:creator>
        <dc:language>{language}</dc:language>
        <meta property="dcterms:modified">2000-03-24T00:00:00Z</meta>
      </metadata>
      <manifest>
      </manifest>
      <spine>
      </spine>
    </package>
    writeXmlToFile(content, filepath)
    }

  def createPageFile(path: Path, title: String): Unit = {
    val filepath = FileUtils.createFile(path.resolve("titlepage.xhtml")).left.get
    val content = <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <meta charset="utf-8"/>
        <title>{title}</title>
      </head>
      <body>
        <h1 class="titlepage">{title}</h1>
        <p>Hello world!</p>
      </body>
      </html>
    writeXmlToFile(content, filepath)
  }

  private def writeContentToFile(path: Path, content: String): Unit = {
    val fos = new FileOutputStream(path.toFile)
    val writer = Channels.newWriter(fos.getChannel, encoding)

    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }

  private def writeXmlToFile(node: Node, path: Path): Unit = {
    val pp = new PrettyPrinter(120, 2)
    val fos = new FileOutputStream(path.toFile)
    val writer = Channels.newWriter(fos.getChannel, encoding)

    try {
      writer.write(s"""<?xml version="1.0" encoding="$encoding"?>\n""")
      writer.write(pp.format(node))
    } finally {
      writer.close()
    }
  }
}
