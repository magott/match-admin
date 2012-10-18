package web

import xml.NodeSeq
import unfiltered.request.HttpRequest
import data._
import data.SelectOption
import scala.Some

case class Snippets(req: HttpRequest[_]) {

  def editMatch(m: Option[Match]) = {
    val isTrio = m.isDefined && m.get.refereeType=="trio"
    bootstrap("Kamp",
      <legend>
        {if (m.isEmpty) "Ny" else "Endre"}
        kamp</legend>
        <form class="form-horizontal" action="/admin/matches/match" method="POST" id="match-form">
          <div class="control-group">
            <label class="control-label">Kamp</label>
            <div class="controls">
              <input type="text" id="home" name="home" class="input-medium" placeholder="Hjemmelag" required="required" value={m.map(_.homeTeam).getOrElse("")}/>
              &nbsp;
              -
              &nbsp;
              <input type="text" id="away" name="away" class="input-medium" placeholder="Bortelag" required="required" value={m.map(_.awayTeam).getOrElse("")}/>
              <span class="help-inline"></span>
            </div>
          </div>
          <div class="control-group">
            <label class="control-label" for="venue">Bane</label>
            <div class="controls">
              <input type="text" name="venue" id="venue" class="input-large" required="required" placeholder="Bane" value={m.map(_.venue).getOrElse("")}/>
              <span class="help-inline"></span>
            </div>
          </div>
          <div class="control-group">
          <label class="control-label">Kampstart</label>
            <div class="controls controls-row">
              <input type="date" id="date" name="date" placeholder="ÅÅÅÅ-MM-DD" class="input-medium" required="required" value={m.map(_.kickoff.toString("yyyy-MM-dd")).getOrElse("")}/>
              <input type="time" id="time" name="time" placeholder="TT:MM" class="input-small" required="required"/>
              <span class="help-inline"></span>
              </div>
            </div>
          <div class="control-group">
            <label class="control-label" for="level">Nivå</label>
            <div class="controls">
              <select id="level" name="level" placeholder="Velg">
                {if (m.isEmpty || m.get.level.isEmpty)
                <option disabled="disabled" selected="selected" value="">Velg</option>
                }
                {Level.all.map(
                (l: SelectOption) => m.map(
                  `match` =>
                  if (`match`.level == l.key) <option selected="selected" value={l.key}> {l.display} </option>
                  else <option value={l.key}> {l.display}</option>
                ).getOrElse(<option value={l.key}> {l.display}</option>))}
              </select>
              <span class="help-inline"></span>
            </div>
          </div>
          <div class="control-group">
            <label class="control-label">Behov</label>
            <div class="controls">
              <label class="radio">
                {if (isTrio)
                  <input type="radio" name="refType" id="refTypeDommer" value="dommer" required="required"/>
                else
                  <input type="radio" name="refType" id="refTypeDommer" value="dommer" checked="checked" required="required"/>
                }
                Dommer
              </label>
              <span class="help-inline"></span>
            <label class="radio">
              {if (!isTrio)
              <input type="radio" name="refType" id="refTypeTrio" value="trio"/>
              else
              <input type="radio" name="refType" id="refTypeTrio" value="trio" checked="checked"/>
              }
                Trio
              </label>
            </div>
            <span class="help-inline"></span>
          </div>
          <div class="control-group">
            <label class="control-label" for="refFee">Honorar</label>
            <div class="controls controls-row">
              <input type="number" id="refFee" name="refFee" class="input-small" placeholder="Dommer" required="required"/>
              <input type="number" id="assFee" name="assFee" class="input-small" placeholder="AD" required="required"/>
              <span class="help-inline"></span>
            </div>
          </div>


          <div class="control-group">
            <label class="control-label" for="refFee">Tildeling</label>
            <div class="controls">
              {select("appointedRef", "Dommer", m.flatMap(_.appointedRef.map(_.id.toString)), m.map(_.interestedRefs.map(_.toSelectOption)).getOrElse(Nil))}
              {select("appointedAssistant1", "AD1", m.flatMap(_.appointedAssistant1.map(_.id.toString)), m.map(_.interestedAssistants.map(_.toSelectOption)).getOrElse(Nil))}
              {select("appointedAssistant2", "AD2", m.flatMap(_.appointedAssistant2.map(_.id.toString)), m.map(_.interestedAssistants.map(_.toSelectOption)).getOrElse(Nil))}
            </div>
          </div>

          <div class="control-group">
            <div class="controls">
              <button type="submit" class="btn btn-primary">Lagre</button>
              <button type="submit" class="btn btn-danger">Slett</button>
              <button type="submit" class="btn btn-inverse" disabled="disabled">Send mail</button>
            </div>
          </div>
        </form>
    , Some(editMatchJS))
  }

  def select(selectId:String, defaultDisplay:String, selected:Option[String], options:List[SelectOption]) = {
    <select id={selectId} name={selectId}>
      {if(selected.isEmpty)
        <option disabled="disabled" selected="selected" value="">{defaultDisplay}</option>
       else
        <option disabled="disabled" value="">{defaultDisplay}</option>
      }
      {selectOption(selectId, selected, options)}
    </select>
  }

  def selectOption(selectId:String, selected:Option[String], options:List[SelectOption]):NodeSeq = {
    options.map(
      o => selected.map(
        s=> if(s == o.key) <option value={o.key} selected="selected">{o.display}</option> else <option value={o.key}>{o.display}</option>)
    .getOrElse(<option value={o.key}>{o.display}</option>))
  }

  def editUserForm(user: Option[User]) = {
    bootstrap("Bruker",

      <form class="form-horizontal" id="user-form" method="POST">
        <legend>
          {if (user.isEmpty) "Ny" else "Endre"}
          bruker</legend>
        <div class="control-group">
          <label class="control-label" for="inputName">Name</label>
          <div class="controls">
            <input type="text" id="inputName" placeholder="Navn" name="name" value={user.map(_.name).getOrElse("")}/>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="inputTelephone">Telefon</label>
          <div class="controls">
            <input type="tel" id="inputTelephone" name="telephone" placeholder="Telefon" value={user.map(_.telephone).getOrElse("")}/>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="inputEmail">Email</label>
          <div class="controls">
            <input type="email" id="inputName" placeholder="Email" name="email" value={user.map(_.email).getOrElse("")}/>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="inputPassword">Passord</label>
          <div class="controls">
            <input type="password" id="inputPassword" name="password" placeholder="Passord"/>
          </div>
          <div class="controls">
            <input type="password" id="inputPassword2" name="password2" placeholder="Gjenta passord"/>
          </div>
        </div>

        <div class="control-group">
          <label class="control-label" for="level">Nivå dømt 2012</label>
          <div class="controls">
            <select id="level" name="level">
              {if (user.isEmpty || user.get.level.isEmpty)
              <option selected="selected">Velg</option>}{Level.all.map(
              (l: SelectOption) => user.map(
                u => if (u.level == l.key) <option selected="selected" value={l.key}>
                  {l.display}
                </option>
                else <option>
                  {l.display}
                </option>
              ).getOrElse(<option value={l.key}>
                {l.display}
              </option>))}
            </select>
          </div>
        </div>

        <div class="control-group">
          <div class="controls">
            <button type="submit" class="btn">Registrer</button>
          </div>
        </div>
      </form>

    )
  }

  def loginForm = {
    <form class="form-horizontal">
      <div class="control-group">
        <label class="control-label" for="inputEmail">Email</label>
        <div class="controls">
          <input type="text" id="inputEmail" placeholder="Email"/>
        </div>
      </div>
      <div class="control-group">
        <label class="control-label" for="inputPassword">Password</label>
        <div class="controls">
          <input type="password" id="inputPassword" placeholder="Password"/>
        </div>
      </div>
      <div class="control-group">
        <div class="controls">
          <label class="checkbox">
            <input type="checkbox"/>
            Remember me
          </label>
          <button type="submit" class="btn">Sign in</button>
        </div>
      </div>
    </form>
  }

  def bootstrap(title: String, body: NodeSeq, bottom:Option[NodeSeq] = None) = {

    <html lang="no">
      <head>
        <meta charset="utf-8"/>
        <title>
          {title}
        </title>

        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <meta name="description" content=" "/>
        <meta name="author" content=" "/>

        <!-- Le styles -->
        <link href="/css/bootstrap.css" rel="stylesheet"/>
        <link href="/css/matchadmin.css" rel="stylesheet"/>
        <style type="text/css">
          {"""
          body{
            padding-top: 60px;
            padding-bottom: 40px;
          }
          .sidebar-nav{
          padding: 9 px 0;
          }
           """}
        </style>
        <link href="/css/bootstrap-responsive.css" rel="stylesheet"/>

        <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
        <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

        <!-- Le fav and touch icons -->
        <link rel="shortcut icon" href="../assets/ico/favicon.ico"/>
      </head>

      <body>

        {header}

        <div class="container-fluid">

          {body}<footer>
          <p>
            &copy;
            Morten Andersen-Gott 2012</p>
        </footer>

        </div> <!--/.fluid-container-->

        <!-- Le javascript -->
        <!-- Placed at the end of the document so the pages load faster -->
        <script src="/js/jquery-1.8.2.min.js"></script>
        <script src="/js/bootstrap.min.js"></script>
        <script src="/js/matchadmin.js"></script>
        {if(bottom.isDefined)
          bottom.get
        }

      </body>
    </html>
  }

  def header = {
   val sessOpt = UserSession.unapply(req)
    <div class="navbar navbar-inverse navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container-fluid">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          <a class="brand" href="#">OFDL Treningskamper</a>
          <div class="nav-collapse collapse">
            {
            if(sessOpt.isDefined){
            val session = sessOpt.get
            <p class="navbar-text pull-right">
              {session.name}
            </p>
            }
            }
            <ul class="nav">
              <li class="active">
                <a href="/matches">Kamper</a>
              </li>
              {if(sessOpt.isDefined){
              <li class="dropdown">
                <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                  Bruker
                  <b class="caret"></b>
                </a>
                <ul class="dropdown-menu">
                  <a href="/logout">Logg ut</a>
                  <a href={"/users/"+sessOpt.get.userId.toString}>Endre bruker</a>
                </ul>
              </li>
            }else{
              <li class="active">
                <a href="/login">Logg inn</a>
              </li>
            }
            }
              {if(sessOpt.isDefined && sessOpt.get.admin){
              <li class="dropdown">
                <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                  Admin
                  <b class="caret"></b>
                </a>
                <ul class="dropdown-menu">
                  <a href="/admin/matches">Kamper</a>
                  <a href="/admin/matches/new">Legg til kamp</a>
                  <a href="/admin/users">Brukere</a>
                </ul>
              </li>
            }
              }
            </ul>
          </div> <!--/.nav-collapse -->
        </div>
      </div>
    </div>

  }

  val editMatchJS = {
    <script src="/js/jquery.validate.min.js" />
    <script type="text/javascript">
      { """
            $(document).ready(editMatchFunctions);

        """}
    </script>
  }

}
