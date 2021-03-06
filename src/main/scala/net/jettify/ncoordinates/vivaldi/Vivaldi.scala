package net.jettify.ncoordinates.vivaldi

import scala.concurrent.duration._
import scala.concurrent.duration.Duration

class Vivaldi(private val config: Config) {

  if (config.dimensionality <= 0) {
    throw new RuntimeException("Dimensionality must be > 0")
  }

  private var coord = Coordinate(config)
  private var origin = Coordinate(config)
  private var adjustmentIndex = 0
  private var adjustmentSamples: Array[Double] = Array.fill(config.adjustmentWindowSize)(0.0)
  private val latencyFilterSamples = scala.collection.mutable.HashMap.empty[String, Seq[Double]]

  def setCoordinate(c: Coordinate): Unit = coord = c
  def getCoordinate: Coordinate = coord

  private def latencyFilter(nodeId: String, rttSeconds: Double): Double = {
    val samples = latencyFilterSamples
      .getOrElseUpdate(nodeId, Seq.empty[Double])

    val newSamples = if (samples.length >= config.latencyFilterSize) {
      samples.drop(1) ++ Seq(rttSeconds)
    } else {
      samples ++ Seq(rttSeconds)
    }
    latencyFilterSamples(nodeId) = newSamples
    val index: Int = newSamples.length / 2
    val median = newSamples.sorted.toList(index)
    median
  }

  private def updateVivaldi(other: Coordinate, rttSeconds: Double): Unit = {
    val zeroThreshold = 1.0e-6
    val dist = coord.distanceTo(other).toUnit(SECONDS)
    val rtt = Math.max(rttSeconds, zeroThreshold)
    val wrongness = Math.abs(dist - rtt) / rtt
    val totalError = Math.max(coord.error + other.error, zeroThreshold)
    val weight = coord.error / totalError

    val newError = config.vivaldiCE * weight * wrongness +
      coord.error * (1.0 - config.vivaldiCE * weight)
    coord = coord.copy(error = Math.min(coord.error, config.vivaldiErrorMax))

    val delta = config.vivaldiCC * weight
    val force = delta * (rttSeconds - dist)
    coord = coord.applyForce(config, force, other)
  }

  private def updateGravity(): Unit = {
    val dist = origin.distanceTo(coord).toUnit(SECONDS)
    val force = -1.0 * Math.pow(dist / config.gravityRho, 2.0)
    coord = coord.applyForce(config, force, origin)
  }

  private def updateAdjustment(other: Coordinate, rttSeconds: Double) = {
    if (config.adjustmentWindowSize > 0) {
      val dist = coord.rawDistanceTo(other)
      adjustmentSamples(adjustmentIndex) = rttSeconds - dist
      adjustmentIndex = (adjustmentIndex + 1) % config.adjustmentWindowSize
      val sum: Double = adjustmentSamples.toSeq.sum

      val newAdjustment = sum / (2.0 * config.adjustmentWindowSize)
      coord = coord.copy(adjustment = newAdjustment)
    }
  }

  def forgetNode(nodeId: String): Option[Seq[Double]] = {
    latencyFilterSamples.remove(nodeId)
  }

  def distanceTo(other: Coordinate): Duration = {
    coord.distanceTo(other)
  }

  def update(nodeId: String, other: Coordinate, rtt: Duration): Coordinate = {
    val rttSeconds = latencyFilter(nodeId, rtt.toUnit(SECONDS))
    updateVivaldi(other, rttSeconds)
    updateAdjustment(other, rttSeconds)
    updateGravity()
    coord
  }
}
