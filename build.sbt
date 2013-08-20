// Copyright (c) 2013 Geoff Ballinger. All rights reserved.

// See https://github.com/sbt/sbt-assembly
import AssemblyKeys._
assemblySettings

jarName in assembly := "simple-sharder.jar"

name := "simple-sharder"

version := "0.1"

scalaVersion := "2.10.2"

externalIvyFile()
