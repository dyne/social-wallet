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
            [ring.util.response :refer [redirect]]

            [failjure.core :as f]
            [mount.core :as mount]
            [clj-storage.core :refer [update!]]

            [social-wallet.webpage :as web]
            [social-wallet.qrcode :as qrcode]
            [social-wallet.config :refer [config] :as c]
            [social-wallet.authenticator :refer [authenticator]]
            [social-wallet.swapi :as swapi]
            [social-wallet.util :as u]
            [social-wallet.components.qrcode :refer [qrcode-page]]
            [social-wallet.components.transactions_list :refer [transactions]]
            [social-wallet.components.participants_list :refer [render-participants]]
            [social-wallet.components.tag :refer [render-tags]]
            [social-wallet.components.sendTo :refer [render-sendTo]]
            [social-wallet.components.login :refer [login-form]]
            [social-wallet.components.signup :refer [signup-form]]
            [social-wallet.components.recover :refer [recover-form]]
            [social-wallet.components.reset :refer [reset-form]]
            [social-wallet.pages.wallet :refer [wallet-page]]
            [buddy.hashers :as hashers]

            [just-auth.core :as auth]
            [just-auth.db
             [account :as account]
             [password-recovery :as pr]]
            [taoensso.timbre :as log]))

(defn get-host [port] (if port
                        (str (:host (mount/args)) ":" port)
                        (:host (mount/args))))

(defn logged-in? [session-auth]
  (if session-auth
    true
    (f/fail "Please log in to be able to access this info")))

(defroutes app-routes

  (GET "/" request
    (let [{{:keys [auth]} :session}  request
          {{:keys [page per-page tag]} :params} request]
      (if (and auth (auth/get-account authenticator auth))
        (wallet-page auth (c/get-swapi-params) (:uri request) (cond-> {}
                                                                tag (assoc :tags (list tag))
                                                                page (assoc :page page)
                                                                per-page (assoc :per-page per-page)))
        (web/render (login-form (c/get-swapi-params))))))


  (GET "/app-state" {{:keys [auth]} :session}
    (web/render auth
                [:div
                 [:h1 "Config Keys loaded"]
                 [:ul (for [[x _] config] [:li x])]]))


  ;; TODO: change conf (POST app-state)
  (GET "/login" {{:keys [auth]} :session
                 {:keys [mime language]} :accept}
    (if (and auth (auth/get-account authenticator auth))
      (web/render auth
                  [:div
                   [:div.toast.toast-warning (str "Already logged in with account: " (:email auth))]
                   [:a.btn.btn-primary {:href "/logout" :style "margin-top: 16px"} "Logout"]])
      (web/render (login-form (c/get-swapi-params)))))



  (POST "/login" request
    (f/attempt-all
     [email (-> request :params :email)
      password (-> request :params :password)
      account (auth/sign-in  authenticator email password {})]
         ;; TODO: pass :ip-address in last argument map
     (let [session {:auth account}]
       (-> (redirect "/")
           (assoc :session session)))
     (f/when-failed [e]
                    (web/render-error-page
                     (str "Login failed: " (f/message e))))))


  (GET "/wallet/:email" request
    (let [{{:keys [auth]} :session
           {:keys [email]} :route-params} request]
      (f/if-let-ok? [auth-resp (logged-in? auth)]
                    (if (and auth (= (:email auth) email))
                      (wallet-page auth (c/get-swapi-params) (:uri request))
                      (redirect "/login"))
                    (web/render-error-page (f/message auth-resp)))))


  (GET "/transactions" request
    (let [{{:keys [auth]} :session} request
          {{:keys [page tag per-page]} :params} request]
      (f/if-let-ok? [auth-resp (logged-in? auth)]
                    (web/render auth (transactions auth
                                                   nil
                                                   (c/get-swapi-params)
                                                   (cond-> {}
                                                     tag (assoc :tags (list tag))
                                                     page (assoc :page page)
                                                     per-page (assoc :per-page per-page))
                                                   (:uri request)))
                    (web/render-error-page (f/message auth-resp)))))

  (GET "/participants" request
    (let [{{:keys [auth]} :session} request]
      (f/if-let-ok? [auth-resp (logged-in? auth)]
                    (web/render auth (render-participants (c/get-swapi-params) auth))
                    (web/render-error-page (f/message auth-resp)))))
  (GET "/tags" request
    (let [{{:keys [auth]} :session} request
          {{:keys [page per-page]} :params} request]
      (f/attempt-all [auth-resp (logged-in? auth)
                      tags (render-tags (c/get-swapi-params)
                                        (cond-> {}
                                          page (assoc :page page)
                                          per-page (assoc :per-page per-page))
                                        (:uri request))]
                     (web/render auth tags)
                     (f/when-failed [e]
                                    (log/error (f/message e))
                                    (web/render-error-page (f/message e))))))

  (GET "/signup" request
    (web/render signup-form))

  (GET "/recover-password" request
    (web/render recover-form))

  (POST "/recover-password" request
    (f/attempt-all
     [email (-> request :params :email)
      recover {:reset-uri (get-host (:link-port (mount/args)))}]
     (web/render
      (f/try*
       (f/if-let-ok?
        [recover (auth/send-password-reset-message
                  authenticator
                  email
                  recover)]
        [:div
         [:h2 (str "reset created: "
                   " &lt;" email "&gt;")]
         [:h3 "chec your mail."]]
        (web/render-error
         (str "Failure creating account: "
              (f/message recover))))))
     (f/when-failed [e]
                    (web/render-error-page
                     (str "Sign-up failure: " (f/message e))))))


  (POST "/reset-password/:email/:token" request
    (f/attempt-all
     [email (-> request :params :email)
      password (-> request :params :password)
      repeat-password (-> request :params :repeat)
      reset {:password-reset-link (get-host (:link-port (mount/args)))}]
     (web/render
      (if (= password repeat-password)
        (f/try*
         (f/if-let-ok?
          [reset-psw (auth/reset-password authenticator
                                          email
                                          password
                                          reset)]
          [:div
           [:h2 (str "password created: "
                      " &lt;" email "&gt;")]]
          (web/render-error
           (str "Failure creating account: "
                (f/message reset-psw)))))
        (web/render-error
         "Repeat password didn't match")))
     (f/when-failed [e]
                    (web/render-error-page
                     (str "reset password failure: " (f/message e))))))


  (POST "/signup" request
    (f/attempt-all
     [name (-> request :params :name)
      email (-> request :params :email)
      password (-> request :params :password)
      repeat-password (-> request :params :repeat-password)
      activation {:activation-uri (get-host (:link-port (mount/args)))}]
     (web/render
      (if (= password repeat-password)
        (f/try*
         (f/if-let-ok?
          [signup (auth/sign-up authenticator
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

  (GET "/reset-password/:email/:token"
    [email token :as request]
    (let [reset-uri
          (str (get-host (:link-port (mount/args)))
               "/activate/" email "/" token)]
      (web/render (reset-form email token reset-uri))))



  (GET "/activate/:email/:activation-id"
    [email activation-id :as request]
    (let [activation-uri
          (str (get-host (:link-port (mount/args)))
               "/activate/" email "/" activation-id)]
      (web/render
       [:div
        (f/if-let-failed?
         [act (auth/activate-account
               authenticator
               email
               {:activation-link activation-uri})]
         (web/render-error
          [:div
           [:h1 "Failure activating account"]
           [:h2 (f/message act)]
           [:p (str "Email: " email " activation-id: " activation-id)]])
         [:h1 (str "Account activated - " email)])])))

  (GET "/qrcode/:email"
    [email :as request]
    (qrcode/transact-to email  (get-host (:link-port (mount/args)))))


  (GET "/session" request
    (-> (:session request) web/render-yaml web/render))


  (GET "/logout" request
    (conj {:session nil}
          ; (web/render [:h1 "Logged out."])
          (redirect "/")))


  (GET "/qrcode" request
    (let [{{:keys [auth]} :session} request]
      (f/if-let-ok? [auth-resp (logged-in? auth)]
                    (web/render auth (qrcode-page (:email auth)))
                    (web/render-error-page (f/message auth-resp)))))


  (GET "/sendto" request
    (let [{{:keys [auth]} :session} request]
      (f/if-let-ok? [auth-resp (logged-in? auth)]
                    (web/render auth (render-sendTo))
                    (web/render-error-page (f/message auth-resp)))))


  (GET "/sendto/:email" request
    (let [{{:keys [auth]} :session
           {:keys [email]} :route-params}
          request]

      (f/if-let-ok? [auth-resp (logged-in? auth)]
                    (web/render auth (render-sendTo email))
                    (web/render-error-page (f/message auth-resp)))))


  (POST "/deactivate" request
    (let [{{:keys [email]} :params} request
          {{:keys [auth]} :session} request]
      (if (some #{:admin} (:flags (auth/get-account authenticator (:email auth))))
        (do (update! authenticator email #(assoc % :activated false))
            (redirect "/participants")))
      (println email)))

  (POST "/sendto" request
    (let [{{:keys [amount to tags description]} :params} request
          {{:keys [auth]} :session} request
          {{:keys [page tag per-page]} :params} request]
      (f/attempt-all
         ;; TODO: specs dont work
       [parsed-amount (u/spec->failjure ::amount amount #(BigDecimal. %))
        parsed-to (u/spec->failjure ::to to)
        parsed-tags (u/spec->failjure ::tags tags #(clojure.string/split % #","))
        sender-balance (swapi/balance (c/get-swapi-params) {:email (:email auth)})]
       (if (:activated (auth/get-account authenticator to))
         (if (or
              (>= (- sender-balance parsed-amount) 0)
              (some #{:admin} (:flags (auth/get-account authenticator (:email auth)))))
           (do (swapi/sendto (c/get-swapi-params) {:amount amount
                                                   :to to
                                                   :description description
                                                   :from (:email auth)
                                                   :tags parsed-tags})
               (redirect "/"))
           (web/render-error-page auth "Not enough funds to make a transaction."))
         (web/render-error-page auth "The receiver is not a valid account."))

       (f/when-failed [e]
           ;; TODO: make it appear in form
                      (web/render-error-page
                       auth
                       (str "Error in send request: " (f/message e)))))))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))