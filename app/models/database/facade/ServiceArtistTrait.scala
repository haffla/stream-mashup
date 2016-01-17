package models.database.facade

import models.database.alias.{Artist, AppDB}
import org.squeryl.PrimitiveTypeMode._

trait ServiceArtistTrait {

  def insertIfNotExists(id:Long):Long
  def insert(id:Long):Long

  def saveArtistWithName(artistName:String):Long = {
    transaction {
      from(AppDB.artists)(a =>
        where(a.name === artistName)
          select a.id
      ).headOption match {
        case Some(artistId) => insertIfNotExists(artistId)
        case _ =>
          val newArtist:Artist = AppDB.artists.insert(Artist(artistName))
          insert(newArtist.id)
      }
    }
  }
}
