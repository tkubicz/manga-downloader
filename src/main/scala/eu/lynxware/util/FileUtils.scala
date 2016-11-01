package eu.lynxware.util

import java.io.IOException
import java.nio.file.{Files, Path, Paths}

import scala.util.{Failure, Success, Try}

object FileUtils {

  def createDirectory(path: String): Either[Path, IOException] =
    Try(Files.createDirectory(Paths.get(path))) match {
      case Success(p) => Left(p)
      case Failure(e: IOException) => Right(e)
    }
}
