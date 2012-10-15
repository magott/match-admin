package web

import xml.NodeSeq
import unfiltered.request.HttpRequest
import javax.servlet.http.HttpServletRequest
import data.{Match, SelectOption, Levels, User}

case class Snippets(req: HttpRequest[_]) {

  def editMatch(m: Option[Match]) = {
    bootstrap("Kamp",
      <legend>
        {if (m.isEmpty) "Ny" else "Endre"}
        kamp</legend>
        <form class="form-horizontal" action="POST" method="POST">
          <div class="control-group">
            <label class="control-label">Kamp</label>
            <div class="controls">
              <input type="text" id="home" class="input-medium" placeholder="Hjemmelag"/> &nbsp;
              -
              &nbsp;
              <input type="text" id="away" class="input-medium" placeholder="Bortelag"/>
            </div>
          </div>
          <div class="control-group">
            <label class="control-label" for="inputName">Bane</label>
            <div class="controls">
              <input type="text" id="venue" class="input-medium" placeholder="Bane" value={m.map(_.venue).getOrElse("")}/>
            </div>
          </div>
          <div class="control-group">
            <label class="control-label">Kampstart</label>
            <div class="controls">
              <input type="date" id="date" name="date" placeholder="ÅÅÅ-MM-DD" class="input-medium"/> &nbsp;
              <input type="time" id="time" name="time" placeholder="TT:MM" class="input-small"/>
            </div>
          </div>
          <div class="control-group">
            <label class="control-label">Behov</label>
            <div class="controls">
            <label class="radio">
              <input type="radio" name="refType" id="refTypeDommer" value="dommer" checked="checked" />
                Dommer
              </label>
            <label class="radio">
              <input type="radio" name="refType" id="refTypeTrio" value="trio" />
                Trio
              </label>
            </div>
          </div>
          <div class="control-group">
            <label class="control-label" for="level">Nivå</label>
            <div class="controls">
              <select id="level" name="level">
                {if (m.isEmpty || m.get.level.isEmpty)
                <option selected="selected">Velg</option>}{Levels.all.map(
                (l: SelectOption) => m.map(
                  `match` => if (`match`.level == l.key) <option selected="selected" value={l.key}>
                    {l.display}
                  </option>
                  else <option value={l.key}>
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
              <button type="submit" class="btn">Lagre</button>
            </div>
          </div>
        </form>
    )
  }

  def displayMatch(m: Match) = {

  }


  def editUserForm(user: Option[User]) = {
    bootstrap("Bruker",

      <form class="form-horizontal">
        <legend>
          {if (user.isEmpty) "Ny" else "Endre"}
          bruker</legend>
        <div class="control-group">
          <label class="control-label" for="inputName">Name</label>
          <div class="controls">
            <input type="email" id="inputEmail" placeholder="Navn" name="name" value={user.map(_.name).getOrElse("")}/>
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
          <label class="control-label" for="inputPassword">Password</label>
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
            <select id="level">
              {if (user.isEmpty || user.get.level.isEmpty)
              <option selected="selected">Velg</option>}{Levels.all.map(
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

  def bootstrap(title: String, body: NodeSeq) = {

    <html lang="en">
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
                <p class="navbar-text pull-right">
                  Logged in as
                  <a href="#" class="navbar-link">Username</a>
                </p>
                <ul class="nav">
                  <li class="active">
                    <a href="#">Kamper</a>
                  </li>
                  <li>
                    <a href="#about">Bruker</a>
                  </li>
                  <li>
                    <a href="#contact">Admin</a>
                  </li>
                </ul>
              </div> <!--/.nav-collapse -->
            </div>
          </div>
        </div>

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

      </body>
    </html>


  }

}
