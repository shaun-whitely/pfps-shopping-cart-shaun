import Dependencies._

scalaVersion := "2.13.1"
version := "0.1.0-SNAPSHOT"
organization := "au.id.smw"
organizationName := "Shaun Whitely"

libraryDependencies ++= Seq(
  compilerPlugin(Libraries.kindProjector cross CrossVersion.full),
  compilerPlugin(Libraries.betterMonadicFor),
  Libraries.cats,
  Libraries.catsEffect,
  Libraries.catsMeowMtl,
  Libraries.catsRetry,
  Libraries.circeCore,
  Libraries.circeGeneric,
  Libraries.circeParser,
  Libraries.circeRefined,
  Libraries.cirisCore,
  Libraries.cirisEnum,
  Libraries.cirisRefined,
  Libraries.fs2,
  Libraries.http4sDsl,
  Libraries.http4sServer,
  Libraries.http4sClient,
  Libraries.http4sCirce,
  Libraries.http4sJwtAuth,
  Libraries.javaxCrypto,
  Libraries.log4cats,
  Libraries.logback % Runtime,
  Libraries.newtype,
  Libraries.redis4catsEffects,
  Libraries.redis4catsLog4cats,
  Libraries.refinedCore,
  Libraries.refinedCats,
  Libraries.skunkCore,
  Libraries.skunkCirce,
  Libraries.squants,
  Libraries.scalaCheck    % Test,
  Libraries.scalaTest     % Test,
  Libraries.scalaTestPlus % Test
)

scalacOptions += "-Ymacro-annotations"
