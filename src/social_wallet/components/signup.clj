(ns social-wallet.components.signup)

(defonce signup-form
  [:div.card.container.grid-sm.login-card
   [:div.card-header
    [:div.card-title.h5 "Create a new account"]]
   [:div.card-body
    [:form {:action "/signup"
            :method "post"}


     [:div.form-group
      [:label.form-label {:for "name"} "Name"]
      [:input.form-input.form-control {:type "text" :id "name" :name "name"
                                       :placeholder "Type your name"}]]

     [:div.form-group
      [:label.form-label {:for "email"} "Email"]
      [:input.form-input.form-control {:type "text" :id "email" :name "email"
                                       :placeholder "Type your email"}]]


     [:div.form-group
      [:label.form-label {:for "password"} "Password"]
      [:input.form-input.form-control {:type "password" :name "password"
                                       :id "password"
                                       :placeholder "Type your password"}]]

     [:div.form-group
      [:label.form-label {:for "repeat-password"} "Repeat password"]
      [:input.form-input.form-control {:type "password" :name "repeat-password"
                                       :id "repeat-password"
                                       :placeholder "Type your password again"}]]
     

     [:input {:type "submit" :name "sign-up-submit" :value "Sign Up"
              :class "btn btn-primary btn-lg btn-block"
              :style "margin-top: 1em"}]]]])