package eu.lynxware.util

import java.nio.file.{Files, Path, Paths}

import scala.util.{Failure, Success, Try}

object FileUtils {

  def createDirectory(path: String): Either[Path, Throwable] =
    Try(Files.createDirectory(Paths.get(path))) match {
      case Success(p) => Left(p)
      case Failure(e) => Right(e)
    }

  def createFile(path: Path): Either[Path, Throwable] = {
    Try(Files.createFile(path)) match {
      case Success(p) => Left(p)
      case Failure(e) => Right(e)
    }
  }
}
