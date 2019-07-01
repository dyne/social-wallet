(ns social-wallet.components.admin
  (:require [just-auth.core :as auth]
            [social-wallet.authenticator :refer [authenticator]]))

(defn render-admin-panel [swapi-params]
  [:table.func--transactions-page--table.table.table-striped
   [:thead
    [:tr
     ;; TODO: from transaction
     [:th "Name"]
     [:th "Email"]
     [:th "Activated"]
     [:th "Roles"]
     [:th ""]]]
   [:tbody
    (let [participants (auth/list-accounts authenticator {})]
      (doall (for [p participants]
               [:tr
                [:td (:name p)]
                [:td (:email p)]
                [:td (:activated p)]
                [:td (for [role (:flags p)] (if (> (count role) 0) [:div.chip role] [:div]))]
                (if (some #{"admin"} (:flags p)) 
                  [:td]
                  [:td
                   [:a.btn.btn-action {:href (str "/admin-panel/edit/" (:email p)) :style "margin-right: 8px"} [:i.icon.icon-edit]]]
                  )])))]])