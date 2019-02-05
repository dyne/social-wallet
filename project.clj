(defproject social-wallet "0.1.0-SNAPSHOT"
  :description "Basic compojure based authenticated website"
  :url "http://dyne.org"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 ;; ring routing
                 [compojure "1.6.1"]
                 
                 ;; HTTP server abstraction
                 [ring/ring-core "1.7.1"]
                 ;; ring middleware and defaults
                 [ring/ring-defaults "0.3.2"]
                 [ring-middleware-accept "2.0.3"]

                 ;; json
                 [cheshire "5.8.1"]

                 ;; csv
                 [org.clojure/data.csv "0.1.4"]

                 ;; mustache templates
                 [de.ubercode.clostache/clostache "1.4.0"]

                 ;; error handling
                 [failjure "1.3.0"]

                 ;; logging done right with timbre
                 [com.taoensso/timbre "4.10.0"]

                 ;; authentication library
                 [org.clojars.dyne/just-auth "0.4.0"]

                 ;; parsing configs if any
                 [io.forward/yaml "1.0.9"]

                 ;; Data validation
                 [prismatic/schema "1.1.10"]

                 ;; Common auxiliary function such as config reader
                 [org.clojars.dyne/auxiliary "0.5.0-SNAPSHOT"]]
  :aliases {"test" "midje"}
  :source-paths ["src"]
  :resource-paths ["resources"]
  :plugins [[lein-ring "0.12.4"]]
  :ring    {:init social-wallet.ring/init
            :handler social-wallet.handler/app}
  :uberwar {:init social-wallet.ring/init
            :handler social-wallet.handler/app}
  :main social-wallet.handler
  :profiles { :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                   [ring/ring-mock "0.3.2"]
                                   [midje "1.9.6"]]
                    :plugins [[lein-midje "3.1.3"]]
                    :aot :all
                    :main social-wallet.handler}
             :uberjar {:aot  :all
                       :main social-wallet.handler}}
  )
