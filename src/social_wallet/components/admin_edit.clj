(ns social-wallet.components.admin_edit
  (:require [clavatar.core :as clavatar]
            [just-auth.core :as auth]
            [social-wallet.authenticator :refer [authenticator]]
            ))


;  USE CONDITIONAL TO RENDER PROPERLY THE FORM BASED ON USER DATA
(defn render-edit-account-panel [email]
  (let [user (auth/get-account authenticator email)]
    [:div.panel.container.grid-sm
     [:div.panel-header.text-center
      [:div.gravatar
       [:img {:src (clavatar/gravatar email :size 87 :default :mm)}]]
      [:div.panel-title.h5.mt-10 (:name user)]
      [:div.panel-subtitle.text-gray (:email user)]
      ]
     [:div.panel-body
      [:form.form-horizontal {:action (str "/admin-panel/edit/" email)
                              :method "post"}

       (if (= true (:activated user))
         [:div.form-group
           [:label.form-switch
            [:input {:id "active" :name "active" :type "checkbox"}]
            [:i.form-icon] (str "Do you want to deactivate " (:name user) " ?")]]
         [:div.form-group
          [:label.form-switch
           [:input {:id "active" :name "active" :type "checkbox"}]
           [:i.form-icon] (str "Do you want to activate " (:name user) " ?")]])
       
       [:div.form-group
        [:label.form-switch
         [:input {:id "admin" :name "admin" :type "checkbox"}]
         [:i.form-icon] "Admin"]]

        [:input {:type "submit" :name "sendto-submit" :value "Send"
                 :class "btn btn-primary btn-lg btn-block"
                 :style "margin-top: 1em"}]]]]))