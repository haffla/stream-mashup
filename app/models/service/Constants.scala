package models.service

object Constants {
  val userSessionKey = "user_session_key"
  val fileHashCacheKeyPrefix = "it-file-hash|"

  val accessTokenRetrievalError = "The access token could not be retrieved"
  val userTracksRetrievalError  = "Error requesting user tracks"

  val stateMismatchError = "Error: State Mismatch. You might be a victim of a CSRF attack."
  val missingOAuthCodeError = "The service did not send an OAuth code."

  val mapKeyArtist = "artist"
  val mapKeyAlbum = "album"
  val mapKeyRdioArtistId = "rdio_artist_id"

  val jsonKeyAccessToken = "access_token"
}
