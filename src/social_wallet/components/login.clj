(ns social-wallet.components.login)

(defonce login-form
  [:div.container.grid-xl.login-card
   [:div.columns
    [:div.column.col-6.col-md-12
     [:div.card
      [:div.card-header
       [:div.card-title.h5.card-login "Login"]
       [:div.card-subtitle.text-gray "Welcome back to your social wallet"]]
      [:div.card-body
       [:form {:action "/login"
               :method "post"}
        [:div.form-group
         [:label.form-label {:for "username"} "Email"]
         [:input.form-input.form-control {:type "text" :id "username" :name "username"
                                          :placeholder "Type your email"}]]
        [:div.form-group
         [:label.form-label {:for "password"} "Password"]
         [:input.form-input.form-control {:type "password" :name "password"
                                          :id "password"
                                          :placeholder "Type your password"}]]
        [:input {:type "submit" :name "login-submit" :value "Login"
                 :class "btn btn-primary btn-lg btn-block"
                 :style "margin-top: 1em"}]]]]]
    [:div.column.col-6.col-md-12
     [:div.card.info-login
      [:div.card-image
       [:img.img-responsive {:src "./static/img/freecoin_logo.png"}]]
      [:div.card-header
       [:div.card-title.h5 "Your social wallet"]
       [:div.card-subtitle.text-gray "Freedom to create and manage your complementary currency"]]
      [:div.card-body
       "The social wallet toolkit is a set of tools to let groups design and/or manage currencies in a simple and secure way.
It is made for participatory and democratic organisations who want to incentivise participation - unlike centralised banking databases and experiment with different economic models - unlike centralised nation state."]]]]])