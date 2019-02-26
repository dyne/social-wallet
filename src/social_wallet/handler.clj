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

            [compojure.core :refer :all]
            [compojure.route :as route]

            [failjure.core :as f]

            [social-wallet
             [webpage :as web]
             [ring :as r]]))

(defroutes app
  (GET "/" [] "<h1>Welcome to the Social Wallet</h1>")
  (GET "/app-state" request
       (web/render "lala"
        [:div
         [:h1 "App State"]
         [:p @r/app-state]]))
  (GET "/login" request
       (web/render "an email"
                   [:div
                    [:h1 (str "Already logged in with account: "
                              (:email "an email"))]
                    [:h2 [:a {:href "/logout"} "Logout"]]]))
  (route/not-found "<h1>Page not found</h1>"))
