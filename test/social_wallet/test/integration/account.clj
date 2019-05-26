;; social-wallet - A generic social wallet UI which uses the social-wallet-api for a beckend

;; Copyright (C) 2019- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti <aspra@dyne.org>

;; This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify this Program, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns social-wallet.test.integration.account
  (:require [midje.sweet :refer [against-background before after facts fact =>]]
            [social-wallet
             [core :as sc]
             [handler :as h]
             [webpage :as web]
             [stores :as stores]]
            [clj-storage.core :as storage]

            [mount.core :as mount]
            [taoensso.timbre :as log])
  
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Document]
           [org.jsoup Connection$Method Connection$Response]))

(def user-data {:name "user1"
                :email "user@mail.com"
                :password "12345678"})
(def server (atom nil))

(against-background [(before :contents (mount/start-with-args {:port 3001
                                                               :stub-email true
                                                               :with-apikey false
                                                               :config "test-resources/config.yaml"}))
                     (after :contents (do
                                        (storage/empty-db-stores! stores/stores)
                                        (mount/stop)))]

                    (facts "Create and account, activate it, login and logout."
                           (fact "create and account"
                                 (let [response (.get (Jsoup/connect "http://localhost:3001/signup"))]
                                   (-> response
                                       (.select "form")
                                       first
                                       (.select "[name$=sign-up-submit]")
                                       (.hasClass "btn"))
                                   => true

                                   (-> response
                                       (.select "form")
                                       first
                                       (.select "[placeholder$=Name]")
                                       first
                                       (.id))
                                   => "signup-name")
                                 (let [response (->
                                                 (Jsoup/connect "http://localhost:3001/signup")
                                                 (.userAgent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                                                 (.header "Content-Type","application/x-www-form-urlencoded")
                                                 (.data "name" (:name user-data))
                                                 (.data "email" (:email user-data))
                                                 (.data "password" (:password user-data))
                                                 (.data "repeat-password" (:password user-data))
                                                 (.data "sing-up-submit" "Sign up")
                                                  (.post))]
                                   (-> response
                                       (.select "body")
                                       (.select "div.container-fluid")
                                       (.select "h2")
                                       (.text)) => (str "Account created: " (:name user-data) " <"(:email user-data) ">")))

                           (fact "Activate it"
                                 (let [activation-uri (:activation-link (storage/fetch (-> stores/stores :account-store)
                                                                                       (:email user-data)))
                                       response (.get (Jsoup/connect activation-uri))]
                                   (-> response
                                       (.select "body")
                                       (.select "div.container-fluid")
                                       (.text))
                                   => (str "Account activated - " (:email user-data))))

                           (fact "Log in"
                                 (let [response (->
                                                 (Jsoup/connect "http://localhost:3001/login")
                                                 (.userAgent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                                                 (.header "Content-Type","application/x-www-form-urlencoded")
                                                 (.data "username" (:email user-data))
                                                 (.data "password" (:password user-data))
                                                 (.data "login-submit" "Login")
                                                 (.post))]
                                   (-> response
                                       (.select "div.balance")
                                       (.text))
                                   => "Balance: 0"))

                           (fact "Log out"
                                 (let [response (.get (Jsoup/connect "http://localhost:3001/logout"))]
                                   (-> response
                                       (.select "body")
                                       (.select "div.container-fluid")
                                       (.text))
                                   => "Logged out."))

                           (fact "Cannot access the wallet page if not logged in - redirected to login"
                                 (let [response (.get (Jsoup/connect (str "http://localhost:3001/wallet/" (:email user-data))))]
                                   (-> response
                                       (.select "body")
                                       (.select "div.container-fluid")
                                       (.select "div.alert")
                                       (.text))
                                   => "Error:Please log in to be able to access this info"))))
