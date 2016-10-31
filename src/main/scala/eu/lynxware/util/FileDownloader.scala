package eu.lynxware.util

import java.io.{File, FileOutputStream, InputStream, OutputStream}
import java.net.{HttpURLConnection, URL, URLConnection}

object FileDownloader {

  def downloadFile(link: String, file: File): Unit = {
    val conn = new URL(link).openConnection
    try {
      downloadFile(conn, file)
    } finally conn match {
      case conn: HttpURLConnection => conn.disconnect
      case _ =>
    }
  }

  def downloadFile(conn: URLConnection, file: File): Unit = {
    val in = conn.getInputStream
    try {
      val out = new FileOutputStream(file)
      try {
        copy(in, out)
      } finally {
        out.close
      }
    } finally {
      in.close
    }
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
}
