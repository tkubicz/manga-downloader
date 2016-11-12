package eu.lynxware.epub

import java.io.FileOutputStream
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import eu.lynxware.epub.file.OpfManifestItemMediaType.OpfManifestItemMediaType
import eu.lynxware.epub.file._
import eu.lynxware.util.FileUtils

case class Resource(path: Path, id: String, mediaType: OpfManifestItemMediaType)

case class Epub(metadata: OpfMetadata = OpfMetadata(), resources: Seq[Resource] = Seq()) extends LazyLogging {

  def withMetadata(newMetadata: OpfMetadata): Epub = copy(metadata = newMetadata)

  def addImage(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.ImageJpeg))
  }

  def addStyle(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.TextCss))
  }

  def addSection(path: Path, id: String) = {
    copy(resources = resources :+ Resource(path, id, OpfManifestItemMediaType.ApplicationXhtmlXml))
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
