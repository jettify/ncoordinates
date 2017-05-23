package net.jettify.ncoordinates.vivaldi

import Coordinate._

import scala.concurrent.duration._
import scala.util.Random

case class Coordinate(vec: Seq[Double], var error: Double, var adjustment: Double, height: Double) {

  def this(config: Config) = this(
    Seq.fill(config.dimensionality)(0.0),
    config.vivaldiErrorMax, 0.0, config.heightMin
  )

  def applyForce(config: Config, force: Double, other: Coordinate): Coordinate = {
    if (other.vec.length != vec.length) {
      throw new RuntimeException("Not compatible")
    }

    val (unit, mag) = unitVectorAt(vec, other.vec)
    val newVec = add(vec, mul(unit, force))

    val newHeight = if (mag > zeroThreshold) {
      Math.max(
        (height + other.height) * force / mag + height,
        config.heightMin
      )
    } else {
      height
    }

    Coordinate(newVec, error, adjustment, newHeight)
  }

  def distanceTo(other: Coordinate): Duration = {
    if (other.vec.length != vec.length) {
      throw new RuntimeException("Not compatible")
    }

    val dist = rawDistanceTo(other)
    val adjDist = dist + adjustment + other.adjustment
    val fdist = if (adjDist > 0) adjDist else dist

    dist.nanoseconds
  }

  def rawDistanceTo(other: Coordinate): Double = {
    magnitude(diff(vec, other.vec)) + height + other.height
  }

}

object Coordinate {

  private val zeroThreshold = 1.0e-6
  private val secondsToNanoseconds = 1.0e9

  def apply(config: Config): Coordinate = new Coordinate(config)

  private def add(v1: Seq[Double], v2: Seq[Double]): Seq[Double] = {
    v1.zip(v2).map({ case (x, y) => x + y })
  }

  private def diff(v1: Seq[Double], v2: Seq[Double]): Seq[Double] = {
    v1.zip(v2).map({ case (x, y) => x - y })
  }

  private def mul(v: Seq[Double], factor: Double): Seq[Double] = {
    v.map(x => x * factor)
  }

  private def magnitude(v: Seq[Double]) = {
    Math.sqrt(v.foldLeft(0.0) { (acc, i) => acc + i * i })
  }

  private def unitVectorAt(v1: Seq[Double], v2: Seq[Double]): (Seq[Double], Double) = {
    val r = diff(v1, v2)
    val mag = magnitude(r)
    if (mag > zeroThreshold) {
      (mul(r, 1.0 / mag), mag)
    } else {
      val randomVector: Seq[Double] = Seq.fill(v1.length)(Random.nextDouble() - 0.5)
      val randomMag = magnitude(randomVector)

      if (randomMag > zeroThreshold) {
        (mul(randomVector, 1.0 / randomMag), 0)

      } else {
        val unit: Seq[Double] = Seq(1.0) ++ Seq.fill(v1.length - 1)(0.0)
        (unit, 0.0)
      }
    }
  }
}

