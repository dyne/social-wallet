;; social-wallet - A generic social wallet UI which uses the social-wallet-api for a beckend

;; Copyright (C) 2019- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; TODO:

;; This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify this Program, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns social-wallet.handler
  (:require [compojure.core :refer [defroutes routes GET POST]]
            [compojure.route :as route]

            [failjure.core :as f]

            [social-wallet.webpage :as web]

            [just-auth.core :as auth]

            [taoensso.timbre :as log]))

(defonce app-state (atom {}))

(def welcome-html (str "<h1>Welcome to the Social Wallet</h1>\n"
                          "<p>" #_request "</p>"))

(defroutes app-routes
  (GET "/" request welcome-html)
  (GET "/app-state" request
       (web/render
        [:div
         [:h1 "Config Keys loaded"]
         [:ul (for [[x y] @app-state] [:li x])]]))
  (GET "/login" {{:keys [user-id]} :session
                 {:keys [mime language]} :accept}
       (log/debug "MIME " mime "\n LAGUAGE " language)
       (if (and user-id (auth/get-account (-> @app-state :authenticator) user-id))
         (web/render user-id
                     [:div
                      [:h1 (str "Already logged in with account: " user-id)]
                      [:h2 [:a {:href "/logout"} "Logout"]]])
         (web/render web/login-form)))
  (POST "/login" {{:keys [username password]} :params}
        (f/attempt-all
         [username username
          password password
          logged (auth/sign-in  (-> @app-state :authenticator) username password {})]
         ;; TODO: pass :ip-address in last argument map
         (let [session {:session {:auth (log/spy :info logged)}}]
           (conj session
                 (web/render
                  logged
                  [:div
                   [:h1 "Logged in: " username]
                   (web/render-yaml session)])))
         (f/when-failed [e]
           (web/render-error-page
            (str "Login failed: " (f/message e))))))
  (GET "/signup" request
       (web/render web/signup-form))
  (POST "/signup" request
        (f/attempt-all
         [name (-> request :params :name)
          email (-> request :params :email)
          password (-> request :params :password)
          repeat-password (-> request :params :repeat-password)
          activation {:activation-uri
                      (get-in request [:headers "host"])}]
         (web/render
          (if (= password repeat-password)
            (f/try*
             (f/if-let-ok?
                 [signup (auth/sign-up (-> @app-state :authenticator)
                                       name
                                       email
                                       password
                                       activation
                                       [])]
               [:div
                [:h2 (str "Account created: "
                          name " &lt;" email "&gt;")]
                [:h3 "Account pending activation."]]
               (web/render-error
                (str "Failure creating account: "
                     (f/message signup)))))
            (web/render-error
             "Repeat password didnt match")))
         (f/when-failed [e]
           (web/render-error-page
            (str "Sign-up failure: " (f/message e))))))
  (GET "/activate/:email/:activation-id"
       [email activation-id :as request]
       (let [activation-uri
             (str "http://"
                  (get-in request [:headers "host"])
                  "/activate/" email "/" activation-id)]
         (web/render
          [:div
           (f/if-let-failed?
               [act (auth/activate-account
                     (-> @app-state :authenticator)
                     email
                     {:activation-link activation-uri})]
             (web/render-error
              [:div
               [:h1 "Failure activating account"]
               [:h2 (f/message act)]
               [:p (str "Email: " email " activation-id: " activation-id)]])
             [:h1 (str "Account activated - " email)])])))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))
