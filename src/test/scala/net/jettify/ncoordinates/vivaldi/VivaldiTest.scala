package net.jettify.ncoordinates.vivaldi

import net.jettify.ncoordinates.NCoordinatesTestSuite

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
}
