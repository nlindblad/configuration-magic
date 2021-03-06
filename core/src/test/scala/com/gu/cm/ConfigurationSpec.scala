package com.gu.cm

import com.typesafe.config.{ConfigValueFactory, ConfigFactory, Config}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ConfigurationSpec extends Specification {

  "a Configuration object" should {
    "compose from multiple sources" in new ConfigurationScope() {
      val source1 = config(Map("src.1.a" -> "a", "src.1.b" -> 2))
      val source2 = config(Map("src.2.a" -> "z", "src.2.b" -> 54))
      val configuration = new Configuration(List(source1, source2)).load

      configuration.getString("src.1.a") shouldEqual "a"
      configuration.getInt("src.1.b") shouldEqual 2
      configuration.getString("src.2.a") shouldEqual "z"
      configuration.getInt("src.2.b") shouldEqual 54
    }
    "respect the order of the sources" in new ConfigurationScope() {
      val source1 = config(Map("src.1.a" -> "a", "src.1.b" -> 2))
      val source2 = config(Map("src.1.a" -> "ignore-me", "src.2.b" -> 54))
      val configuration = new Configuration(List(source1, source2)).load

      configuration.getString("src.1.a") shouldEqual "a"
      configuration.getInt("src.1.b") shouldEqual 2
      configuration.getInt("src.2.b") shouldEqual 54
    }
  }

  trait ConfigurationScope extends Scope {
    def config(values: Map[String, Any]): ConfigurationSource = new ConfigurationSource {
      override def load: Config = values.foldLeft(ConfigFactory.empty()) {
        case (agg, (path, value)) => agg.withValue(path, ConfigValueFactory.fromAnyRef(value, "Unit test"))
      }
    }
  }

}
