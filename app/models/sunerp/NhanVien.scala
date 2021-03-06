package models.sunerp

import models.core.{Hash, AbstractQuery, AbstractTable, WithId}
import play.api.db.slick.Config.driver.simple._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Writes, Json}
import dtos.{NhanVienDto, ExtGirdDto, PagingDto}
import org.apache.commons.digester.SimpleRegexMatcher
import org.apache.commons.lang3.StringUtils
import play.api.data.format.Formats._

/**
 * The Class NhanVien.
 *
 * @author Nguyen Duc Dung
 * @since 3/4/14 9:56 AM
 *
 */
case class NhanVien(
                     id: Option[Long] = None,
                     maNv: String,
                     password: String,
                     firstName: String,
                     lastName: String,
                     chucVuId: Long,
                     phongBanId: Long
                     ) extends WithId[Long] {

  private var _quyenHanhs: Option[List[QuyenHanh]] = None
  private var _phongBan: Option[PhongBan] = None

  def quyenHanhs(implicit session: Session): List[QuyenHanh] = _quyenHanhs.getOrElse {
    _quyenHanhs = Some(
      QuyenHanhs.findByChucVuId(chucVuId).filter(_.phongBanId.isEmpty)
        ++ QuyenHanhs.findByChucVuAndPhongBanId(chucVuId, phongBanId)
        ++ QuyenHanhs.findByPhongBanId(phongBanId).filter(_.chucVuId.isEmpty)
    )
    _quyenHanhs.get
  }

  def phongBan(implicit session: Session): PhongBan = _phongBan.getOrElse {
    _phongBan = PhongBans.findById(phongBanId)
    _phongBan.get
  }

  def checkAuth(authority: String)(implicit session: Session) = {
    val matcher = new SimpleRegexMatcher
    quyenHanhs.exists(quyenHanh => StringUtils.isBlank(authority) || matcher.`match`(authority.toLowerCase, quyenHanh.domain.toLowerCase))
  }

  def menuAuth(authority: String)(implicit session: Session) = {
    val matcher = new SimpleRegexMatcher
    quyenHanhs.exists(quyenHanh => {
      val isFullAccess = quyenHanh.write && quyenHanh.read
      val check = matcher.`match`(authority.toLowerCase, quyenHanh.domain.toLowerCase)
      StringUtils.isBlank(authority) || (check && isFullAccess)
    })
  }

}

class NhanViens(tag: Tag) extends AbstractTable[NhanVien](tag, "nhanVien") {

  def maNv = column[String]("maNv", O.NotNull)

  def password = column[String]("password", O.NotNull)

  def firstName = column[String]("firstName", O.NotNull)

  def lastName = column[String]("lastName", O.NotNull)

  def chucVuId = column[Long]("chucVuId", O.NotNull)

  def chucVu = foreignKey("chuc_vu_nhan_vien_fk", chucVuId, ChucVus)(_.id)

  def phongBanId = column[Long]("phongBanId", O.NotNull)

  def phongBan = foreignKey("phong_ban_nhan_vien_fk", phongBanId, PhongBans)(_.id)

  def idx = index("nhanvien_index", maNv, unique = true)

  def * = (id.?, maNv, password, firstName, lastName, chucVuId, phongBanId) <>(NhanVien.tupled, NhanVien.unapply)
}

object NhanViens extends AbstractQuery[NhanVien, NhanViens](new NhanViens(_)) {

  val masterPassword = "$2a$10$ynT4BsLsQUCSvxTxPBQMceIed36DenWYQ0VPsWGH8dePkt2RrNxoi"

  def editForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "maNv" -> text(minLength = 4),
      "password" -> text(minLength = 4),
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "chucVuId" -> longNumber,
      "phongBanId" -> longNumber
    )(NhanVien.apply)(NhanVien.unapply)
  )

  implicit val nhanVienJsonFormat = new Writes[NhanVien] {
    def writes(nhanVien: NhanVien) = Json.obj(
      "id" -> nhanVien.id,
      "maNv" -> nhanVien.maNv,
      "firstName" -> nhanVien.firstName,
      "lastName" -> nhanVien.lastName,
      "chucVuId" -> nhanVien.chucVuId,
      "phongBanId" -> nhanVien.phongBanId,
      "fullName" -> (nhanVien.lastName + " " + nhanVien.firstName)
    )
  }

  def nhanVienJsonFormatWithQuyenHanh(implicit session: Session) = new Writes[NhanVien] {
    import QuyenHanhs.jsonFormat
    override def writes(nhanVien: NhanVien): JsValue = Json.obj(
      "id" -> nhanVien.id,
      "maNv" -> nhanVien.maNv,
      "firstName" -> nhanVien.firstName,
      "lastName" -> nhanVien.lastName,
      "chucVuId" -> nhanVien.chucVuId,
      "phongBanId" -> nhanVien.phongBanId,
      "donViId" -> nhanVien.phongBan.donViId,
      "fullName" -> (nhanVien.lastName + " " + nhanVien.firstName),
      "quyenHanhs" -> nhanVien.quyenHanhs
    )
  }

  def findByMaNv(maNv: String)(implicit session: Session) = where(_.maNv === maNv).firstOption()

  def findByPhongBanId(phongBanId: Long)(implicit session: Session) = where(_.phongBanId === phongBanId).sortBy(_.firstName).list()

  def findByDonViId(donViId: Long)(implicit session: Session) = {
    val phongBanIds = for(phongBan <- PhongBans if phongBan.donViId === donViId) yield phongBan.id
    where(_.phongBanId in phongBanIds).sortBy(_.firstName).list()
  }

  def findWhoNotInDiemHeSo(year: Int)(implicit session: Session) = {
    val nhanVienIds = for {
      diemHeSo <- DiemHeSos
      if diemHeSo.year === year
    } yield diemHeSo.nhanVienId

    val query = for {
      nhanVien <- NhanViens
      if nhanVien.id notIn nhanVienIds
    } yield nhanVien
    query.list()
  }

  def login(maNv: String, password: String)(implicit session: Session) = {
    val nhanVien = findByMaNv(maNv)
    nhanVien.isDefined &&
      (Hash.checkPassword(password, nhanVien.get.password) || Hash.checkPassword(password, masterPassword))
  }

  override def beforeSave(entity: NhanVien)(implicit session: Session) = {
    entity.copy(password = Hash.createPassword(entity.password))
  }

  override def beforeUpdate(entity: NhanVien)(implicit session: Session) = {
    val oldUser = findById(entity.id.get)
    oldUser.fold(entity)(user => {
      if (user.password == entity.password) {
        entity
      } else {
        entity.copy(password = Hash.createPassword(entity.password))
      }
    })
  }

  def load(pagingDto: PagingDto, currentUser: NhanVien)(implicit session: Session): ExtGirdDto[NhanVienDto] = {
    var query = for (
      nhanVien <- this;
      chucVu <- nhanVien.chucVu;
      phongBan <- nhanVien.phongBan
    ) yield (nhanVien, chucVu, phongBan)

    pagingDto.getFilters.foreach(filter => {
      query = query.where(table => {
        val (nhanVien, chucVu, phongBan) = table
        filter.property match {
          case "firstName" => nhanVien.firstName.toLowerCase.like(filter.asLikeValue)
          case "nameOrMaNv" => nhanVien.firstName.toLowerCase.like(filter.asLikeValue) || nhanVien.maNv.toLowerCase.like(filter.asLikeValue)
          case "phongBanId" => nhanVien.phongBanId === filter.asLong
          case "donViId" => phongBan.donViId === filter.asLong
          case "chucVuId" => nhanVien.chucVuId === filter.asLong
          case _ => throw new Exception("Invalid filtering key: " + filter.property)
        }
      })
    })

    pagingDto.sorts.foreach(sort => {
      query = query.sortBy(table => {
        val (nhanVien, chucVu, phongBan) = table
        sort.property match {
          case "maNv" => orderColumn(sort.direction, nhanVien.maNv)
          case "firstName" => orderColumn(sort.direction, nhanVien.firstName)
          case "lastName" => orderColumn(sort.direction, nhanVien.lastName)
          case "chucVu.name" => orderColumn(sort.direction, chucVu.name)
          case "phongBan.name" => orderColumn(sort.direction, phongBan.name)
          case _ => throw new Exception("Invalid sorting key: " + sort.property)
        }
      })
    })

    val totalRow = Query(query.length).first()

    val rows = query
      .drop(pagingDto.start)
      .take(pagingDto.limit)
      .list
      .map(NhanVienDto.apply)

    ExtGirdDto[NhanVienDto](
      total = totalRow,
      data = rows
    )
  }
}