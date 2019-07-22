
(ns social-wallet.components.tag
 (:require [social-wallet.swapi :as swapi]))

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
                      [:td [:a {:href (str "/transactions/tag/" (:tag t))} [:div.chip (:tag t)]]]
                      [:td (:count t)]
                      [:td (:amount t)]
                      [:td (:created-by t)]
                      [:td (:created t)]])))]]])
