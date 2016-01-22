
import models.User
import models.database.facade.CollectionFacade
import models.service.library._
import org.scalatest.time.{Millis, Seconds, Span}
import org.squeryl.PrimitiveTypeMode._
import play.api.libs.json.JsValue
import play.api.test.{WithServer, WithApplication}

class UserCollectionTest extends UnitSpec {

  implicit val defaultPatience = PatienceConfig(timeout = Span(4, Seconds), interval = Span(500, Millis))

  val identifier = Right("testusersession")

  "The Library" should "correctly save a collection to DB and retrieve it" in new WithServer {
    val collection = Map(
      "The Beatles" -> Map("Sgt. Pepper’s Lonely Hearts Club Band" -> Set("With a Little Help from My Friends", "Getting Better")),
      "Extrawelt" -> Map("Dark Side Of The Moon" -> Set("Etre", "Colomb")),
      "Apparat" -> Map("Walls" -> Set("Useless Information", "Limelight"))
    )

    using(squerylSession) {

      val library = new Library(identifier)
      library.persist(collection)

      Thread.sleep(1000) // Need to wait a little for the data to be saved in DB

      val fromDb = CollectionFacade(identifier).userCollection
      whenReady(fromDb) { col =>
        val forFrontEnd = library.prepareCollectionForFrontend(col)

        val converted: List[JsValue] = forFrontEnd.as[List[JsValue]]
        val artists = converted map (x => (x \ "name").as[String])
        val albumObjects = converted map (x => (x \ "albums").as[List[JsValue]])

        val albums = albumObjects flatMap { albumObj =>
          albumObj map { album =>
            (album \ "name").as[String]
          }
        }

        val tracks: List[String] = albumObjects flatMap { albumObj =>
          albumObj flatMap { album =>
            (album \ "tracks").as[Set[JsValue]].map { tr =>
              (tr \ "name").as[String]
            }
          }
        }

        tracks.toSet should equal(Set("With a Little Help from My Friends", "Getting Better", "Etre", "Colomb", "Useless Information", "Limelight"))
        artists.toSet should equal(Set("The Beatles", "Apparat", "Extrawelt"))
        albums.toSet should equal(Set("Sgt. Pepper’s Lonely Hearts Club Band", "Dark Side Of The Moon", "Walls"))
      }

      User(identifier).deleteUsersCollection()
    }
  }
}
