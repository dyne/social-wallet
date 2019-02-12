;; social-wallet - A generic social wallet UI which uses the social-wallet-api for a beckend

;; Copyright (C) 2019- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; TODO:

;; This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify this Program, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns social-wallet.session
  (:refer-clojure :exclude [get])
  (:require
   [auxiliary.config :as conf]
   [taoensso.timbre :as log]
   [failjure.core :as f]
   [just-auth.core :as auth]
   [social-wallet.ring :as ring]
   [social-wallet.webpage :as web]))

(defn param [request param]
  (let [value
        (get-in request
                (conj [:params] param))]
    (if (nil? value)
      (f/fail (str "Parameter not found: " param))
      value)))

;; TODO: not working?
(defn get [req arrk]
  {:pre (coll? arrk)}
  (if-let [value (get-in req (conj [:session] arrk))]
    value
    (f/fail (str "Value not found in session: " (str arrk)))))

(defn check-config [request]
  ;; reload configuration from file all the time if in debug mode
  (if-let [session (:session request)]
    (if (contains? session :config)
      (:config session)
      (conf/load-config "social-wallet" conf/default-settings))
    (f/fail "Session not found. ")))

(defn check-account [request]
  ;; check if login is present in session
  (f/attempt-all
   [login (get-in request [:session :auth :email])
    user (auth/get-account @ring/auth login)]
   user
   (f/when-failed [e]
     (->> e f/message
          (str "Unauthorized access. ")
          f/fail))))

(defn check-database []
  (if-let [db @ring/db]
    db
    (f/fail "No connection to database. ")))

(defn check [request fun]
  (f/attempt-all
   [db (check-database)
    config (check-config request)
    account (check-account request)]
    (fun request config account)
    (f/when-failed [e]
      (web/render
       [:div
        (web/render-error (f/message e))
        web/login-form]))))
