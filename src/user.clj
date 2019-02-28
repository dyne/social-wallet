;; social-wallet - A generic social wallet UI which uses the social-wallet-api for a beckend

;; Copyright (C) 2019- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; TODO:

;; This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify this Program, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns user
  (:require [taoensso.timbre :as log]
            
            [social-wallet.ring :as ring]
            [social-wallet.handler :as handler]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn run-dev-server [port]
  (log/info "Starting Jetty server...")
  (run-jetty handler/app {:port port})
  (log/info "Started server!"))

(defn stop-dev-server [server]
  (log/info "Stopping Jetty server...")
  (.stop server)
  (log/info "Stopped server!"))

(defn start-dev [port]
  (ring/init)
  (run-dev-server port))

(defn stop-dev []
  ;; TODO: Repl hanging when starting jetty-server cause it is synchronous. How to stop?
  (ring/destroy))
