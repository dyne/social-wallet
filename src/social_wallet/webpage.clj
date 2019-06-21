;; social-wallet - TODO:

;; Copyright (C) 2019- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; TODO:

;; This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify this Program, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns social-wallet.webpage
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [yaml.core :as yaml]
            [taoensso.timbre :as log]
            
            [hiccup.page :as page]
            [hiccup.form :as hf]
            [hiccup.util :as hu]
            
            [clavatar.core :as clavatar]

            [auxiliary.translation :as t]
            [just-auth.core :as auth]

            [social-wallet.swapi :as swapi]
            [social-wallet.authenticator :refer [authenticator]]
            
            [failjure.core :as f]))

(declare render)
(declare render-head)
(declare navbar-guest)
(declare navbar-account)
(declare render-footer)
(declare render-yaml)
(declare render-edn)
(declare render-error)
(declare render-error-page)
(declare render-static)

(defn q [req]
  "wrapper to retrieve parameters"
  ;; TODO: sanitise and check for irregular chars
  (get-in req (conj [:params] req)))

(defn button
  ([url text] (button url text [:p]))

  ([url text field] (button url text field "btn-secondary btn-lg"))

  ([url text field type]
   (hf/form-to [:post url]
               field ;; can be an hidden key/value field (project,
               ;; person, etc using hf/hidden-field)
               (hf/submit-button {:class (str "btn " type)} text))))

(defn button-cancel-submit [argmap]
  [:div
   {:class
    (str "row col-md-6 btn-group btn-group-lg "
         (:btn-group-class argmap))
    :role "group"}
   (button
    (:cancel-url argmap) "Cancel"
    (:cancel-params argmap)
    "btn-primary btn-lg btn-danger col-md-3")
   (button
    (:submit-url argmap) "Submit"
    (:submit-params argmap)
    "btn-primary btn-lg btn-success col-md-3")])


(defn reload-session [request]
  ;; TODO: validation of all data loaded via prismatic schema
  #_(conf/load-config "social-wallet" conf/default-settings)

  )

(defn render
  ([body]
  {:headers {"Content-Type"
             "text/html; charset=utf-8"}
   :body (page/html5
          (render-head)
          [:body ;; {:class "static"}
           navbar-guest
           [:div {:class "container-fluid"} body]
           (render-footer)])})
  ([account body]
   {:headers {"Content-Type"
              "text/html; charset=utf-8"}
    :body (page/html5
           (render-head)
           [:body (if (empty? account)
                    navbar-guest
                    (navbar-account account))
            [:div {:class "container-fluid"} body]
            (render-footer)])}))


(defn render-error
  "render an error message without ending the page"
  [err]
  (log/error "Error occured: " err)
  [:div {:class "alert alert-danger" :role "alert"}
   [:span {:class "far fa-meh"
           :aria-hidden "true" :style "padding: .5em"}]
   [:span {:class "sr-only"} "Error:" ]
   (f/message err)])

(defn render-error-page
  ([]    (render-error-page {} "Unknown"))
  ([err] (render-error-page {} err))
  ([session error]
   (render
    [:div {:class "container-fluid"}
     (render-error error)
     (if-not (empty? session)
       [:div {:class "config"}
        [:h2 "Environment dump:"]
        (render-yaml session)])])))


(defn render-head
  ([] (render-head
       "social-wallet" ;; default title
       "social-wallet"
       "https://social-wallet.dyne.org")) ;; default desc

  ([title desc url]
   [:head [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta
     {:name "viewport"
      :content "width=device-width, initial-scale=1, maximum-scale=1"}]

    [:title title]

    ;; javascript scripts
    (page/include-js  "/static/js/jquery-3.2.1.min.js")
    (page/include-js  "/static/js/bootstrap.min.js")

    ;; cascade style sheets
    (page/include-css "/static/css/bootstrap.min.css")
    (page/include-css "/static/css/json-html.css")
    (page/include-css "/static/css/highlight-tomorrow.css")
    (page/include-css "/static/css/formatters-styles/html.css")
    (page/include-css "/static/css/formatters-styles/annotated.css")
    (page/include-css "/static/css/fa-regular.min.css")
    (page/include-css "/static/css/fontawesome.min.css")
    (page/include-css "/static/css/social-wallet.css")]))

(def navbar-guest
  [:nav
   {:class "navbar navbar-default navbar-fixed-top navbar-expand-md navbar-expand-lg"}
    [:div {:class "navbar-header"}
     [:button {:class "navbar-toggle" :type "button"
               :data-toggle "collapse"
               :data-target "#navbarResponsive"
               :aria-controls "navbarResponsive"
               :aria-expanded "false"
               :aria-label "Toggle navigation"}
      [:span {:class "sr-only"} "Toggle navigation"]
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]]
     [:a {:class "navbar-brand far fa-handshake" :href "/"} "social-wallet"]]
    [:div {:class "collapse navbar-collapse" :id "navbarResponsive"}
     [:ul {:class "nav navbar-nav hidden-sm hidden-md ml-auto"}
      ;; --
      [:li {:class "divider" :role "separator"}]
      [:li {:class "nav-item"}
       [:a {:class "nav-link far fa-address-card"
            :href "/login"} " Login"]]
      [:li [:a {:class "nav-link far fa-address-card"
                 :href "/signup"} " Sign-up"]]
      ]]])

(defn navbar-account [account]
  [:nav {:class "navbar navbar-default navbar-fixed-top navbar-expand-lg"}

    [:div {:class "navbar-header"}
     [:button {:class "navbar-toggle" :type "button"
               :data-toggle "collapse" :data-target "#navbarResponsive"
               :aria-controls "navbarResponsive" :aria-expanded "false"
               :aria-label "Toggle navigation"}
      [:span {:class "sr-only"} "Toggle navigation"]
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]]
     [:a {:class "navbar-brand far fa-handshake" :href "/"} "social-wallet"]]

    [:div {:class "collapse navbar-collapse" :id "navbarResponsive"}
     [:ul {:class "nav navbar-nav hidden-sm hidden-md ml-auto"}
      ;; --
      [:li {:class "divider" :role "separator"}]
      [:li {:role "separator" :class "divider"} ]
      [:li {:class "nav-item"}
       [:a {:class "nav-link far fa-file-code"
            :href "/app-state"} (t/locale [:navbar :conf])]]
      [:li {:class "nav-item"}
       [:a {:class "nav-link far fa-file-code"
            :href (str "/wallet/" (:email account))} (t/locale [:navbar :my-wallet])]]
      [:li {:class "nav-item"}
       [:a {:class "nav-link far fa-file-code"
            :href "/sendto"} (t/locale [:wallet :send])]]
      [:li {:class "nav-item"}
       [:a {:class "nav-link far fa-file-code"
            :href "/transactions"} (t/locale [:navbar :transactions])]]
      [:li {:class "nav-item"}
       [:a {:class "nav-link far fa-file-code"
            :href "/participants"} (t/locale [:navbar :participants])]]
      [:li {:class "nav-item"}
       [:a {:class "nav-link far fa-file-code"
            :href "/tags"} (t/locale [:navbar :tags])]]
      [:li {:class "nav-item"}
       [:a {:class "nav-link far fa-file-code"
            :href "/logout"} (t/locale [:navbar :log-out])]]
      ]]])

(defn render-footer []
  [:footer {:class "row" :style "margin: 20px"}
   [:hr]

   [:div {:class "footer col-lg-3"}
    [:img {:src "/static/img/AGPLv3.png" :style "margin-top: 2.5em"
           :alt "Affero GPLv3 License"
           :title "Affero GPLv3 License"} ]]

   [:div {:class "footer col-lg-3"}
    [:a {:href "https://www.dyne.org"}
     [:img {:src "/static/img/swbydyne.png"
            :alt   "Software by Dyne.org"
            :title "Software by Dyne.org"}]]]
   ])


(defn render-static [body]
  (page/html5 (render-head)
              [:body {:class "fxc static"}

               navbar-guest

               [:div {:class "container"} body]

               (render-footer)
               ]))


;; highlight functions do no conversion, take the format they highlight
;; render functions take edn and convert to the highlight format
;; download functions all take an edn and convert it in target format
;; edit functions all take an edn and present an editor in the target format


(defn render-yaml
  "renders an edn into an highlighted yaml"
  [data]
  [:span
   [:pre [:code {:class "yaml"}
          (yaml/generate-string data)]]
   [:script "hljs.initHighlightingOnLoad();"]])

(defn render-pagination
  [total current uri]
  (let [start-page 1
        per-page 10]
    [:nav
     [:ul.pagination.justify-content-end
      ;; TODO: for now 10 per page fixed, maybe config
      ;; +1 because range starts from 0 and + 1 because per-page fits x times in total but we need x+1 pages
      (for [p (range start-page (+ 2 (quot total per-page)))]
        (if (= (str current) (str p))
          [:li.page-item.active [:a.page-link {:href "#"} p]]
          [:li.page-item [:a.page-link {:href (str uri "?page=" p)} p]]))]]))

(defn render-transactions [account swapi-params query-params uri]
  (let [response (swapi/list-transactions swapi-params (cond-> {}
                                                         query-params (merge query-params)
                                                         account (assoc :account (:email account))))
        transactions (:transactions response)
        total (:total-count response)]
    [:div
     [:table.func--transactions-page--table.table.table-striped
      [:thead
       [:tr
        ;; TODO: from transation
        [:th "From"]
        [:th "To"]
        [:th "Amount"]
        [:th "Time"]
        [:th "Tags"]]]
      [:tbody
       (doall (for [t transactions]
                [:tr
                 [:td (:from-id t)]
                 [:td (:to-id t)]
                 [:td (:amount-text t)]
                 [:td (:timestamp t)]
                 [:td (interpose ", " (:tags t))]]))]]
     (when (= uri "/transactions")
       (render-pagination total (or (:page query-params) 1) uri))])) 

(defn render-participants [swapi-params]
  [:table.func--transactions-page--table.table.table-striped
   [:thead
    [:tr
     ;; TODO: from transation
     [:th "Name"]
     [:th "Email"]
     [:th "Other names"]]]
   [:tbody
    (let [participants (auth/list-accounts authenticator {})]
      (doall (for [p participants]
               [:tr
                [:td (:name p)]
                [:td (:email p)]
                [:td (interpose ", " (:other-names p))]])))]])

(defn render-tags [swapi-params query-params uri]
  [:div [:table.func--transactions-page--table.table.table-striped
         [:thead
          [:tr
           ;; TODO: from transation
           [:th "Tag"]
           [:th "Count"]
           [:th "Amount"]
           [:th "Created by"]
           [:th "Created"]]]
         [:tbody
          (let [tags (swapi/list-tags swapi-params {})]
            (doall (for [t tags]
                     [:tr
                      [:td (:tag t)]
                      [:td (:count t)]
                      [:td (:amount t)]
                      [:td (:created-by t)]
                      [:td (:created t)]])))]]])

(defn render-wallet [account swapi-params uri]
  (let [email (:email account)]
    {:headers {"Content-Type"
               "text/html; charset=utf-8"}
     :body (page/html5
            (render-head)
            [:body ;; {:class "static"}
             (navbar-account account)
             [:div {:class "wallet-details"}
              [:div {:class "card"}
               [:span (str (t/locale [:wallet :name]) ": " (:name account))]
               [:br]
               [:span (str (t/locale [:wallet :email]) ": ") [:a {:href (str "mailto:" email)} email]]
               [:br]
               [:span {:class "qrcode pull-left"}
                [:img {:src (hu/url  "/qrcode/" email)}]]
               [:span {:class "gravatar pull-right"}
                [:img {:src (clavatar/gravatar email :size 87 :default :mm)}]]
               [:div {:class "clearfix"}]]
              (f/if-let-ok? [balance (swapi/balance swapi-params
                                                    (select-keys account [:email]))]
                [:div {:class "balance"}
                 (str (t/locale [:wallet :balance]) ": ")
                 [:span {:class "func--account-page--balance"}]
                 balance]
                (render-error balance))
              [:div
               (render-transactions account swapi-params {} uri)]]
             (render-footer)])}))

(defonce render-sendto
  [:div
   [:h1 "Send tokens"
    [:form {:action "/sendto"
            :method "post"}
     [:input {:type "text" :name "amount"
              :placeholder "amount"
              :class "form-control"
              :style "margin-top: 1em"}]
     [:input {:type "text" :name "to"
              :placeholder "to"
              :class "form-control"
              :style "margin-top: 1em"}]
     [:input {:type "text" :name "tags"
              :placeholder "tags (comma separated)"
              :class "form-control"
              :style "margin-top: 1em"}]
     [:input {:type "submit" :name "sendto-submit" :value "Send"
              :class "btn btn-primary btn-lg btn-block"
              :style "margin-top: 1em"}]]]])

(defn highlight-yaml
  "renders a yaml text in highlighted html"
  [data]
  [:span
   [:pre [:code {:class "yaml"}
          data]]
   [:script "hljs.initHighlightingOnLoad();"]])


(defn highlight-json
  "renders a json text in highlighted html"
  [data]
  [:span
   [:pre [:code {:class "json"}
          data]]
   [:script "hljs.initHighlightingOnLoad();"]])

(defn download-csv
  "takes an edn, returns a csv plain/text for download"
  [data]
  {:headers {"Content-Type"
             "text/plain; charset=utf-8"}
   :body (with-out-str (csv/write-csv *out* data))})

(defn edit-edn
  "renders an editor for the edn in yaml format"
  [data]
  [:div;; {:class "form-group"}
   [:textarea {:class "form-control"
               :rows "20" :data-editor "yaml"
               :id "config" :name "editor"}
    (yaml/generate-string data)]
   [:script {:src "/static/js/ace.js"
             :type "text/javascript" :charset "utf-8"}]
   [:script {:type "text/javascript"}
    (slurp (io/resource "public/static/js/ace-embed.js"))]
   ;; code to embed the ace editor on all elements in page
   ;; that contain the attribute "data-editor" set to the
   ;; mode language of choice
   [:input {:class "btn btn-success btn-lg pull-top"
            :type "submit" :value "submit"}]])

;; (defonce readme
;;   (slurp (io/resource "public/static/README.html")))

(defonce login-form
  [:div
   [:h1 "Login for your  account"
    [:form {:action "/login"
            :method "post"}
     [:input {:type "text" :name "username"
              :placeholder "Username"
              :class "form-control"
              :style "margin-top: 1em"}]
     [:input {:type "password" :name "password"
              :placeholder "Password"
              :class "form-control"
              :style "margin-top: 1em"}]
     [:input {:type "submit" :name "login-submit" :value "Login"
              :class "btn btn-primary btn-lg btn-block"
              :style "margin-top: 1em"}]]]])

(defonce signup-form
  [:div
   [:h1 "Sign Up for a social-wallet account"
    [:form {:action "/signup"
            :method "post"}
     [:input {:type "text" :name "name"
              :placeholder "Name"
              :class "form-control"
              :style "margin-top: 1em"
              :id "signup-name"}]
     [:input {:type "text" :name "email"
              :placeholder "Email"
              :class "form-control"
              :style "margin-top: 1em"}]
     [:input {:type "password" :name "password"
              :placeholder "Password"
              :class "form-control"
              :style "margin-top: 1em"}]
     [:input {:type "password" :name "repeat-password"
              :placeholder "Repeat password"
              :class "form-control"
              :style "margin-top: 1em"}]
     [:input {:type "submit" :name "sign-up-submit" :value "Sign Up"
              :class "btn btn-primary btn-lg btn-block"
              :style "margin-top: 1em"}]]]])
