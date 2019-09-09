(ns social-wallet.components.login)

(defonce login-form
  [:div.container.grid-xl.login-card
   [:div.columns

    [:div.column.col-6.col-md-12
     [:div.card.info-login
      [:div.card-image
       [:img.img-responsive {:src "./static/img/commonfare.png"}]]
      [:div.card-header
       [:div.card-title.h5.login-title "Your social wallet"]
       [:div.card-subtitle.text-gray "Freedom to create and manage your complementary currency"]]
      [:div.card-body
       "Commonfare.net is developed thanks to the project PIE News, that has received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement No 687922"]]]

    [:div.column.col-6.col-md-12
     [:div.card
      [:div.card-header
       [:div.card-title.h5.card-login "Login"]
       [:div.card-subtitle.text-gray "Welcome back to your social wallet"]]
      [:div.card-body
       [:form {:action "/login"
               :method "post"}
        [:div.form-group
         [:label.form-label {:for "email"} "Email"]
         [:input.form-input.form-control {:type "text" :id "email" :name "email"
                                          :placeholder "Type your email"}]]
        [:div.form-group
         [:label.form-label {:for "password"} "Password"]
         [:input.form-input.form-control {:type "password" :name "password"
                                          :id "password"
                                          :placeholder "Type your password"}]]
        [:input {:type "submit" :name "login-submit" :value "Login"
                 :class "btn btn-primary btn-lg btn-block"
                 :style "margin-top: 1em"}]]]
;       [:div.card-footer
;        [:div.divider.text-center {:data-content "OR"}]
;        [:a.btn.btn-link.p-centered {:href "/recover-password"} "Ask a new password"]]
      
      ]]]])