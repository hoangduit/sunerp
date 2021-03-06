package dtos

import models.sunerp.{DonVis, PhongBan, DonVi}
import play.api.libs.json.{Json, JsValue, Writes}

/**
 * The Class PhongBanDto.
 *
 * @author Nguyen Duc Dung
 * @since 3/19/14 10:20 AM
 *
 */
case class PhongBanDto(
                         id: Long,
                         name: String,
                         shortName: Option[String],
                         donViId: Long,
                         donVi: DonVi,
                         showOnReport: Boolean
                         )


object PhongBanDto {

  def apply(tuple: (DonVi, PhongBan)) = {
    val (donVi, phongBan) = tuple
    new PhongBanDto(
      id = phongBan.id.get,
      name = phongBan.name,
      shortName = phongBan.shortName,
      donViId = phongBan.donViId,
      showOnReport = phongBan.showOnReport,
      donVi = donVi
    )
  }

  implicit val jsonWrite = new Writes[PhongBanDto] {
    override def writes(o: PhongBanDto): JsValue = Json.obj(
      "id" -> o.id,
      "name" -> o.name,
      "shortName" -> o.shortName,
      "donViId" -> o.donViId,
      "showOnReport" -> o.showOnReport,
      "donVi" -> DonVis.donViJsonFormat.writes(o.donVi)
    )
  }
}