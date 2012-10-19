package web

import unfiltered.request.HttpRequest
import data.{User, Match}
import xml.NodeSeq

case class Pages(req:HttpRequest[_]) {

  val snippets = Snippets(req)
  import snippets._

  def editMatchForm(m:Option[Match]) = {
    editMatch(m)
  }

  def alreadyLoggedIn = {
    bootstrap("Logg inn", <div class="well">Du er alt logget inn. Vil du logge ut, så <a href="/logout">trykk her</a></div>)
  }

  def notFound(text:Option[String] = None) = {
    bootstrap("Siden finnes ikke", <div class="alert alert-error"> <h4 class="alert-heading">Siden finnes ikke!</h4> {text.getOrElse("")} </div> , None)
  }

  def errorPage(errorMessage:NodeSeq) = {
    bootstrap("Feil", <div class="alert alert-error"> <h4 class="alert-heading">Feil!</h4> {errorMessage}</div> , None)
  }
  def forbidden = {
    bootstrap("Ingen tilgang", <div class="alert alert-error"> <h4 class="alert-heading">Ingen tilgang!</h4> Du har ikke tilgang til denne siden eller mangler rettigheter til å gjøre dette</div> , None)
  }

  def userForm(user:Option[User]) = snippets.editUserForm(user)

  def login = snippets.loginForm

}
