package net.jettify.ncoordinates.vivaldi

import net.jettify.ncoordinates.NCoordinatesTestSuite
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

class VivaldiTest extends NCoordinatesTestSuite {

  test("Instance creation") {
    val config = new ConfigBuilder()
      .withDimensionality(0)
      .build()
    intercept[RuntimeException] {
      new Vivaldi(config)
    }
  }

  test("Instance creation 2") {
    val config = new ConfigBuilder()
      .build()
    new Vivaldi(config)
  }

  test("Vivaldi update") {
    val config = new ConfigBuilder()
      .withDimensionality(3)
      .build()

    val client = new Vivaldi(config)
    var c = client.getCoordinate
    assert(c.vec == Seq(0.0, 0.0, 0.0))

    val other = Coordinate(config).copy(vec = Seq(0.0, 0.0, 0.01))

    val rtt = Duration(2.0 * other.vec(2), SECONDS)
    c = client.update("node", other, rtt)

    assert(c.vec(2) < 0.0)

  }

}
