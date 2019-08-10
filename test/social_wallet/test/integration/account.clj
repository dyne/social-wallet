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
            [clojure.java.io :as io]
            [social-wallet
             [core :as sc]
             [handler :as h]
             [swapi :as swapi]
             [config :refer [config] :as c]
             [webpage :as web]
             [authenticator :refer [authenticator]]
             [stores :as stores]]
            [clj-storage.core :as storage :refer [Store]]
            [etaoin.keys :as k]
            [clojure.test :refer :all]
            [ring.util.response :refer [redirect]]
            [org.httpkit.client :as client]
            [mount.core :as mount]
            [taoensso.timbre :as log])
  (:use [etaoin.api])

  (:import 
   [org.jsoup Jsoup]
   [org.jsoup.nodes Document]
   [org.jsoup Connection$Method Connection$Response]))

(def user-data {:name "user1"
                :email "test@mail.com"
                :password "12345678"})

(def admin-data {:name "admin"
                :email "admin@mail.com"
                :password "12345678"})


(def driver (firefox)) ;; here, a Firefox window should appear


(against-background [(before :contents (mount/start-with-args {:port 3001
                                                               :host "http://localhost"
                                                               :link-port 3001
                                                               :stub-email true
                                                               :with-apikey false
                                                               :config "test-resources/config.yaml"}))
                     (after :contents (do
                                        (storage/empty-db-stores! stores/stores)
                                        (mount/stop)))]

                    (let [driver (firefox)]
                      (go driver "http://localhost:3001")
                      (click driver {:tag :a :id :signup})
                      (fill driver {:tag :input :name :name} (:name user-data))
                      (fill driver {:tag :input :name :email} (:email user-data))
                      (fill driver {:tag :input :name :password} (:password user-data))
                      (fill driver {:tag :input :name :repeat-password} (:password user-data))
                      (click driver {:tag :input :name :sign-up-submit})

                      (fact "As a user I can signup"
                            (has-text? driver (str "Account created: " (:name user-data) " <" (:email user-data) ">"))
                            => true)

                      (fact "As a user I can activate my account"
                            (let [activation-uri (:activation-link (storage/fetch (-> stores/stores :account-store)
                                                                                  (:email user-data)))]
                              (go driver activation-uri)
                              (has-text? driver (str "Account activated - " (:email user-data))))
                            => true)


                      (click driver {:tag :a :id :login})
                      (fill driver {:tag :input :name :email} (:email user-data))
                      (fill driver {:tag :input :name :password} (:password user-data))
                      (click driver {:tag :input :name :login-submit})

                      (fact "As a user I can login into the webapp"
                            (has-text? driver "Total balance: 0") => true)

                      (fact "As a user I do not see the admin badge on my profile"
                            (has-text? driver "Admin") => false)

                      (fact "As a user I can check my profile page if logged"
                            (has-text? driver (:name user-data)) => true)

                      (fact "As a user I can check the form to send amount if logged"
                            (click driver {:tag :a :id :sendto})
                            (has-text? driver "Send tokens") => true)
                      (fact "As a user I can check the participants list if logged"
                            (click driver {:tag :a :id :participants})
                            (has-text? driver "Other names") => true)
                      (fact "As a user I can check the tags list if logged"
                            (click driver {:tag :a :id :tags})
                            (has-text? driver "Tag") => true)

                      (fact "As a user I can check the transactions list if logged"
                            (click driver {:tag :a :id :transactions})
                            (has-text? driver "All")
                            => true)


                      (fact "As an admin I can have negative balance"
                            (click driver {:tag :a :id :dropdown})
                            (wait-visible driver {:tag :a :id :logout})
                            (click driver {:tag :a :id :logout})
                            (wait-visible driver {:tag :a :id :signup})
                            (click driver {:tag :a :id :signup})
                            (fill driver {:tag :input :name :name} (:name admin-data))
                            (fill driver {:tag :input :name :email} (:email admin-data))
                            (fill driver {:tag :input :name :password} (:password admin-data))
                            (fill driver {:tag :input :name :repeat-password} (:password admin-data))
                            (click driver {:tag :input :name :sign-up-submit})
                            (let [activation-uri (:activation-link (storage/fetch (-> stores/stores :account-store)
                                                                                  (:email admin-data)))]
                              (go driver activation-uri)
                              (storage/update! (-> stores/stores :account-store) (:email admin-data) (fn [account] (update account :flags #(conj % :admin))))
                              (wait-visible driver {:tag :a :id :login})
                              (click driver {:tag :a :id :login})
                              (fill driver {:tag :input :name :email} (:email admin-data))
                              (fill driver {:tag :input :name :password} (:password admin-data))
                              (click driver {:tag :input :name :login-submit})
                              (click driver {:tag :a :id :sendto})
                              (fill driver {:tag :input :name :amount} 10)
                              (fill driver {:tag :input :name :to} (:email user-data))
                              (fill driver {:tag :input :name :tags} "test, fake")
                              (fill driver {:tag :textarea :name :description} "tokens are good")
                              (click driver {:tag :input :name :sendto-submit}))
                            (has-text? driver "Total balance: -10") => true)

                      (fact "As an admin I can see the admin badge on my profile"
                            (has-text? driver "Admin") => true)


                      (fact "As a user I can receive some tokens"
                            (click driver {:tag :a :id :dropdown})
                            (wait-visible driver {:tag :a :id :logout})
                            (click driver {:tag :a :id :logout})
                            (wait-visible driver {:tag :a :id :login})
                            (click driver {:tag :a :id :login})
                            (fill driver {:tag :input :name :email} (:email user-data))
                            (fill driver {:tag :input :name :password} (:password user-data))
                            (click driver {:tag :input :name :login-submit})
                            (has-text? driver "Total balance: 10") => true)

                      (fact "As a user I can send tokens to another participants"
                            (click driver {:tag :a :id :sendto})
                            (fill driver {:tag :input :name :amount} 10)
                            (fill driver {:tag :input :name :to} (:email admin-data))
                            (fill driver {:tag :input :name :tags} "fake")
                            (click driver {:tag :input :name :sendto-submit})
                            (has-text? driver "Total balance: 0") => true)

                      (fact "Send amount bigger than the user balance && check the correct error message"
                            (click driver {:tag :a :id :sendto})
                            (fill driver {:tag :input :name :amount} 10)
                            (fill driver {:tag :input :name :to} (:email admin-data))
                            (fill driver {:tag :input :name :tags} "fake")
                            (click driver {:tag :input :name :sendto-submit})
                            (has-text? driver "Not enough funds to make a transaction.")
                            => true)

                      (fact "Send a transaction to an invalid receiver && check the correct error message"
                            (click driver {:tag :a :id :sendto})
                            (fill driver {:tag :input :name :amount} 10)
                            (fill driver {:tag :input :name :to} "Duane Allman")
                            (fill driver {:tag :input :name :tags} "fake")
                            (click driver {:tag :input :name :sendto-submit})
                            (has-text? driver "The receiver is not a valid account.")
                            => true)

                      (fact "Send a transaction without receiver && check the correct error message"
                            (click driver {:tag :a :id :sendto})
                            (fill driver {:tag :input :name :amount} 10)
                            (fill driver {:tag :input :name :tags} "fake")
                            (click driver {:tag :input :name :sendto-submit})
                            (has-text? driver "The receiver is not a valid account.")
                            => true)

                      (fact "As a user I can logout from the webapp"
                            (click driver {:tag :a :id :dropdown})
                            (wait-visible driver {:tag :a :id :logout})
                            (click driver {:tag :a :id :logout})
                            (has-text? driver "Your social wallet") => true)

                      (fact "Authenticated page without an active session returns the correct error message"
                            (go driver "http://localhost:3001/sendto")
                            (has-text? driver "Please log in to be able to access this info") => true))
                    )
