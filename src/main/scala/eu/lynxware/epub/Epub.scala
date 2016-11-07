package eu.lynxware.epub

import java.nio.file.Path

import eu.lynxware.epub.file.{ContainerFile, ContainerRootFile, MimetypeFile, OpfFile}
import eu.lynxware.util.FileUtils

class Epub(mimetype: MimetypeFile = MimetypeFile(),
           container: ContainerFile = ContainerFile(Seq(ContainerRootFile("EPUB/package.opf", "application/oebps-package+xml"))),
           opfFile: OpfFile = OpfFile()) {

  def createFolderStructure(path: Path): Unit = {
    FileUtils.createDirectory(path.resolve("META-INF"))
    FileUtils.createDirectory(path.resolve("EPUB")).right.map { epubPath =>
      FileUtils.createDirectory(epubPath.resolve("css"))
      FileUtils.createDirectory(epubPath.resolve("xhtml"))
      FileUtils.createDirectory(epubPath.resolve("img"))
    }
  }

  def createPageFile(path: Path, title: String): Unit = {
    val filepath = FileUtils.createFile(path.resolve("titlepage.xhtml")).left.get
    val content = <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <meta charset="utf-8"/>
        <title>
          {title}
        </title>
      </head>
      <body>
        <h1 class="titlepage">
          {title}
        </h1>
        <p>Hello world!</p>
      </body>
    </html>
    //writeXmlToFile(content, filepath)
  }
}
