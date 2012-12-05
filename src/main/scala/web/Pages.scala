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

  def listMatches(m: Seq[Match], matchLinkPrefix:String) = bootstrap("Kamper", matchList(m, matchLinkPrefix), None, <meta property="og:description" content={"%s ledige dommeroppdrag ".format(m.foldLeft(0)(_ + _.availableCount) )}/>)

  def userList(users:List[User]) = bootstrap("Brukere", listUserTable(users))

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

  def user(user:User) = bootstrap(user.name, userView(user), None)

  def userForm(user:Option[User]) = snippets.editUserForm(user)

  def login(queryParams:Map[String, Seq[String]]) = bootstrap(
    "Logg inn",
      <div>
       {snippets.loginMessages(queryParams)}
      </div> ++
    snippets.loginForm,
    Some(snippets.loginJs))

  def resetPassword = bootstrap("Sett nytt passord", resetPasswordform, Some(resetPasswordFormJs))


  def lostPassword = bootstrap("Glemt passord",
    <legend>Glemt passord</legend>
      <p>Fyll inn skjema under. Du vil da motta en e-post med instruksjoner for å sette nytt passord.</p>
    <form class="form-inline" method="POST">
      <input type="email" placeholder="Email" name="email"/>
      <span class="help-inline"></span>
      <button type="submit" class="btn">Send</button>
    </form>
  )
}
