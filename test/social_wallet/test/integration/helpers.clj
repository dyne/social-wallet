(ns social-wallet.test.integration.helpers
  (:require [just-auth.db.account :as account]
            [social-wallet
             [stores :as stores]]
            [taoensso.timbre :as log]
            )
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Document]
           [org.jsoup Connection$Method Connection$Response]))

(defn make-admin [stores-m email]
  (account/add-flag! (:account-store stores-m) email :admin))

(defn remove-admin [stores-m email]
  (account/remove-flag! (:account-store stores-m) email :admin))

(def password "testtest")

(defn login [name]
  (let [email (str name "@mail.com")]
    (->
     (Jsoup/connect "http://localhost:3001/login")
     (.userAgent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
     (.header "Content-Type","application/x-www-form-urlencoded")
     (.data "username" email)
     (.data "password" password)
     (.data "login-submit" "Login")
     (.post))))

(defn signup [name]
  (let [email (str name "@mail.com")]
    (-> (Jsoup/connect "http://localhost:3001/signup")
        (.userAgent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
        (.header "Content-Type","application/x-www-form-urlencoded")
        (.data "name" name)
        (.data "email" email)
        (.data "password" password)
        (.data "repeat-password" password)
        (.data "sing-up-submit" "Sign up")
        (.post))))


(defn send-token [amount to description tags]
  (let [email (str to "@mail.com")]
    (log/info (account/fetch (:account-store stores/stores) "test@mail.com"))
    (-> 
     (Jsoup/connect "http://localhost:3001/sendto")
     (.userAgent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
     (.header "Content-Type","application/x-www-form-urlencoded")
     (.data "amount" amount)
     (.data "to" email)
     (.data "description" description)
     (.data "tags" tags)
     (.data "sendto-submit" "Send")
     (.post))))