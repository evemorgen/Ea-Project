name := "Ea-Project"

version := "0.1"

scalaVersion := "2.12.7"
cancelable in Global := true

libraryDependencies += "com.concurrentthought.cla" %% "command-line-arguments" % "0.5.0"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
libraryDependencies += "org.clapper" %% "grizzled-slf4j" % "1.3.2"
