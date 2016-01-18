package models.database.alias.service

import org.squeryl.annotations._

case class SpotifyArtist(@Column("id_spotify_artist") id:Long) extends ServiceArtist {
  override def getId: Long = this.id
}
