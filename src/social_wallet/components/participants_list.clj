(ns social-wallet.components.participants_list
  (:require [just-auth.core :as auth]
            [social-wallet.authenticator :refer [authenticator]]))

(defn render-participants [swapi-params auth]
  [:table.func--transactions-page--table.table.table-striped
   [:thead
    [:tr
     ;; TODO: from transaction
     [:th "Name"]
     [:th "Email"]
     [:th "Other names"]
     [:th "Send tokens"]
     (if (some #{:admin} (:flags (auth/get-account authenticator (:email auth))))
       [:th "Status"])]]
   [:tbody
    (let [participants (auth/list-accounts authenticator {})]
      (doall (for [p participants]
               (if (or (= true (:activated p)) (some #{:admin} (:flags (auth/get-account authenticator (:email auth)))))
                 [:tr
                  [:td (:name p)]
                  [:td (:email p)]
                  [:td (interpose ", " (:other-names p))]
                  [:td [:a.btn.btn-primary.tooltip {:href (str "/sendto/" (:email p))
                                                    :data-tooltip (str "Send tokens to " (:name p))}
                        "Send"]]
                  (if (some #{:admin} (:flags (auth/get-account authenticator (:email auth))))
                    [:td
                     (if (= true (:activated p))
                       [:form {:action (str "/deactivate?email=" (:email p))
                               :method "post"}
                       [:input.btn.btn-error {:type "submit" :value "deactivate"}]]
                       [:a.btn.primary "activate"])])]))))]])