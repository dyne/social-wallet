(ns social-wallet.components.transactions_list
  (:require
   [social-wallet.components.pagination :refer [pagination]]
   [social-wallet.swapi :as swapi]
   [failjure.core :as f]))



(defn transactions [account tag swapi-params query-params uri]
  (let [response (swapi/list-transactions swapi-params (cond-> {}
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
                 {:data-tag (str "tag-" (inc (.indexOf tags (first (:tags t)))))}
                 [:td (:from-id t)]
                 [:td (:to-id t)]
                 [:td (:amount-text t)]
                 [:td (:timestamp t)]
                 [:td (:description t)]
                 [:td (for [tag (:tags t)] (if (> (count tag) 0) [:div.chip tag] [:div]))]]))]]
     (when (= uri "/transactions")
       (pagination total (or (:page query-params) 1) uri))
]
; (f/when-failed [e]
;                (f/message e))
    
    ))
