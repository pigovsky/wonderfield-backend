name := "wonderfield-backend"

version := "1.0"
scalaVersion := "2.11.6"

lazy val root = (project in file("."))

/*
sourceGenerators in Compile <+= (sourceManaged in Compile) map { d =>
  val v = Process("git describe --tags").lines.head
  val file = d / "BuildInfo.scala"
  IO.write(file, """package com.pigovsky.wonderfield.backend
                   |object BuildInfo {
                   |  val version = "%s"
                   |}
                   | """.stripMargin.format(v))
  Seq(file)
}
*/

libraryDependencies ++= Seq(
  "com.google.firebase" % "firebase-server-sdk" % "3.0.1"  withSources() withJavadoc()
)
