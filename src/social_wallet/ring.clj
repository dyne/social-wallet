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
            [clojure.spec.alpha :as spec]
            [yummy.config :as yc]
            [clj-storage.db.mongo :as mongo]
            [just-auth.db.just-auth :as auth-db]
            [just-auth.core :as auth]
            [failjure.core :as f]
            [mount.core :refer [defstate]]
            social-wallet.spec))

(defonce app-state (atom {}))

(defn conf->mongo-uri [mongo-conf]
  (str "mongodb://" (:host mongo-conf) ":" (:port mongo-conf) "/" (:db mongo-conf)))

(defn connect-db [app-state]
  (if (:db app-state)
    app-state
    (f/attempt-all [uri (conf->mongo-uri (-> app-state :config :just-auth :mongo-config))
                    db (mongo/get-mongo-db uri)]
                   (assoc app-state :db db)
                   (f/if-failed [e]
                                (log/error (str "Could not connect to db: " (f/message e)))
                                (System/exit 0)))))

(defn init []
  (log/info "Loading config...")
  (swap! app-state #(assoc % :config (yc/load-config {:path "config.yaml" :spec ::config})))
  (log/info "Config loaded.")
  
  ;; Connect to DB
  (swap! app-state connect-db)
  
  ;; Create collections
  (swap! app-state #(assoc % :stores (auth-db/create-auth-stores (:db @app-state))))

  (log/info "APP STATE " @app-state)
  
  ;; start authenticator
  (f/attempt-all [config-path (-> @app-state :config :just-auth :email-config)
                  config (yc/load-config {:path config-path
                                          :spec ::email-conf})
                  authenticator (auth/email-based-authentication
                                 (:stores @app-state)
                                 config
                                 (-> @app-state :config :just-auth :throttling))]
                 (swap! app-state  #(assoc % :authenticator authenticator))
                 (f/if-failed [e]
                              (log/error (str "Could start the authentication service: " (f/message e)))
                              (System/exit 0)))
  

  ;; Start connection to swapi
  ;; TODO: think here, treat swapi as separate instance?
)
