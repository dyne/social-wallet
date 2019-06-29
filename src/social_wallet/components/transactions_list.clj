(ns social-wallet.components.transactions_list
  (:require
   [social-wallet.components.pagination :refer [pagination]]
   [social-wallet.swapi :as swapi])
  )

(defn transactions [account swapi-params query-params uri]
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
       (pagination total (or (:page query-params) 1) uri))]))