import scalariform.formatter.preferences._

name := "ncoordinates"

organization := "net.jettify"

version := "0.0.1"

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.12.2", "2.11.8")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:implicitConversions") ++
  (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, major)) if major >= 11 => Seq("-Ywarn-unused-import")
    case _ => Seq()
   })

scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import"))
scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, false)
  .setPreference(DoubleIndentClassDeclaration, true)


addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17")

scapegoatVersion := "1.3.0"

wartremoverErrors ++= Warts.allBut(Wart.Var, Wart.Throw, Wart.MutableDataStructures,
  Wart.Overloading, Wart.Equals, Wart.NonUnitStatements)

publishMavenStyle := true

publishTo := Some(
  if (version.value.trim.endsWith("SNAPSHOT")) {
    "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  } else {
    "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  }
)

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("MIT License" ->
  url("http://www.opensource.org/licenses/mit-license.php"))

homepage := Some(url("https://github.com/jettify/ncoordinates"))

