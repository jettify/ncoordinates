package net.jettify.ncoordinates.vivaldi

import net.jettify.ncoordinates.NCoordinatesTestSuite

class CoordinateTest extends NCoordinatesTestSuite {

  test("Instance creation") {
    val config = new ConfigBuilder().build()
    val c1 = Coordinate(config)
    assert(c1.vec.length == 8)
  }
}
