package runner

import scalikejdbc.config.DBs

object ServerRunner extends Server {
  DBs.setupAll()
}
