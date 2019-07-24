(ns social-wallet.components.participants_list
  (:require [just-auth.core :as auth]
            [social-wallet.authenticator :refer [authenticator]]))

(defn render-participants [swapi-params]
  [:table.func--transactions-page--table.table.table-striped
   [:thead
    [:tr
     ;; TODO: from transaction
     [:th "Name"]
     [:th "Email"]
     [:th "Other names"]
     [:th "Send tokens"]
     ]]
   [:tbody
    (let [participants (auth/list-accounts authenticator {})]
      (doall (for [p participants]
               [:tr
                [:td (:name p)]
                [:td [:a {:href (str "/sendto/" (:email p))} (:email p) ]]
                [:td (interpose ", " (:other-names p))]
                [:td [:a.btn.btn-primary.tooltip {:href (str "/sendto/" (:email p))
                                             :data-tooltip (str "Send tokens to " (:name p))}
                 "Send"
                 ]]
                ])))]])