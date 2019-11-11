(ns social-wallet.components.reset)

(defn reset-form
  [email token reset-uri]
  [:div.card.container.grid-sm.login-card
   [:div.card-body
    [:form {:action (str "/reset-password/" email "/" token)
            :method "post"}
     [:div.form-group
      [:label.form-label {:for "password"} "New password"]
      [:input.form-input.form-control {:type "password" :id "password" :name "password"
                                       :placeholder "Type your password"}]]

     [:div.form-group
      [:label.form-label {:for "repeat"} "Confirm your password"]
      [:input.form-input.form-control {:type "password" :id "repeat" :name "repeat"
                                       :placeholder "Confirm your password"}]]

     [:input {:type "submit" :name "sign-up-submit" :value "Reset your password"
              :class "btn btn-primary btn-lg btn-block"
              :style "margin-top: 1em"}]]]])