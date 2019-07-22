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
            [social-wallet.test-helpers.integration :as ih]
            [social-wallet.test.integration.helpers :as helpers]
            [mount.core :as mount]
            [freecoin-lib.db.freecoin :as db]
            [freecoin-lib.core :as blockchain]
            [just-auth.db
             [account :as account]
             [just-auth :as auth-db]]
            [just-auth.core :as auth]
            [taoensso.timbre :as log])

  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Document]
           [org.jsoup Connection$Method Connection$Response]))


(def username "test")

(def server (atom nil))


(ih/setup-db)
(def freecoin-stores (db/create-freecoin-stores (ih/get-test-db) {}))


(def stores-m (merge 
               freecoin-stores
               (auth-db/create-auth-stores (ih/get-test-db) {:ttl-password-recovery 30})))


(against-background [(before  :facts (storage/empty-db-stores! stores-m))
                     (before
                      :contents (mount/start-with-args {:port 3001
                                                        :host "http://localhost"
                                                        :link-port 3001
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
                                   => "name")
                                 (let [response (helpers/signup username)]
                                   (-> response
                                       (.select "body")
                                       (.select "div")
                                       (.select "h2")
                                       (.text)) => (str "Account created: " username " <" (str username "@mail.com") ">")))

                           (fact "Activate it"
                                 (let [activation-uri (:activation-link (storage/fetch (-> stores/stores :account-store)
                                                                                       (str username "@mail.com")))
                                       response (.get (Jsoup/connect activation-uri))]


                                    ;  (helpers/make-admin stores/stores (str username "@mail.com"))
                                   (-> response
                                       (.select "body")
                                       (.select "h1")
                                       (.text))
                                   => (str "Account activated - " (str username "@mail.com"))))

                           
                           

                           (fact "Log in"
                                 (let [response (helpers/login username)]
                                   (-> response
                                       (.select "div.balance")
                                       (.select "h2")
                                       (.text))
                                   =>  "0"))

                              (fact "make test admin"
                                    (fact "Caution!! Mongo converts the keywords to a string"
                                          (helpers/make-admin stores/stores "test@mail.com")
                                          (->
                                        ;    (log/spy (account/fetch (:account-store stores/stores) "test@mail.com"))
                                           (account/fetch (:account-store stores/stores)  "test@mail.com")
                                           :flags
                                           (first))  => :admin))

                           (fact "Send token to another account"
                                 (let [response
                                       (helpers/send-token "10" "bea" "test description" "apple")]
                                   (-> response
                                       (log/spy response)) => response))



                           (fact "Log out"
                                 (let [response (.get (Jsoup/connect "http://localhost:3001/logout"))]
                                   (-> response
                                       (.select "body")
                                       (.select "div.card-title")
                                       (.text))
                                   => "Login"))

                           (fact "Cannot access the wallet page if not logged in - redirected to login"
                                 (let [response (.get (Jsoup/connect (str "http://localhost:3001/wallet/" (str username "@mail.com"))))]
                                   (-> response
                                       (.select "body")
                                       (.select "div")
                                       (.select "div.alert")
                                       (.text))
                                   => "Error:Please log in to be able to access this info")))

                    (facts "Participant can send freecoins to another account")
                    )
