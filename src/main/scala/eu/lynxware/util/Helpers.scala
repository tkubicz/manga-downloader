package eu.lynxware.util

import scala.util.control.Exception.catching

object Helpers {
  def handling[Ex <: Throwable, T](exType: Class[Ex])(block: => T): Either[Ex, T] =
    catching(exType).either(block).asInstanceOf[Either[Ex, T]]
}
