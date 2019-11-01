(ns social-wallet.pages.wallet
  (:require [hiccup.page :as page]
            [auxiliary.translation :as t]
            [clavatar.core :as clavatar]
            [just-auth.core :as auth]
            [social-wallet.swapi :as swapi]
            [social-wallet.authenticator :refer [authenticator]]
            [social-wallet.components.transactions_list :refer [transactions]]
            [social-wallet.components.footer :refer [footer]]
            [social-wallet.components.sendTo :refer [mini-render-sendTo]]
            [social-wallet.webpage :refer [render-error]]
            [social-wallet.components.header :refer [header-account]]
            [social-wallet.components.head :refer [render-head]]
            [failjure.core :as f]))

(defn wallet-page [account swapi-params uri pagination]
  (let [email (:email account)]
    {:headers {"Content-Type"
               "text/html; charset=utf-8"}
     :body (page/html5
            (render-head)
            [:body.container.grid-lg
             (header-account account)
             [:div.columns
              [:div.column.col-4.col-md-12
               [:div {:class "wallet-details"}
                [:div.wallet-card.clearfix
                 [:div.card-info.panel-header
                  [:figure.avatar.avatar-lg
                   [:img {:src (clavatar/gravatar email :size 87 :default :mm)}]]
                  [:div.info
                   [:div.panel-title
                    (str "@" (:name account)) (if (some #{:admin} (:flags (auth/get-account authenticator email)))
                                                [:div.chip "Admin"])]
                   [:div.panel-subtitle
                    [:i.icon.icon-mail] email]
                   
                   
                   (f/if-let-ok? [balance (swapi/balance swapi-params
                                                         (select-keys account [:email]))]
                                 [:div.panel-subtitle.balance-subtitle
                                  [:span "👛"] (str balance " tokens")]
                                 (render-error balance))]]
                 [:div.hide-md
                  
                  (mini-render-sendTo)
                ; [:span {:class "qrcode"}
                ;  [:img {:src (hu/url  "/qrcode/" email)}]]
                  ]]]
               ]
              [:div.column.col-8.col-md-12
               (transactions account nil swapi-params pagination uri)
               ]
              ]
             (footer)])}))