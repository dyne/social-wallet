;; social-wallet - A generic social wallet UI which uses the social-wallet-api for a beckend

;; Copyright (C) 2019- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti <aspra@dyne.org>

;; This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify this Program, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns social-wallet.ring
  (:require [taoensso.timbre :as log]
            
            [yummy.config :as yc]
            
            [clj-storage.db.mongo :as mongo]
            [just-auth.db.just-auth :as auth-db]
            [just-auth.core :as auth]

            [failjure.core :as f]

            [clojure.spec.alpha :as spec]
            social-wallet.spec))

(defonce app-state (atom {}))

(defn conf->mongo-uri [mongo-conf]
  (str "mongodb://" (:host mongo-conf) ":" (:port mongo-conf) "/" (:db mongo-conf)))

(defn connect-db [app-state]
  (if (:db app-state)
    app-state
    (f/attempt-all [uri (conf->mongo-uri (-> app-state :config :just-auth :mongo-config))
                    db (mongo/get-mongo-db-and-conn uri)]
                   (assoc app-state :db db)
                   (f/if-failed [e]
                                (log/error (str "Could not connect to db: " (f/message e)))
                                (System/exit 0)))))

(defn disconnect-db [app-state]
  (if (:db app-state)
    (do
      (mongo/disconnect (log/spy (:conn (:db app-state))))
      (dissoc app-state :db))
    (log/warn "Could not disconnect db.")))

(defn exception->failjure
  [e msg]
  (f/fail (str msg ": " {:cause e})))

(defn init-logger [log-level]
  (log/merge-config! {:level (keyword log-level)
                      ;; #{:trace :debug :info :warn :error :fatal :report}

                      ;; Control log filtering by
                      ;; namespaces/patterns. Useful for turning off
                      ;; logging in noisy libraries, etc.:
                      :ns-whitelist  ["social-wallet.*"
                                      "freecoin-lib.*"
                                      "clj-storage.*"
                                      "just-auth.*"]
                      :ns-blacklist  ["org.eclipse.jetty.*"]}))

(defn init
  ([]
   (init "config.yaml"))
  ([path]
   (f/attempt-all [_ (log/info "Loading config...")
                   config (yc/load-config {:path path
                                           :spec ::config
                                           :die-fn exception->failjure})
                   _ (swap! app-state #(assoc % :config config))
                   _ (log/info "Config loaded!")
                   ;; Initialising logger
                   _ (init-logger (or (:log-level config) "info"))
                   ;; Connect to DB
                   _ (log/info "Connecting to DB...")
                   _ (swap! app-state connect-db)
                   _ (log/info "Connected to DB!")
                   ;; Create collections
                   _ (log/info "Creating collections...")
                   _ (swap! app-state #(assoc % :stores (auth-db/create-auth-stores
                                                         (-> @app-state :db :db))))
                   _ (log/info "Collections created!")

                   ;; Starting authenticator
                   _ (log/info "Starting authenticator...")
                   config-path (-> @app-state :config :just-auth :email-config)
                   email-config (yc/load-config {:path (log/spy config-path)
                                                 :spec ::email-conf
                                                 :die-fn exception->failjure})
                   ;; start authenticator
                   authenticator (auth/email-based-authentication
                                  (:stores @app-state)
                                  email-config
                                  (-> @app-state :config :just-auth :throttling))
                   _ (log/info "Collections created!")]
                  (swap! app-state  #(assoc % :authenticator authenticator))

                  ;; Start connection to swapi
                  ;; TODO: think here, treat swapi as separate instance?

                  ;; ERROR HANDLING
                  (f/if-failed [e]
                               (log/error (str "Could start the service: " (f/message e)))
                               (swap! app-state disconnect-db)))))

(defn destroy []
  (swap! app-state disconnect-db))
