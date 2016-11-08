package eu.lynxware.epub.file

import scala.xml.Elem

case class ContainerRootFile(fullPath: String, mediaType: String) {
  def toXml(): Elem = <rootfile full-path={fullPath} media-type={mediaType}/>
}

case class ContainerFile(rootFiles: Seq[ContainerRootFile] = Seq(ContainerRootFile("EPUB/package.opf", "application/oebps-package+xml"))) {
  def toXml(): Elem =
    <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
      <rootfiles>
        {rootFiles.map(_.toXml)}
      </rootfiles>
    </container>
}
