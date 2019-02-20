;; social-wallet - A generic social wallet UI which uses the social-wallet-api for a beckend

;; Copyright (C) 2019- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; TODO:

;; This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify this Program, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(defproject social-wallet "0.1.0-SNAPSHOT"
  :description "Basic compojure based authenticated website"
  :url "http://dyne.org"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 ;; ring routing
                 [compojure "1.6.1"]
                 
                 ;; HTTP server abstraction
                 [ring/ring-core "1.7.1"]
                 ;; ring middleware and defaults
                 [ring/ring-defaults "0.3.2"]
                 [ring-middleware-accept "2.0.3"]
                 [ring-cors "0.1.12"]

                 ;; json
                 [cheshire "5.8.1"]

                 ;; csv
                 [org.clojure/data.csv "0.1.4"]

                 ;; error handling
                 [failjure "1.3.0"]

                 ;; logging done right with timbre
                 [com.taoensso/timbre "4.10.0"]

                 ;; yaml config
                 [exoscale/yummy "0.2.6"]

                 ;; Data validation
                 [prismatic/schema "1.1.10"]

                 ;; State management
                 [mount "0.1.16"]
                 
                 ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; Dyne libs
                 ;;
                 ;; Common auxiliary function such as config reader
                 [org.clojars.dyne/auxiliary "0.5.0-SNAPSHOT"]
                 ;; storage lib
                 [org.clojars.dyne/clj-storage "0.9.0"]
                 ;; authentication library
                 [org.clojars.dyne/just-auth "0.5.0-SNAPSHOT"]]


  :pendatic? :warn
  :aliases {"test" "midje"}
  :source-paths ["src"]
  :resource-paths ["resources"
                   "test-resources"]
  :ring    {:init social-wallet.ring/init
            :handler social-wallet.handler/app}

  :profiles {:dev {:dependencies [[ring/ring-mock "0.3.2"]
                                   [midje "1.9.6"]
                                   [javax.servlet/servlet-api "2.5"]]
                    :plugins [[lein-ring "0.12.0"]
                              [lein-midje "3.1.3"]]}}
  )