package web

import conf.Config
import unfiltered.request.HttpRequest
import data.{MatchTemplate, User, Match}
import xml.NodeSeq

case class Pages(req:HttpRequest[_]) (implicit val config:Config){

  val snippets = Snippets(req)
  import snippets._

  def editMatchForm(m:Option[Match]) = {
    editMatch(m)
  }

  def adminMatchListView(m: Seq[Match]) = bootstrap("Kamper", adminMatchList(m), Some(filterTableJS), <meta property="og:description" content={"%s ledige dommeroppdrag ".format(m.foldLeft(0)(_ + _.availableCount) )}/>)

  def listMatches(m: Seq[Match], matchLinkPrefix:String, userId:Option[String]) = bootstrap("Kamper", matchList(m, matchLinkPrefix, userId), None, <meta property="og:description" content={"%s ledige dommeroppdrag ".format(m.foldLeft(0)(_ + _.availableCount) )}/>)

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

  def user(user:User, matches:Seq[Match], matchLinkPrefix:String) = bootstrap(user.name, userView(user) ++ matchList(matches, matchLinkPrefix, None), None)

  def userForm(user:Option[User]) = snippets.editUserForm(user)

  def login(queryParams:Map[String, Seq[String]]) = bootstrap(
    "Logg inn",
      <div>
       {snippets.loginMessages(queryParams)}
      </div> ++
    snippets.loginForm,
    Some(snippets.loginJs))

  def resetPassword = bootstrap("Sett nytt passord", resetPasswordform, Some(resetPasswordFormJs))

  def clubNewMatch = bootstrap("Meld inn behov for dommer", <legend>Bestill dommer</legend> ++ newMatchForm, Some(newMatchJS))

  def lostPassword = bootstrap("Glemt passord",
    <legend>Glemt passord</legend>
      <p>Fyll inn skjema under. Du vil da motta en e-post med instruksjoner for å sette nytt passord.</p>
    <form class="form-inline" method="POST">
      <input type="email" placeholder="Email" name="email"/>
      <span class="help-inline"></span>
      <button type="submit" class="btn">Send</button>
    </form>
  )

  def refereeUpdateLevel = {
    bootstrap("Nivå",
      <legend>Bekreft nivå</legend>
      <p>Hva er det høyeste nivået du har dømt kamper <strong>tildelt av kretsen</strong> som <strong style="text-decoration: underline;">hoveddommer i år</strong></p>
      <p>Du skal ikke fylle inn nivå du har dømt tidligere sesonger eller nivå du har dømt på i kamper som ikke er administrert av kretsen</p>
      ++ {confirmLevelForm}, Some(levelFormJS)
    )
  }

  def unpublishedMatches(matches:Seq[Match]) = unpublishedMatchesTable(matches)

  def refereeOrderReceipt(matchTemplate:MatchTemplate) = bootstrap("Takk for bestillingen",
    refereeOrderReceiptSnippet(matchTemplate),
    None)

}
