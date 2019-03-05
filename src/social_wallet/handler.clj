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
  (:require [ring.middleware.defaults :refer
             [wrap-defaults site-defaults]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.accept :refer [wrap-accept]]

            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]

            [failjure.core :as f]

            [social-wallet
             [webpage :as web]
             [ring :as r]]

            [just-auth.core :as auth]

            [taoensso.timbre :as log]))

(defroutes app-routes
  (GET "/" [] "<h1>Welcome to the Social Wallet</h1>")
  (GET "/app-state" request
       (web/render
        [:div
         [:h1 "Config Keys loaded"]
         [:ul (for [[x y] @r/app-state] [:li x])]]))
  (GET "/login" {{:keys [user-id]} :session}
       (if (and user-id (auth/get-account (-> @r/app-state :authenticator) user-id))
         (web/render user-id
                     [:div
                      [:h1 (str "Already logged in with account: " user-id)]
                      [:h2 [:a {:href "/logout"} "Logout"]]])
         (web/render web/login-form)))
  (POST "/login" {{:keys [username password]} :params}
        (f/attempt-all
         [username username
          password password
          logged (auth/sign-in  (-> @r/app-state :authenticator) username password {})]
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
  
  #_(GET "/login" request
       (web/render 
                   [:div
                    [:h1 (str "Already logged in with account: "
                              (:email "an email"))]
                    [:h2 [:a {:href "/logout"} "Logout"]]]))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
      (wrap-defaults site-defaults)
      (wrap-accept {:mime ["text/html"
                           "text/plain"]
                    ;; preference in language, fallback to english
                    :language ["en" :qs 0.5
                               "it" :qs 1
                               "nl" :qs 1
                               "hr" :qs 1]})))
