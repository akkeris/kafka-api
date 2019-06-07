package helper

import java.io.File

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.scalatest.{ BeforeAndAfterAll, TestSuite }
import play.api.db.Databases
import play.api.db.evolutions.Evolutions

object PostgresSuiteSingleton {
  val dbname = "default"
  lazy val pg: EmbeddedPostgres = EmbeddedPostgres.builder()
    .setErrorRedirector(ProcessBuilder.Redirect.to(new File("/dev/null")))
    .setOutputRedirector(ProcessBuilder.Redirect.to(new File("/dev/null")))
    .start
  lazy val port = pg.getPort
}

trait PostgresTestSpec extends TestSuite with BeforeAndAfterAll {
  val db = Databases(
    driver = "org.postgresql.Driver",
    url = s"jdbc:postgresql://localhost:${PostgresSuiteSingleton.port}/postgres",
    name = databaseName,
    config = Map(
      "username" -> "postgres",
      "password" -> "postgres"
    )
  )

  override protected def beforeAll(): Unit = applyEvolutions

  override protected def afterAll(): Unit = {
    db.shutdown()
  }

  def applyEvolutions(): Unit = {
    Evolutions.applyEvolutions(db)
  }

  //Override to run evolutions other than default
  protected def databaseName = PostgresSuiteSingleton.dbname
}

