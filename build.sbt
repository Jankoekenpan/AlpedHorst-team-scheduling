lazy val root = project.in(file("."))
    .settings(
        name := "Alpe d'Horst team scheduling",
        version := "0.2",
        scalaVersion := "3.1.2",

        libraryDependencies += "com.lihaoyi" %% "ujson" % "2.0.0",
        libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.10",

        libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.11" % "test",
        libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test",
    )