package web

import xml.NodeSeq
import unfiltered.request.HttpRequest
import data._
import data.KeyAndValue
import scala.Some
import data.RefereeType.{Dommer, Trio}

case class Snippets(req: HttpRequest[_]) {

  def editMatch(m: Option[Match]) = {
    val isTrio = m.isDefined && m.get.refereeType==Trio.key
    bootstrap("Kamp",
      <legend>
        {if (m.isEmpty) "Ny" else "Endre"}
        kamp</legend>
        <form class="form-horizontal" action={"/admin/matches/"+m.flatMap(_.id.map(_.toString)).getOrElse("")} method="POST" id="match-form">
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
              <input type="time" id="time" name="time" placeholder="TT:MM" class="input-small" required="required" value={m.map(_.kickoff.toString("HH:mm")).getOrElse("")}/>
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
                (l: KeyAndValue) => m.map(
                  `match` =>
                  if (`match`.level == l.key) <option selected="selected" value={l.key}> {l.display} </option>
                  else <option value={l.key}> {l.display}</option>
                ).getOrElse(<option value={l.key}> {l.display}</option>))}
              </select>
              <span class="help-inline"></span>
            </div>
          </div>
          <label class="control-label">Behov</label>
          <div class="controls">
            <label class="radio">
              {if (isTrio)
                <input type="radio" name="refType" id="refTypeDommer" value={Dommer.key} required="required"/>
            else
                <input type="radio" name="refType" id="refTypeDommer" value={Dommer.key} checked="checked" required="required"/>
              }
              <div>{Dommer.display}</div>
              </label>
              <label class="radio">
              {if (!isTrio)
                <input type="radio" name="refType" id="refTypeTrio" value={Trio.key}/>
            else
                <input type="radio" name="refType" id="refTypeTrio"  checked="checked" value={Trio.key}/>
              }
              <div>{Trio.display}</div>
              </label>
              <span class="help-inline"></span>
          </div>
          <div class="control-group">
            <label class="control-label" for="refFee">Honorar</label>
            <div class="controls controls-row">
              <input type="number" id="refFee" name="refFee" class="input-small" placeholder="Dommer" required="required" value={m.flatMap(_.refFee.map(_.toString)).getOrElse("")}/>
              <input type="number" id="assFee" name="assFee" class="input-small" placeholder="AD" required="required" value={m.flatMap(_.assistantFee.map(_.toString)).getOrElse("")}/>
              <span class="help-inline"></span>
            </div>
          </div>


          <div class="control-group">
            <label class="control-label" for="refFee">Tildeling</label>
            <div class="controls">
              <div class="ref-controls">
               {select("appointedRef", "Dommer", m.flatMap(_.appointedRef.map(_.id.toString)), m.map(_.interestedRefs.map(_.toSelectOption)).getOrElse(Nil))}
                <a class="user-profile btn btn-small" href=""><i class="icon-user"></i></a>
              </div>
              <div class="ass-controls">
              {select("appointedAssistant1", "AD1", m.flatMap(_.appointedAssistant1.map(_.id.toString)), m.map(_.interestedAssistants.map(_.toSelectOption)).getOrElse(Nil))}
                <a class="user-profile btn btn-small" href=""><i class="icon-user"></i></a>
              </div>
              <div class="ass-controls">
                {select("appointedAssistant2", "AD2", m.flatMap(_.appointedAssistant2.map(_.id.toString)), m.map(_.interestedAssistants.map(_.toSelectOption)).getOrElse(Nil))}
                <a class="user-profile btn btn-small" href=""><i class="icon-user"></i></a>
              </div>
            </div>
          </div>

          <div class="control-group">
            <div class="controls">
              <button type="submit" class="btn btn-primary">Lagre</button>
              <button type="button" data-toggle="modal" class="btn btn-danger" data-target="#confirmDelete">Slett</button>
              <button type="button" class="btn btn-inverse" id="send-mail">Send mail</button>
              <span class="mail-status mail-processing fade in hide"><i class="icon-time"/>Sender..</span>
              <span class="mail-status mail-success fade in hide"><i class="icon-ok icon-green"/>Mail sendt!</span>
              <span class="mail-status mail-failure fade in hide"><i class="icon-remove icon-red"/>Feil ved sending av mail</span>
            </div>
          </div>
          {modal}
        </form>
    , Some(editMatchJS))
  }

  def modal = {
    <div id="confirmDelete" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel">Bekreft sletting</h3>
      </div>
      <div class="modal-body">
        <p>Sikker på at du vil slette denne kampen?</p>
      </div>
      <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Avbryt</button>
        <button type="button" class="btn btn-danger" id="delete-match">Slett kamp</button>
      </div>
    </div>
  }

  def select(selectId:String, defaultDisplay:String, selected:Option[String], options:List[KeyAndValue]) = {
    <select id={selectId} name={selectId}>
      {if(selected.isEmpty)
        <option selected="selected" value="">{defaultDisplay}</option>
       else
        <option value="">{defaultDisplay}</option>
      }
      {selectOption(selectId, selected, options)}
    </select>
  }

  def selectOption(selectId:String, selected:Option[String], options:List[KeyAndValue]):NodeSeq = {
    options.map(
      o => selected.map(
        s=> if(s == o.key) <option value={o.key} selected="selected">{o.display}</option> else <option value={o.key}>{o.display}</option>)
    .getOrElse(<option value={o.key}>{o.display}</option>))
  }

  def viewMatch(m:Match, userId:Option[String]) = {
    bootstrap("Kamp",
       <table class="table table-condensed table-bordered table-nonfluid">
        <tr>
          <th>Kamp</th>
          <td>{m.homeTeam} - {m.awayTeam}</td>
        </tr>
        <tr>
          <th>Bane</th>
          <td>{m.venue}</td>
        </tr>
        <tr>
          <th>Avspark</th>
          <td>{m.kickoff.toString("dd.MM.yyyy HH:mm")}</td>
        </tr>
        <tr>
          <th>Honorar dommer</th>
          <td>{m.refFee.getOrElse("-")}</td>
        </tr>
        <tr>
          <th>Honorar Assistentdommer</th>
          <td>{m.assistantFee.getOrElse("-")}</td>
        </tr>
        <tr>
          <th>Dommer</th>
          <td>{m.interestedRefButton(userId)}</td>
        </tr>
         {if(m.refereeType==Trio.key)
         <tr>
           <th>Assistentdommer</th>
           <td>{m.interestedAssistant1Button(userId)}</td>
         </tr>
           <tr>
           <th>&nbsp;</th>
           <td>{m.interestedAssistant2Button(userId)}</td>
         </tr>
         }
      </table>
      , Some(viewMatchJS))
  }

  def editUserForm(user: Option[User]) = {
    bootstrap("Bruker",

      <form class="form-horizontal" id="user-form" method="POST">
        <legend>
          {if (user.isEmpty) "Ny" else "Endre"}
          bruker</legend>
        <div class="control-group">
          <label class="control-label" for="inputName">Navn</label>
          <div class="controls">
            <input type="text" id="name" placeholder="Fullt navn" name="name" value={user.map(_.name).getOrElse("")}/>
            <span class="help-inline"></span>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="refNumber"> Dommernummer</label>
          <div class="controls">
            <div class="input-prepend">
              <span class="add-on"><strong>03-</strong></span>
              <input type="text" class="input-mini" id="refNumber" placeholder="Dnr" name="refNumber" value={user.map(_.refereeNumber.toString).getOrElse("")} maxlength="6"/>
              <span class="help-inline"></span>
            </div>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="inputTelephone">Telefon</label>
          <div class="controls">
            <input type="tel" id="telephone" name="telephone" placeholder="Telefon" value={user.map(_.telephone).getOrElse("")}/>
            <span class="help-inline"></span>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="inputEmail">Email</label>
          <div class="controls">
            <input type="email" id="email" placeholder="Email" name="email" value={user.map(_.email).getOrElse("")}/>
            <span class="help-inline"></span>
          </div>
        </div>
        <div class="control-group">
          <label class="control-label" for="inputPassword">Passord</label>
          <div class="controls">
            <input type="password" id="password" name="password" placeholder="Passord"/>
            <span class="help-inline"></span>
          </div>
          <div class="controls">
            <input type="password" id="password2" name="password2" placeholder="Gjenta passord"/>
            <span class="help-inline"></span>
          </div>
        </div>

        <div class="control-group">
          <label class="control-label" for="level">Nivå dømt 2012</label>
          <div class="controls">
            <select id="level" name="level">
              {if (user.isEmpty || user.get.level.isEmpty)
                <option selected="selected" value="">Velg</option>}{Level.all.map(
                (l: KeyAndValue) => user.map(
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
          <span class="help-inline"></span>
        </div>

        <div class="control-group">
          <div class="controls">
            <button type="submit" class="btn">Lagre</button>
          </div>
        </div>
      </form>, Some(userFormJS)

    )
  }

  def loginMessages(mp:Map[String, Seq[String]]):NodeSeq = {
    if(mp.contains("failed")) <div class="alert alert-block">Feil brukernavn/passord, prøve igjen</div>
    else if(mp.contains("reset")) <div class="alert alert-success">Passordet er endret, logg inn med ditt nye passord </div>
    else Nil
  }

  def loginForm = {
    <legend>Logg inn</legend>
    <form class="form-horizontal" id="login-form" name="login-form" method="post">
      <div class="control-group">
        <label class="control-label" for="inputEmail">Email</label>
        <div class="controls">
          <input type="text" id="email" name="email" placeholder="Email"/>
          <span class="help-inline"></span>
        </div>
      </div>
      <div class="control-group">
        <label class="control-label" for="inputPassword">Password</label>
        <div class="controls">
          <input type="password" id="password" name="password" placeholder="Password"/>
          <span class="help-inline"></span>
        </div>
      </div>
      <div class="control-group">
        <div class="controls">
          <label class="checkbox">
          <input type="checkbox" name="remember"/>
            Husk meg
          </label>
          <button type="submit" class="btn">Sign in</button>
        </div>
      </div>
      <div class="control-group">
        <div class="controls">
          Har du glemt passordet ditt? <a href="/lostpassword">Trykk her</a>
        </div>
      </div>
    </form>
  }

  def userView(user:User) = {
      <table class="table table-condensed table-bordered table-nonfluid">
        <tr>
          <th>Dommernummer</th>
          <td>03-{user.refereeNumber}</td>
        </tr>
        <tr>
          <th>Navn</th>
          <td>{user.name}</td>
      </tr>
      <tr>
        <th>E-mail</th>
        <td>{user.email}</td>
      </tr>
      <tr>
        <th>Telefon</th>
        <td>{user.telephone}</td>
      </tr>
      <tr>
        <th>Nivå</th>
        <td>{Level.asMap(user.level)}</td>
      </tr>
        {if(user.admin)
        <tr>
        <th></th>
          <td>Admin</td>
        </tr>
        }
    </table>
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

  def matchList(matches:Iterator[Match], matchLinkPath:String) = {
    <table class="table table-striped table-bordered table-condensed">
      <thead>
        <tr>
          <th>Tid</th>
          <th>Kamp</th>
          <th>Nivå</th>
          <th>Sted</th>
          <th>Dommer</th>
          <th>AD 1</th>
          <th>AD 2</th>
        </tr>
      </thead>
      <tbody>
        {
          matches.map( m =>
            <tr>
            <td>{m.kickoff.toString("dd.MM.yyyy HH:mm")}</td>
              <td><a href={matchLinkPath + m.id.get.toString} > {m.homeTeam} - {m.awayTeam} </a></td>
              <td>{Level.asMap(m.level)}</td>
              <td>{m.venue}</td>
              <td>{m.appointedRef.map(_.name).getOrElse(m.refFee.map(_.toString).getOrElse(""))}</td>
              <td>{m.appointedAssistant1.map(_.name).getOrElse(m.assistantFee.map(_.toString).getOrElse(""))}</td>
              <td>{m.appointedAssistant2.map(_.name).getOrElse(m.assistantFee.map(_.toString).getOrElse(""))}</td>

            </tr>
          )

        }
      </tbody>
    </table>
  }

  def resetPasswordform =
    <form class="form-horizontal" id="reset-password-form" method="POST">
      <legend>Sett nytt passord</legend>
      <div class="control-group">
        <label class="control-label" for="password">Passord</label>
        <div class="controls">
          <input type="password" id="password" name="password" placeholder="Passord"/>
          <span class="help-inline"></span>
        </div>
        <div class="controls">
          <input type="password" id="password2" name="password2" placeholder="Gjenta passord"/>
          <span class="help-inline"></span>
        </div>
      </div>
      <div class="control-group">
        <div class="controls">
          <button type="submit" class="btn btn-primary">Sett nytt passord</button>
        </div>
      </div>
    </form>

  val editMatchJS = {
    <script src="/js/jquery.validate.min.js" />
    <script src="/js/additional-methods.min.js" />
    <script type="text/javascript">
      { """
            $(document).ready(editMatchFunctions);

        """}
    </script>
  }

  val loginJs = {
      <script src="/js/jquery.validate.min.js" />
      <script type="text/javascript">
        { """
            $(document).ready(validateLogin());

          """}
      </script>
  }
  val userFormJS = {
      <script src="/js/jquery.validate.min.js" />
      <script type="text/javascript">
        { """
            $(document).ready(validateUserForm());

          """}
      </script>
  }

  val viewMatchJS = {
      <script type="text/javascript">
        { """
            $(document).ready(interestButtonFunctions());

          """}
      </script>
  }

  val resetPasswordFormJs = {
    <script src="/js/jquery.validate.min.js" />
    <script type="text/javascript">
      { """
            $(document).ready(validatePasswordResetForm());

        """}
    </script>
  }

}
