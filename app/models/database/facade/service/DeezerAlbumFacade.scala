package models.database.facade.service

import models.database.alias.AppDB
import models.database.alias.service.DeezerAlbum
import org.squeryl.PrimitiveTypeMode._

object DeezerAlbumFacade extends ServiceAlbumFacade {

  override def insertServiceAlbumIfNotExists(id:Long, serviceId:String):Long = {
    from(AppDB.deezerAlbums)(da =>
      where(da.id === id)
        select da.id
    ).headOption match {
      case None => insertAlbum(id, serviceId)
      case _ => id
    }
  }

  override def insertAlbum(id: Long, serviceId: String): Long = {
    AppDB.deezerAlbums.insert(DeezerAlbum(id, serviceId)).id
  }
}

class DeezerAlbumFacade(identifier:Either[Int,String]) {

  def countMissingUserAlbums(artistIds:List[Long]):Long = {
    inTransaction {
      join(AppDB.albums, AppDB.tracks, AppDB.collections, AppDB.deezerAlbums.leftOuter)((alb,tr,col,spAlb) =>
        where(alb.id.in(artistIds) and AppDB.userWhereClause(col, identifier) and spAlb.map(_.id).isNull)
          compute countDistinct(alb.id)
          on(
          tr.albumId === alb.id,
          col.trackId === tr.id,
          spAlb.map(_.id) === alb.id
          )
      ).toLong
    }
  }
}