name := "TrailHead"

version := "1.0"

scalaVersion := "2.9.2"

scalaSource in Compile <<= baseDirectory(_ / "src")

scalacOptions ++= Seq("-unchecked", "-Ywarn-dead-code", "-deprecation")

libraryDependencies  ++= Seq(
            // other dependencies here
            // pick and choose:
			"org.scalanlp" %% "breeze-math" % "0.1",
            "org.scalanlp" %% "breeze-learn" % "0.1",
            "org.scalanlp" %% "breeze-process" % "0.1",
			"net.databinder" %% "unfiltered-filter" % "0.6.4",
			"net.databinder" %% "unfiltered-json" % "0.6.4",
			"net.databinder" %% "unfiltered-jetty" % "0.6.4"
)

resolvers ++= Seq(
            // other resolvers here
            // if you want to use snapshot builds (currently 0.2-SNAPSHOT), use this.
            "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

// The main class
mainClass in (Compile, run) := Some("web.Main")
//mainClass in (Compile, run) := Some("paper.Main")

// The sources to be watched
// watchSources <+= baseDirectory map { _ / "paper" }
// watchSources <+= baseDirectory map { _ / "web" }
// watchSources <+= baseDirectory map { _ / "paper/Linking" }
// watchSources <+= baseDirectory map { _ / "paper/Loading" }
// watchSources <+= baseDirectory map { _ / "paper/Parsing" }