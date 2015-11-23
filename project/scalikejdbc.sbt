buildInfoSettings

sourceGenerators in Compile <+= buildInfo

val mysqlDriverVersion = "5.1.37"

buildInfoKeys := Seq[BuildInfoKey](
  "mysqlDriverVersion" -> mysqlDriverVersion
)

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.3.0")
libraryDependencies += "mysql" % "mysql-connector-java" % mysqlDriverVersion
