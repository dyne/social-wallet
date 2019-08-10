(ns social-wallet.pages.wallet
  (:require [hiccup.page :as page]
            [auxiliary.translation :as t]
            [clavatar.core :as clavatar]
            [just-auth.core :as auth]
            [social-wallet.swapi :as swapi]
            [social-wallet.authenticator :refer [authenticator]]
            [social-wallet.components.transactions_list :refer [transactions]]
            [social-wallet.components.footer :refer [footer]]
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
             [:div {:class "wallet-details"}
              [:div.wallet-card.clearfix
               [:div.card-info.panel-header.text-center
                [:figure.avatar.avatar-lg
                 [:img {:src (clavatar/gravatar email :size 87 :default :mm)}]]
                [:div.panel-title.h5.mt-10
                 (:name account)]
                [:div.panel-subtitle
                 [:i.icon.icon-mail] email]
                (if (some #{:admin} (:flags (auth/get-account authenticator email)))
                  [:div.chip "Admin"]
                  [:div])
                ]
               [:div.info.clearfix.text-center
                (f/if-let-ok? [balance (swapi/balance swapi-params
                                                      (select-keys account [:email]))]
                              [:div.chip.balance
                               [:span "👛"]
                               [:h2 (str (t/locale [:wallet :balance]) ": " balance)]]
                              (render-error balance))
                ; [:span {:class "qrcode"}
                ;  [:img {:src (hu/url  "/qrcode/" email)}]]
                ]]]

             [:div {:style "margin-top: 60px"}

              [:div.divider.text-left {:data-content "YOUR TRANSACTIONS"}]
              (transactions account nil swapi-params pagination uri)]
             (footer)])}))