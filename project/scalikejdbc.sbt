enablePlugins(BuildInfoPlugin)

val mysqlDriverVersion = "5.1.39"

buildInfoKeys := Seq[BuildInfoKey](
  "mysqlDriverVersion" -> mysqlDriverVersion
)

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.4.2")
libraryDependencies += "mysql" % "mysql-connector-java" % mysqlDriverVersion
