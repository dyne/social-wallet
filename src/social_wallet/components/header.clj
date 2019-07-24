(ns social-wallet.components.header
  (:require [auxiliary.translation :as t]))


(def header-guest
  [:header.navbar.header
   [:div.navbar-section
    [:a.bold.navbar-brand.mr-2 {:href "/"} "social wallet"]]
   
   [:div.navbar-section
    [:a {:class "btn"
         :href "/login"} " Login"]
    [:a {:class "btn btn-primary"
         :href "/signup"} " Sign-up"]]
   
   ])


(defn header-account [account]
  [:header.navbar.header
   [:div.navbar-section
    [:div.show-md
     [:div.dropdown
      [:a.btn.btn-link.dropdown-toggle {:href "#" :tabIndex "0"}
       [:i.icon.icon-menu]]
      [:ul.menu
       [:li.menu-item
        [:a {:href "/sendto"} (t/locale [:wallet :send])]]
       [:li.menu-item
        [:a {:href "/transactions"}
         (t/locale [:navbar :transactions])]]
       [:li.menu-item
        [:a {:href "/participants"}
         (t/locale [:navbar :participants])]]
       [:li.menu-item
        [:a {:href "/tags"}
         (t/locale [:navbar :tags])]]
       [:li.menu-item
        [:a {:href "/qrcode"}
         (t/locale [:navbar :qrcode])]]
       [:li.menu-item
        [:a {:href "/app-state"} (t/locale [:navbar :conf])]]
       [:li.menu-item
        [:a {:href "/logout"} (t/locale [:navbar :log-out])]]]]]
    [:a.bold.navbar-brand.mr-2 {:href "/"} "social wallet"]]
   [:div.navbar-section.hide-md
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
      [:i.icon.icon-menu]
      ]
     [:ul.menu
      [:li.menu-item
       [:a {:href "/qrcode"}
        (t/locale [:navbar :qrcode])]]
      [:li.menu-item
       [:a {:href "/app-state"} (t/locale [:navbar :conf])]]
      [:li.menu-item
       [:a {:href "/logout"} (t/locale [:navbar :log-out])]]
      ]
     ]]])
   
   
;    <div class= "dropdown" >
; <a href= "#" class= "btn btn-link dropdown-toggle" tabindex= "0" >
; dropdown menu <i class= "icon icon-caret" ></i>
; </a>
; <!-- menu component -->
; <ul class= "menu" >
; ...
; </ul>
; </div>

; <!-- dropdown button group -->
; <div class= "dropdown" >
; <div class= "btn-group" >
; <a href= "#" class= "btn" >
; dropdown button
; </a>
; <a href= "#" class= "btn dropdown-toggle" tabindex= "0" >
; <i class= "icon icon-caret" ></i>
; </a>

; <!-- menu component -->
; <ul class= "menu" >
; ...
; </ul>
; </div>
; </div>
   
   
;    [:div {:class "collapse navbar-collapse" :id "navbarResponsive"}
;     [:ul {:class "nav navbar-nav hidden-sm hidden-md ml-auto"}
;       ;; --
;      [:li {:class "divider" :role "separator"}]
;      [:li {:role "separator" :class "divider"}]
;      [:li {:class "nav-item"}
;       [:a {:class "nav-link far fa-file-code"
;            :href "/app-state"} (t/locale [:navbar :conf])]]
;      ; [:li {:class "nav-item"}
;      ;  [:a {:class "nav-link far fa-file-code"
;      ;       :href (str "/wallet/" (:email account))} (t/locale [:navbar :my-wallet])]]
;      ; [:li {:class "nav-item"}
;      ;  [:a {:class "nav-link far fa-file-code"
;      ;       :href "/sendto"} (t/locale [:wallet :send])]]
;      ; [:li {:class "nav-item"}
;      ;  [:a {:class "nav-link far fa-file-code"
;      ;       :href "/transactions"} (t/locale [:navbar :transactions])]]
;      ; [:li {:class "nav-item"}
;      ;  [:a {:class "nav-link far fa-file-code"
;      ;       :href "/participants"} (t/locale [:navbar :participants])]]
;      ; [:li {:class "nav-item"}
;      ;  [:a {:class "nav-link far fa-file-code"
;      ;       :href "/tags"} (t/locale [:navbar :tags])]]
;      [:li {:class "nav-item"}
;       [:a {:class "nav-link far fa-file-code"
;            :href "/logout"} (t/locale [:navbar :log-out])]]]]])