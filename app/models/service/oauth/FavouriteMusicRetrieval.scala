package models.service.oauth

import models.service.Constants
import models.util.Logging
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait FavouriteMusicRetrieval {

  /**
   * @param accessToken The access token
   * @param page The page number or offset for the next request
   * @return The HTTP request
   */
  def favouriteMusicRetrievalRequest(accessToken:String, page:String):Future[WSResponse]

  /**
   * This method must decide whether another request must be started in case the service has a maximum
   * of entities it returns. In case of true it must provide the next value (e.g. offset or page) for the
   * next request
   * @param json The JSON returned from the previous request
   * @return Tuple2[Boolean, Int)
   */
  def getPageInformation(json:JsValue):(Boolean,Int)

  def requestUsersTracks(token:Option[String]):Future[Seq[JsValue]] = {
    token match {
      case Some(accessToken) =>
        doPaginatedRequest(accessToken, Seq.empty, 1)
      case None => throw new Exception (Constants.accessTokenRetrievalError)
    }
  }

  /**
   * @param accessToken The access token to access the api endpoint
   * @param aggregatedResponses A sequence where all responses are to be saved
   * @param page The value for the next request (e.g. page or limit)
   * @return
   */
  private def doPaginatedRequest(accessToken:String, aggregatedResponses:Seq[JsValue], page:Int):Future[Seq[JsValue]] = {
    favouriteMusicRetrievalRequest(accessToken, page.toString) flatMap { response =>
      response.status match {
        case 200 =>
          val json = Json.parse(response.body)
          val (tobeContinued, next) = getPageInformation(json)
          if(tobeContinued) doPaginatedRequest(accessToken, aggregatedResponses :+ json, next)
          else Future.successful(aggregatedResponses :+ json)
        case http_code =>
          Logging.error(this.getClass.toString, Constants.userTracksRetrievalError + ": " +  http_code + "\n" + response.body)
          Future.successful(aggregatedResponses)
      }
    }
  }
}
