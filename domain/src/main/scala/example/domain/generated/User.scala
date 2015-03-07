package example.domain.generated

import scalikejdbc._
import org.joda.time.{DateTime}

case class User(
  id: Int, 
  name: String, 
  createdAt: DateTime) {

  def save()(implicit session: DBSession): User = User.save(this)(session)

  def destroy()(implicit session: DBSession): Unit = User.destroy(this)(session)

}
      

object User extends SQLSyntaxSupport[User] {

  override val tableName = "users"

  override val columns = Seq("id", "name", "created_at")

  def apply(u: SyntaxProvider[User])(rs: WrappedResultSet): User = apply(u.resultName)(rs)
  def apply(u: ResultName[User])(rs: WrappedResultSet): User = new User(
    id = rs.get(u.id),
    name = rs.get(u.name),
    createdAt = rs.get(u.createdAt)
  )
      
  val u = User.syntax("u")

  override val autoSession = AutoSession

  def find(id: Int)(implicit session: DBSession): Option[User] = {
    withSQL {
      select.from(User as u).where.eq(u.id, id)
    }.map(User(u.resultName)).single.apply()
  }
          
  def findAll()(implicit session: DBSession): List[User] = {
    withSQL(select.from(User as u)).map(User(u.resultName)).list.apply()
  }
          
  def countAll()(implicit session: DBSession): Long = {
    withSQL(select(sqls"count(1)").from(User as u)).map(rs => rs.long(1)).single.apply().get
  }
          
  def findBy(where: SQLSyntax)(implicit session: DBSession): Option[User] = {
    withSQL {
      select.from(User as u).where.append(sqls"${where}")
    }.map(User(u.resultName)).single.apply()
  }
      
  def findAllBy(where: SQLSyntax)(implicit session: DBSession): List[User] = {
    withSQL {
      select.from(User as u).where.append(sqls"${where}")
    }.map(User(u.resultName)).list.apply()
  }
      
  def countBy(where: SQLSyntax)(implicit session: DBSession): Long = {
    withSQL {
      select(sqls"count(1)").from(User as u).where.append(sqls"${where}")
    }.map(_.long(1)).single.apply().get
  }
      
  def create(
    name: String,
    createdAt: DateTime)(implicit session: DBSession): User = {
    val generatedKey = withSQL {
      insert.into(User).columns(
        column.name,
        column.createdAt
      ).values(
        name,
        createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    User(
      id = generatedKey.toInt, 
      name = name,
      createdAt = createdAt)
  }

  def save(entity: User)(implicit session: DBSession): User = {
    withSQL {
      update(User).set(
        column.id -> entity.id,
        column.name -> entity.name,
        column.createdAt -> entity.createdAt
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }
        
  def destroy(entity: User)(implicit session: DBSession): Unit = {
    withSQL { delete.from(User).where.eq(column.id, entity.id) }.update.apply()
  }
        
}
