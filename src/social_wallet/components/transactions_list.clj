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
        total (:total-count response)
        tags (into [] (set (filter #(> (count %) 0)
                                   (reduce into []
                                           (map #(:tags %) (:transactions response))))))]
    [:div.filter
     [:input.filter-tag {:hidden true :checked true :name "filter-radio" :type "radio" :id "tag-0"}]
     (for [t tags]
       [:input.filter-tag {:hidden true :name "filter-radio" :type "radio" :id (str "tag-" (inc (.indexOf tags t)))}])

     [:div.filter-nav
      [:label.chip {:for "tag-0"} "All"]
      (for [t tags]
        [:label.chip {:for (str "tag-" (inc (.indexOf tags t)))} t])]

     [:div.filter-body
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
                  [:td (for [tag (:tags t)] (if (> (count tag) 0) [:div.chip tag] [:div]))]]))]]]
     (when (= uri "/transactions")
       (pagination total (or (:page query-params) 1) uri))]))