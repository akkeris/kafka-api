package daos

import anorm._
import java.sql.Connection

class MigrationDao {

  def insertStage1MigrationTopicsToMigrationTable(cluster: String)(implicit conn: Connection) = {
    SQL"""
         INSERT INTO migration (topic_id, topic, cluster, status)
         SELECT topic_id, topic, cluster, 'require_migration' FROM topic WHERE cluster = $cluster AND config_name = 'state'
         ON CONFLICT ON CONSTRAINT migration_topic_id_key DO NOTHING
         RETURNING topic;
      """.execute()
    SQL"SELECT topic FROM migration where status = 'require_migration' AND cluster = $cluster"
      .as(SqlParser.str("topic").*)
  }

  def insertStage1MigrationTopicsToMigrationTable(cluster: String, topics: List[String])(implicit conn: Connection) = {
    SQL"""
         INSERT INTO migration (topic_id, topic, cluster, status)
         SELECT topic_id, topic, cluster, 'require_migration' FROM topic
         WHERE cluster = $cluster AND config_name = 'state' and topic in ($topics)
         ON CONFLICT ON CONSTRAINT migration_topic_id_key DO NOTHING RETURNING topic;
      """
      .execute()
    SQL"""SELECT topic FROM migration
          WHERE status = 'require_migration' AND cluster = $cluster
          AND topic in ($topics)
      """
      .as(SqlParser.str("topic").*)
  }

  def changeStatus(topic: String, status: String)(implicit conn: Connection): Int = {
    SQL"Update migration SET status = $status WHERE topic = $topic;"
      .executeUpdate()
  }

  def changeStatus(topics: List[String], status: String)(implicit conn: Connection): Int = {
    SQL"Update migration SET status = $status WHERE topic in ($topics);"
      .executeUpdate()
  }

  def getTopicsForStage2Migration()(implicit conn: Connection): List[String] = {
    SQL"SELECT topic FROM migration WHERE status = 'stage1_complete'".as(SqlParser.str("topic").*)
  }

  def getTopicsForStage2Migration(topics: List[String])(implicit conn: Connection): List[String] = {
    SQL"SELECT topic FROM migration WHERE status = 'stage1_complete' AND topic in ($topics)".as(SqlParser.str("topic").*)
  }
}
