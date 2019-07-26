(ns social-wallet.components.tag
  (:require [social-wallet.swapi :as swapi]
            [clj-time.format :as ft]
            [failjure.core :as f]))


(def formatter (ft/formatter "dd MMMM, yyyy"))


(defn render-tags [swapi-params query-params uri]
  (f/if-let-ok? [tags (swapi/list-tags swapi-params {})]
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
            (doall (for [t tags]
                     [:tr
                      [:td [:a {:href (str "/transactions/tag/" (:tag t))} [:div.chip (:tag t)]]]
                      [:td (:count t)]
                      [:td (:amount t)]
                      [:td (:created-by t)]
                      [:td (ft/unparse formatter (ft/parse (:created t)))]]))]]]
    (f/message tags)))
