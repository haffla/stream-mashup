package models.database.facade

import models.database.alias.{Album,AppDB,Artist,Track}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CollectionFacade {
  def apply(identifier:Either[Int,String]) = new CollectionFacade(identifier)
}

class CollectionFacade(identifier:Either[Int,String]) extends Facade {

  def userCollection:Future[List[(Album, Artist, Track)]] = {
    Future {
      AppDB.getCollectionByUser(identifier)
    }
  }
}