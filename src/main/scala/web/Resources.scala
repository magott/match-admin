package web

import unfiltered.filter.Plan
import unfiltered.request.{Seg, Path}
import unfiltered.response.{Ok, Html5}

class Resources extends Plan{

  val loginHandler = new LoginHandler()
  def intent = {
    case r@Path(Seg(List("admin", _*))) => new AdminHandler().handleAdmin(r)
    case r@Path(Seg(List("users", _*))) => new UserHandler().handleUser(r)
    case r@Path(Seg(List("matches", _*))) => new MatchHandler().handleMatches(r)
    case r@Path("/login") => loginHandler.handleLogin(r)
    case r@Path("/logout") => loginHandler.handleLogout(r)
    case r@Path("/resetpassword") => loginHandler.handlePasswordReset(r)
    case r@Path("/lostpassword") => loginHandler.handleLostPassword(r)
    case r@Path("/") => HerokuRedirect(r, "/matches")

    case Path("/foo") => {
      Ok ~> Html5(
          <html lang="no">
            <head>
              <meta charset="utf-8"></meta>
              <title>
                Kamp
              </title>

              <meta name="viewport" content="width=device-width, initial-scale=1.0"></meta>
              <meta name="description" content=" "></meta>
              <meta name="author" content=" "></meta>

              <!-- Le styles -->
              <link rel="stylesheet" href="css/bootstrap.css"></link>
              <link rel="stylesheet" href="css/matchadmin.css"></link>

              <link rel="stylesheet" href="css/bootstrap-responsive.css"></link>

              <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
              <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

              <!-- Le fav and touch icons -->
              <link rel="shortcut icon" href="../assets/ico/favicon.ico"></link>
            </head>

            <body>

              <div class="navbar navbar-inverse navbar-fixed-top">
                <div class="navbar-inner">
                  <div class="container-fluid">
                    <a class="btn btn-navbar" data-target=".nav-collapse" data-toggle="collapse">
                      <span class="icon-bar"></span>
                      <span class="icon-bar"></span>
                      <span class="icon-bar"></span>
                    </a>
                    <a class="brand" href="#">OFDL Treningskamper</a>
                    <div class="nav-collapse collapse">
                      <p class="navbar-text pull-right">
                        Morten Andersen-Gott
                      </p>
                      <ul class="nav">
                        <li class="active">
                          <a href="/matches">Kamper</a>
                        </li>
                        <li class="dropdown">
                          <a class="dropdown-toggle" href="#" data-toggle="dropdown">
                            Bruker
                            <b class="caret"></b>
                          </a>
                          <ul class="dropdown-menu">
                            <a href="/logout">Logg ut</a>
                            <a href="/users/5081237e040279cbe43df56d">Endre bruker</a>
                          </ul>
                        </li>
                        <li class="dropdown">
                          <a class="dropdown-toggle" href="#" data-toggle="dropdown">
                            Admin
                            <b class="caret"></b>
                          </a>
                          <ul class="dropdown-menu">
                            <a href="/admin/matches">Kamper</a>
                            <a href="/admin/matches/new">Legg til kamp</a>
                            <a href="/admin/users">Brukere</a>
                          </ul>
                        </li>
                      </ul>
                    </div> <!--/.nav-collapse -->
                  </div>
                </div>
              </div>

              <div class="container-fluid">

                <table class="table table-condensed table-bordered">
                  <tr>
                    <th>Kamp</th>
                    <td>Holmen - Øvrevoll Hosle</td>
                  </tr>
                  <tr>
                    <th>Bane</th>
                    <td>Holmen kunstgress</td>
                  </tr>
                  <tr>
                    <th>Avspark</th>
                    <td>01.11.2012 00:12</td>
                  </tr>
                  <tr>
                    <th>Dommer</th>
                    <td>
                      <button class="btn not-interested" id="ref">Meld interesse</button>
                    </td>
                  </tr>
                  <tr>
                    <th></th>
                    <td>
                      <button class="btn" data-state="interested" id="refBtn">
                        <span class="int interested-txt">Interesse meldt</span>
                        <span class="int not-interested-txt">Meld interesse</span>
                        <span class="int hover-interested-txt">Meld av</span>
                        <span class="int assigned-txt">Kampen er tildelt</span>
                        <span class="int login-txt">Logg inn for å melde interesse</span>
                      </button>
                    </td>
                  </tr>
                </table><footer>
                <p>
                  &copy;
                  Morten Andersen-Gott 2012</p>
              </footer>

              </div> <!--/.fluid-container-->

              <!-- Le javascript -->
              <!-- Placed at the end of the document so the pages load faster -->
              <script src="js/jquery-1.8.2.min.js"></script>
              <script src="js/bootstrap.min.js"></script>
              <script src="js/matchadmin.js"></script>
              <script type="text/javascript">

                $(document).ready(interestButtonFunctions());


              </script>

            </body>
          </html>
      )
    }

  }
}
