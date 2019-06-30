(ns social-wallet.components.header
  (:require
   [just-auth.core :as auth]
   [social-wallet.authenticator :refer [authenticator]]
   [auxiliary.translation :as t]))


(def header-guest
  [:header.navbar.header
   [:div.navbar-section
    [:a.bold.navbar-brand.mr-2 {:href "/"} "social wallet"]]
   [:div.navbar-section
    [:a {:class "btn"
         :href "/login"} " Login"]
    [:a {:class "btn btn-primary"
         :href "/signup"} " Sign-up"]]])


(defn header-account [account]
  [:header.navbar.header
   [:div.navbar-section
    [:a.bold.navbar-brand.mr-2 {:href "/"} "social wallet"]]
   [:div.navbar-section
    [:a.btn.btn-link {:href "/sendto"}
     (t/locale [:wallet :send])]
    [:a.btn.btn-link {:href "/transactions"}
     (t/locale [:navbar :transactions])]
   [:a.btn.btn-link {:href "/participants"}
    (t/locale [:navbar :participants])]
   [:a.btn.btn-link {:href "/tags"}
    (t/locale [:navbar :tags])]
   [:div.dropdown
    [:a.btn.btn-link.dropdown-toggle {:href "#" :tabIndex "0"}
     [:i.icon.icon-menu]]
    [:ul.menu
     
     (if
      (some #{:admin} (:flags (auth/get-account authenticator (:email account))))
       [:div
        [:li.divider {:data-content "ADMIN"}]
        [:li.menu-item
         [:a {:href "/admin-panel"} (t/locale [:navbar :admin])]]]
       [:div])
     [:li.divider {:data-content "GENERAL"}]
     [:li.menu-item
      [:a {:href "/app-state"} (t/locale [:navbar :conf])]]
     [:li.menu-item
      [:a {:href "/logout"} (t/locale [:navbar :log-out])]]
     ]
    ]]])
   
   