(ns social-wallet.components.login)

(defonce login-form
  [:div.card.container.grid-sm.login-card
   [:div.card-header
    [:div.card-title.h5 "Login"]
    [:div.card-subtitle.text-gray "Welcome back to your social wallet"]]
   [:div.card-body
    [:form {:action "/login"
            :method "post"}
     [:div.form-group
      [:label.form-label {:for "username"} "Username"]
      [:input.form-input.form-control {:type "text" :id "username" :name "username"
                                       :placeholder "Type your username"}
                                       ]]
     [:div.form-group
      [:label.form-label {:for "password"} "Password"]
      [:input.form-input.form-control {:type "password" :name "password"
                                       :id "password"
                                       :placeholder "Type your password"}
                                       ]]
     [:input {:type "submit" :name "login-submit" :value "Login"
              :class "btn btn-primary btn-lg btn-block"
              :style "margin-top: 1em"}]]]]
  
  )