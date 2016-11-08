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
        FileUtils.copy(in, out)
      } finally {
        out.close
      }
    } finally {
      in.close
    }
  }


}
