
scalaVersion := "2.13.11"

name := "scala-dev-mooc-2023-09"
organization := "ru.otus"
version := "1.0"

val scala3 = (project in file("scala3"))
  .settings(scalaVersion := "3.3.0")

libraryDependencies += Dependencies.scalaTest
libraryDependencies ++= Dependencies.cats
libraryDependencies ++= Dependencies.zio
libraryDependencies ++= Dependencies.zioConfig

scalacOptions += "-Ymacro-annotations"
