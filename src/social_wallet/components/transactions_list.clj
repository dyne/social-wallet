(ns social-wallet.components.transactions_list
  (:require
   [social-wallet.components.pagination :refer [pagination]]
   [social-wallet.swapi :as swapi]
   [social-wallet.webpage :refer [render-error]]
   [clj-time.format :as ft]
   [clavatar.core :as clavatar]
   [social-wallet.stores :as stores]
   [taoensso.timbre :as log]
   [just-auth.db.account :as auth]
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

                 [:div.transactions
                  [:h3 "Transactions list"]
                  [:div.filter-nav
                   [:a {:href (str uri)} [:label.chip "All"]]
                   (for [t tags]
                     [:a {:href (str uri "?tag=" t)} [:label.chip t]])]
                  [:div.feeds (doall (for [t transactions]

                                       [:div [:div.tile.feed
                                              [:div.tile-icon
                                               [:figure.avatar.avatar-lg
                                                [:img {:src (clavatar/gravatar (:from-id t) :size 87 :default :mm)}]]]
                                              [:div.tile-content
                                               [:p.tile-title [:b (:name (auth/fetch (:account-store stores/stores)  (:from-id t)))] " sent " [:b (:amount-text t)] " " (:currency t) " to " [:b (:name (auth/fetch (:account-store stores/stores)  (:to-id t)))]]
                                               [:p.tile-subtitle (:description t)]
                                               [:div.clearfix
                                                [:p.text-gray.float-left (ft/unparse formatter (ft/parse (:timestamp t)))]
                                                [:div.float-left (for [tag (:tags t)] (if (> (count tag) 0) [:div.chip tag] [:div]))]]]]
                                        [:div.divider]]))]
                  (when (and (integer? total) (empty? (:tags query-params)))
                    (pagination total (or (:page query-params) 1) uri))]
                 (f/when-failed [response]
                                (render-error response))))
