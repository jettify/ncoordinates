package net.jettify.ncoordinates.vivaldi

case class Config(
  dimensionality: Int = 8,
  vivaldiErrorMax: Double = 1.5,
  vivaldiCE: Double = 0.25,
  vivaldiCC: Double = 0.25,
  adjustmentWindowSize: Int = 20,
  heightMin: Double = 10.0e-6,
  latencyFilterSize: Int = 3,
  gravityRho: Double = 150.0
)

class ConfigBuilder() {
  private var dimensionality: Int = 8
  private var vivaldiErrorMax: Double = 1.5
  private var vivaldiCE: Double = 0.25
  private var vivaldiCC: Double = 0.25
  private var adjustmentWindowSize: Int = 20
  private var heightMin: Double = 10.0e-6
  private var latencyFilterSize: Int = 3
  private var gravityRho: Double = 150.0

  def withDimensionality(d: Int): this.type = {
    dimensionality = d
    this
  }
  def withVivaldiErrorMax(d: Double): this.type = {
    vivaldiErrorMax = d
    this
  }
  def withHeightMin(d: Double): this.type = {
    heightMin = d
    this
  }

  def build(): Config = {
    Config(dimensionality, vivaldiErrorMax, vivaldiCE,
      vivaldiCC, adjustmentWindowSize,
      heightMin, latencyFilterSize, gravityRho)
  }

}
