import org.flywaydb.sbt.FlywayPlugin.autoImport._
import sbt._, Keys._
import scalikejdbc._
import scalikejdbc.mapper.SbtPlugin.JDBCSettings
import scalikejdbc.mapper._

object build {

  val mysql = "mysql" % "mysql-connector-java" % buildinfo.BuildInfo.mysqlDriverVersion

  private val defaultSchema = "schema_" + System.currentTimeMillis
  val databaseSchema = SettingKey[String]("databaseSchema")
  val host = "localhost"

  val jdbcSettings = Def.setting{
    val schema = databaseSchema.value
    SbtPlugin.JDBCSettings(
      driver = "com.mysql.jdbc.Driver",
      url = s"jdbc:mysql://$host/$schema",
      username = "root",
      password = "",
      schema = schema 
    )
  }

  def generatorSettings(tables: Map[String, String], packageName: String) =
    SbtPlugin.scalikejdbcSettings ++ inConfig(Compile)(Seq(
      SbtKeys.scalikejdbcJDBCSettings := jdbcSettings.value,
      SbtKeys.scalikejdbcGeneratorSettings := null,
      SbtKeys.scalikejdbcCodeGeneratorAll := { (jdbc, _) =>
        val config = GeneratorConfig(
          srcDir = scalaSource.value.getAbsolutePath,
          testTemplate = GeneratorTestTemplate(""),
          packageName = packageName,
          caseClassOnly = true,
          defaultAutoSession = false
        )
        try {
          Class.forName(jdbc.driver)
          val model = Model(jdbc.url, jdbc.username, jdbc.password)
          model.allTables(jdbc.schema).filter(table => tables.contains(table.name)).map { table =>
            new CodeGenerator(table.copy(schema = None), tables.get(table.name))(config)
          }
        } finally {
          scalikejdbc.ConnectionPool.closeAll()
        }
      }
    )) ++ Seq(
      TaskKey[Unit]("showDatabases") := {
        val query = scalikejdbc.SQL[Any]("""SHOW DATABASES;""")
        executeQuery(jdbcSettings.value, query){ (sql, session) =>
          implicit val s = session
          sql.map(_.string(1)).list().apply().foreach(println)
        }
      },
      TaskKey[Unit]("checkGeneratedCode") := {
        val diff = "git diff".!!
        if(diff.nonEmpty){
          sys.error(diff)
        }
      }
    )

  private def executeQuery[A, C](jdbc: JDBCSettings, sql: SQL[A, NoExtractor])(f: (SQL[A, NoExtractor], DBSession) => C): C = {
    try {
      Class.forName(jdbc.driver)
      ConnectionPool.singleton(s"jdbc:mysql://$host/test", jdbc.username, jdbc.password)
      DB.autoCommit { session =>
        f(sql, session)
      }
    } finally {
      ConnectionPool.closeAll()
    }
  }

  private val commonSettings = Seq[Def.Setting[_]](
    scalaVersion := "2.11.8",
    fullResolvers ~= {_.filterNot(_.name == "jcenter")},
    licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
    databaseSchema <<= databaseSchema.??(defaultSchema),
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials"
    ),
    resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
    javacOptions ++= Seq("-encoding", "UTF-8"),
    javaOptions ++= sys.process.javaVmArguments.filter(
      a => Seq("-Xmx", "-Xms", "-XX", "-Xss").exists(a.startsWith)
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    )
  )

  def module(id: String): Project =
    Project(id, file(id)).settings(commonSettings)
}
