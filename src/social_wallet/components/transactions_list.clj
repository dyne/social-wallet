(ns social-wallet.components.transactions_list
  (:require
   [social-wallet.components.pagination :refer [pagination]]
   [social-wallet.swapi :as swapi]
   [social-wallet.webpage :refer [render-error]]
   [clj-time.format :as ft]
   [taoensso.timbre :as log]
   [failjure.core :as f]))

(def formatter (ft/formatter "dd MMMM, yyyy"))

(defn transactions [account tag swapi-params query-params uri]
  (f/attempt-all [response (swapi/list-transactions swapi-params (cond-> {}
                                                                   query-params (merge query-params)
                                                                   account (assoc :account (:email account))))
                  transactions (:transactions response)
                  total (:total-count response)
                  tags (into [] (set (filter #(> (count %) 0)
                                             (reduce into []
                                                     (map #(:tags %) (:transactions response))))))]

                 [:div
                  (if (not (= nil tag))
                    [:h1 (str "Transactions for " tag)]
                    [:div.filter-nav
                     (for [t tags]
                       [:a {:href (str "/transactions/tag/" t)} [:label.chip t]])])

                  [:table.func--transactions-page--table.table.table-striped
                   [:thead
                    [:tr
        ;; TODO: from transation
                     [:th "From"]
                     [:th "To"]
                     [:th "Amount"]
                     [:th "Time"]
                     [:th "Description"]
                     [:th "Tags"]]]
                   [:tbody
                    (doall (for [t transactions]
                             [:tr.filter-item
                              [:td (:from-id t)]
                              [:td (:to-id t)]
                              [:td (:amount-text t)]
                              [:td.minwidth (ft/unparse formatter (ft/parse (:timestamp t)))]
                              [:td (:description t)]
                              [:td (for [tag (:tags t)] (if (> (count tag) 0) [:div.chip tag] [:div]))]]))]]
                  (when (integer? total)
                    (pagination total (or (:page query-params) 1) uri)
                    )]
                 (f/when-failed [response]
                                (render-error response))))
