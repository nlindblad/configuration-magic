package com.gu.cm

import com.amazonaws.regions.{Region, Regions}
import com.typesafe.config._
import Mode._

class Configuration(sources: List[ConfigurationSource]) extends ConfigurationSource {
  def load: Config = {
    sources.map(_.load).foldLeft(ConfigFactory.empty()) {
      case (agg, source) => agg.withFallback(source)
    }
  }
}

object Configuration {
  def buildSources(
    mode: Mode,
    identity: Identity,
    region: Region = Region.getRegion(Regions.EU_WEST_1)): List[ConfigurationSource] = {

    lazy val userHome = FileConfigurationSource(s"${System.getProperty("user.home")}/.configuration-magic/${identity.app}")
    lazy val devClassPath = ClassPathConfigurationSource("application-DEV")
    lazy val testClassPath = ClassPathConfigurationSource("application-TEST")
    lazy val classPath = ClassPathConfigurationSource("application")
    lazy val dynamo = DynamoDbConfigurationSource(region, identity)

    mode match {
      case Dev => List(userHome, devClassPath)
      case Test => List(testClassPath)
      case Prod => List(dynamo, classPath)
    }
  }

  def apply(
    mode: Mode,
    identity: Identity,
    region: Region = Region.getRegion(Regions.EU_WEST_1)): Configuration = {
    new Configuration(buildSources(mode, identity, region))
  }
}