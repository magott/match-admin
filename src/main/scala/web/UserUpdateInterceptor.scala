package web

import unfiltered.filter.Plan
import org.joda.time.DateTime
import unfiltered.response.Pass
import unfiltered.request.{Seg, Path}

class UserUpdateInterceptor(cutoff:DateTime, enable:Boolean) extends Plan{
  def intent = {
    case _ if(!enable) => Pass

    case Path(Seg("img" :: _ :: Nil)) | Path(Seg("css" :: _ :: Nil)) | Path(Seg("img" :: "yr" :: _ :: Nil))
         | Path(Seg("js" :: _ :: Nil)) | Path(Seg("favicon.ico" :: Nil)) => Pass

    //User redirected to update, or is posting update
    case Path(Seg("users" :: userid :: "level" :: Nil)) => Pass

    case r@LoggedOnUser(user) => {
      if(user.lastUpdate.exists(_.isAfter(cutoff))) Pass
      else HerokuRedirect(r, s"/users/${user.id.get.toString}/level")
    }
  }
}
