(ns social-wallet.components.recover)

(defonce recover-form
  [:div.card.container.grid-sm.login-card
   [:div.card-body
    [:form {:action "/recover-password"
            :method "post"}
     [:div.form-group
      [:label.form-label {:for "email"} "Your email"]
      [:input.form-input.form-control {:type "text" :id "email" :name "email"
                                       :placeholder "Type your email"}]]

     [:input {:type "submit" :name "sign-up-submit" :value "Reset your password"
              :class "btn btn-primary btn-lg btn-block"
              :style "margin-top: 1em"}]]]])