package net.jettify.ncoordinates.vivaldi

import net.jettify.ncoordinates.NCoordinatesTestSuite
import scala.concurrent.duration._

class CoordinateTest extends NCoordinatesTestSuite {

  test("Instance creation") {
    val config = new ConfigBuilder().build()
    val c1 = Coordinate(config)
    assert(c1.vec.length == 8)
  }

  test("Test compatibleWith") {
    val config = new ConfigBuilder().build()
    val c1 = Coordinate(config)
    val c2 = Coordinate(config)
    assert(c1.vec.length == 8)
    assert(c1.compatibleWith(c2))
    assert(c1.compatibleWith(c1))

    val configOther = new ConfigBuilder()
      .withDimensionality(3)
      .build()
    val cOther = Coordinate(config)
    assert(c1.compatibleWith(cOther))
    assert(c2.compatibleWith(cOther))
    assert(cOther.compatibleWith(c1))

    val copyOther = cOther.copy(error = 1)
    assert(cOther.compatibleWith(copyOther))
  }

  test("Test distance to") {
    val config = new ConfigBuilder()
      .withDimensionality(3)
      .withHeightMin(0)
      .build()

    val c1Vec = Seq(-0.5, 1.3, 2.4)
    val c2Vec = Seq(1.2, -2.3, 3.4)

    var c1 = Coordinate(config).copy(vec = c1Vec)
    var c2 = Coordinate(config).copy(vec = c2Vec)

    c1.distanceTo(c2)
    assert(c1.distanceTo(c1).toUnit(SECONDS) == 0.0)
    assert(c1.distanceTo(c2).toSeconds == c2.distanceTo(c1).toSeconds)
    assert(c1.distanceTo(c2).toUnit(SECONDS) == 4.10487515)

    // Make sure negative adjustment factors are ignored.
    c1 = c1.copy(adjustment = -1.0e6)
    assert(c1.distanceTo(c2).toUnit(SECONDS) == 4.10487515)

    // Make sure positive adjustment factors affect the distance.
    c1 = c1.copy(adjustment = 0.1)
    c2 = c2.copy(adjustment = 0.2)

    assert(c1.distanceTo(c2).toUnit(SECONDS) == 4.40487515)

    // Make sure the heights affect the distance.
    c1 = c1.copy(height = 0.7)
    c2 = c2.copy(height = 0.1)
    assert(c1.distanceTo(c2).toUnit(SECONDS) == 5.20487515)

    val c3 = Coordinate(new ConfigBuilder().withDimensionality(8).build())
    intercept[RuntimeException] {
      c1.distanceTo(c3)
    }
  }

  test("Test raw rawDistanceTo") {
    val config = new ConfigBuilder()
      .withDimensionality(3)
      .withHeightMin(0)
      .build()

    val c1Vec = Seq(-0.5, 1.3, 2.4)
    val c2Vec = Seq(1.2, -2.3, 3.4)

    var c1 = Coordinate(config).copy(vec = c1Vec)
    var c2 = Coordinate(config).copy(vec = c2Vec)

    assert(c1.rawDistanceTo(c1) == 0.0)
    assert(c1.rawDistanceTo(c2) == c2.rawDistanceTo(c1))
    assert(c1.rawDistanceTo(c2) == 4.104875150354758)

    // Make sure negative adjustment factors are ignored.
    c1 = c1.copy(adjustment = -1.0e6)
    assert(c1.rawDistanceTo(c2) == 4.104875150354758)

    // Make sure positive adjustment factors affect the distance.
    c1 = c1.copy(adjustment = 0.1)
    c2 = c2.copy(adjustment = 0.2)

    assert(c1.rawDistanceTo(c2) == 4.104875150354758)

    // Make sure the heights affect the distance.
    c1 = c1.copy(height = 0.7)
    c2 = c2.copy(height = 0.1)
    assert(c1.rawDistanceTo(c2) == 4.904875150354758)
  }
}
