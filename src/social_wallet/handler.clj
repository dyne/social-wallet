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

            [social-wallet.webpage :as web]
            [social-wallet.qrcode :as qrcode]
            [social-wallet.config :refer [config] :as c]
            [social-wallet.authenticator :refer [authenticator]]
            [social-wallet.swapi :as swapi]
            [social-wallet.util :as u]

            [just-auth.core :as auth]

            [taoensso.timbre :as log]))

(def welcome-html (str "<h1>Welcome to the Social Wallet</h1>\n"
                          "<p>" #_request "</p>"))
(defn get-host [request] (str (:host (log/spy (mount/args))) ":" (:port (mount/args))))

(defn logged-in? [session-auth]
  (if session-auth
    true
    (f/fail "Please log in to be able to access this info") ))

(defroutes app-routes
  (GET "/" {{:keys [auth]} :session}
       (web/render auth welcome-html))
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
                      [:h1 (str "Already logged in with account: " auth)]
                      [:h2 [:a {:href "/logout"} "Logout"]]])
         (web/render web/login-form)))
  (POST "/login" request
        (f/attempt-all
         [username (-> request :params :username)
          password (-> request :params :password)
          account (auth/sign-in  authenticator username password {})]
         ;; TODO: pass :ip-address in last argument map
         (let [session {:session {:auth account}}]
           (conj session
                 (redirect (str "/wallet/" username))))
         (f/when-failed [e]
           (web/render-error-page
            (str "Login failed: " (f/message e))))))
  (GET "/wallet/:email" request
       (let [{{:keys [auth]} :session
              {:keys [email]} :route-params} request]
         (f/if-let-ok? [auth-resp (logged-in? auth)]
           (if (and auth (= (:email auth) email))
             (web/render-wallet auth (c/get-swapi-params) (:uri request))
             (redirect "/login"))
           (web/render-error-page (f/message auth-resp)))))
  (GET "/transactions" request
       (let [{{:keys [auth]} :session} request
             {{:keys [page per-page]} :params} request]
         (f/if-let-ok? [auth-resp (logged-in? auth)]
           (web/render auth (web/render-transactions auth
                                                     (c/get-swapi-params)
                                                     (cond-> {}
                                                       page (assoc :page page)
                                                       per-page (assoc :per-page per-page))
                                                     (:uri request)))
           (web/render-error-page (f/message auth-resp)))))
  (GET "/participants" request
       (let [{{:keys [auth]} :session} request]
         (f/if-let-ok? [auth-resp (logged-in? auth)]
           (web/render auth (web/render-participants (c/get-swapi-params)))
           (web/render-error-page (f/message auth-resp)))))
  (GET "/tags" request
       (let [{{:keys [auth]} :session} request
             {{:keys [page per-page]} :params} request]
         (f/if-let-ok? [auth-resp (logged-in? auth)]
           (web/render auth (web/render-tags (c/get-swapi-params)
                                             (cond-> {}
                                                       page (assoc :page page)
                                                       per-page (assoc :per-page per-page))
                                             (:uri request)))
           (web/render-error-page (f/message auth-resp)))))
  (GET "/signup" request
       (web/render web/signup-form))
  (POST "/signup" request
        (f/attempt-all
         [name (-> request :params :name)
          email (-> request :params :email)
          password (-> request :params :password)
          repeat-password (-> request :params :repeat-password)
          activation {:activation-uri (get-host request)}]
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
  (GET "/activate/:email/:activation-id"
       [email activation-id :as request]
       (let [activation-uri
             (str (get-host request)
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
       (qrcode/transact-to email  (get-host request)))
  (GET "/session" request
       (-> (:session request) web/render-yaml web/render))
  (GET "/logout" request
       (conj {:session nil}
             (web/render [:h1 "Logged out."])))
  (GET "/sendto" request
       (let [{{:keys [auth]} :session} request ]
         (f/if-let-ok? [auth-resp (logged-in? auth)]
           (web/render auth web/render-sendto)
           (web/render-error-page (f/message auth-resp)))))
  (POST "/sendto" {{:keys [amount to tags]} :params
                  {:keys [auth]} :session}
        (f/attempt-all
         ;; TODO: specs dont work
         [parsed-amount (u/spec->failjure ::amount amount #(BigDecimal. %))
          parsed-to (u/spec->failjure ::to to)
          parsed-tags (u/spec->failjure ::tags tags #(clojure.string/split % #","))
          sender-balance (swapi/balance (c/get-swapi-params) {:account-id (:email auth)})]
         (if (or
              (>= (- sender-balance parsed-amount) 0)
              (some #{:admin} (:flags (auth/get-account authenticator (:email auth)))))
           (do (swapi/sendto (c/get-swapi-params) {:amount amount
                                                      :to to
                                                      :from (:email auth) 
                                                   :tags parsed-tags})
               ;; TODO: here we dont need the uri cause there is no paging needed.
               ;; However passing nil is pretty bad
               (web/render-wallet auth (c/get-swapi-params) nil))
           (web/render-error-page "Not enough funds to make a transaction."))
         (f/when-failed [e]
           ;; TODO: make it appear in form
           (web/render-error-page
            (str "Error in send request: " (f/message e))))))
  (route/resources "/")
  (route/not-found "<h1>Page not found</h1>"))
