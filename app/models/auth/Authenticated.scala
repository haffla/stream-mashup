package models.auth

import controllers.routes
import models.auth.form.AuthHandling
import models.service.Constants
import play.api.mvc.{Result, Controller, Request, ActionBuilder, WrappedRequest}
import play.cache.Cache

import scala.concurrent.Future

object Authenticated extends AuthenticatedAction {
  def userNameAllowed(username:String) = true
}

object AdminAccess extends AuthenticatedAction {
  val admins = Array("jacke")
  def userNameAllowed(username:String) = admins.contains(username)
}

trait AuthenticatedAction extends ActionBuilder[Request] with AuthHandling {

  def userNameAllowed(username:String):Boolean
  val login = routes.AuthController.login().toString
  val index = routes.Application.index().toString

  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    request.session.get("username").map { username =>
      if(userNameAllowed(username)) {
        val secretFromCache = Cache.get(s"user.$username")
        val secretFromSession = request.session.get("auth-secret").getOrElse("no-session-key")
        if(secretFromCache == secretFromSession)
          block(new AuthenticatedRequest(request))
        else
          redirectTo(login, request, Constants.sessionTamperingMessage)
      }
      else {
        redirectTo(index, request, "This location can only be accessed by admins.")
      }
    } getOrElse {
      redirectTo(login, request, "Please login first.")
    }
  }

  class AuthenticatedRequest[A](request: Request[A]) extends WrappedRequest[A](request)
}

