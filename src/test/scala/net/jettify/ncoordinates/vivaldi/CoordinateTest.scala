package net.jettify.ncoordinates.vivaldi

import net.jettify.ncoordinates.NCoordinatesTestSuite
import org.scalactic._
import scala.concurrent.duration._

class CoordinateTest extends NCoordinatesTestSuite {

  private val epsilon: Double = 1e-6f
  private implicit val doubleEquality = TolerantNumerics.tolerantDoubleEquality(epsilon)

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

  test("Test applyForce") {
    val config = new ConfigBuilder()
      .withDimensionality(3)
      .withHeightMin(0)
      .build()

    var origin = Coordinate(config)

    // This proves that we normalize, get the direction right, and apply the
    // force multiplier correctly.
    val aboveVec = Seq(0.0, 0.0, 2.9)
    val above = Coordinate(config).copy(vec = aboveVec)
    var c = origin.applyForce(config, 5.3, above)

    assert(c.vec == Seq(0.0, 0.0, -5.3))

    // Scoot a point not starting at the origin to make sure there's nothing
    // special there.
    val rightVec = Seq(3.4, 0.0, -5.3)
    val right = Coordinate(config).copy(vec = rightVec)
    c = c.applyForce(config, 2.0, right)
    assert(c.vec == Seq(-2.0, 0.0, -5.3))

    // If the points are right on top of each other, then we should end up
    // in a random direction, one unit away. This makes sure the unit vector
    // build up doesn't divide by zero.
    c = origin.applyForce(config, 1.0, origin)
    assert(origin.distanceTo(c).toUnit(SECONDS) === 1.0)

    val configH = new ConfigBuilder()
      .withDimensionality(3)
      .withHeightMin(10.0e-6)
      .build()

    val originH = Coordinate(configH)
    c = origin.applyForce(configH, 5.3, above)
    assert(c.vec == Seq(0.0, 0.0, -5.3))
    // TODO: fix this assert
    // assert(c.height == configH.heightMin + 5.3 * configH.heightMin / 2.9)

    c = origin.applyForce(configH, -5.3, above)
    assert(c.vec == Seq(0.0, 0.0, 5.3))
    assert(c.height == configH.heightMin)

    val c3 = Coordinate(new ConfigBuilder().withDimensionality(8).build())
    intercept[RuntimeException] {
      c.distanceTo(c3)
    }
  }

  test("Test utils") {
    import Coordinate._
    var vec = add(Seq(1, 2, 3), Seq(3, 4, 5))
    assert(vec === Seq(4, 6, 8))

    vec = diff(Seq(1, 2, 3), Seq(3, 4, 5))
    assert(vec === Seq(-2, -2, -2))

    vec = mul(Seq(1, 2, 3), 2)
    assert(vec === Seq(2, 4, 6))

    assert(magnitude(Seq(1, 1, 1, 1)) == 2)

    val expected = (Seq(0.5773502691896258, 0.5773502691896258, 0.5773502691896258),
      1.7320508075688772)
    assert(unitVectorAt(Seq(1, 1, 1), Seq(0, 0, 0)) == expected)
  }

  test("Test unitVectorAtt") {
    import Coordinate._
    val vec1 = Seq(1.0, 2.0, 3.0)
    val vec2 = Seq(0.5, 0.6, 0.7)
    var (u, mag) = unitVectorAt(vec1, vec2)
    assert(u === Seq(0.18257418583505536, 0.511207720338155, 0.8398412548412546))
    assert(magnitude(u) === 1.0)
    assert(mag == magnitude(diff(vec1, vec2)))
    // If we give positions that are equal we should get a random unit vector
    // returned to us, rather than a divide by zero.
    var (u1, mag1) = unitVectorAt(vec1, vec1)
    assert(magnitude(u1) === 1.0)
    assert(mag1 == 0.0)
  }
}
