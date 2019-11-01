(ns social-wallet.components.sendTo
  (:require [just-auth.core :as auth]
            [social-wallet.authenticator :refer [authenticator]]))


(defn mini-render-sendTo
  []
  [:div.card.container.grid.sm.homepage-card
   
   [:div.card-body
    [:form {:action "/sendto"
            :method "post"}

     [:div.form-group
      [:input.form-input.form-control {:id "amount" :name "amount"
                                       :placeholder "Insert the amount to transfer"}]]

     [:div.form-group
      [:select.form-select.form-control {:id "to" :name "to"}
       (let [participants (auth/list-accounts authenticator {:activated true})]
         (doall (for [p participants]
                  [:option {:value (:email p)} (:name p)])))
       ]]


     [:div.form-group
      [:input.form-input.form-control {:type "text" :id "tags" :name "tags"
                                       :placeholder "Add some tags (comma separated)"}]]

     [:div.form-group
      [:textarea.form-input.form-control {:type "text" :id "description" :name "description"
                                          :placeholder "Add a message"
                                          :rows "3"}]]

     [:input {:type "submit" :name "sendto-submit" :value "Send"
              :class "btn btn-primary btn-lg btn-block"
              :style "margin-top: 1em"}]]]]
  )

(defn render-sendTo
  ([]
   [:div.card.container.grid.sm.login-card
    [:div.card-header
     [:div.card-title.h5 "Send tokens"]]
    [:div.card-body
     [:form {:action "/sendto"
             :method "post"}

      [:div.form-group
       [:label.form-label {:for "amount"} "Amount"]
       [:input.form-input.form-control {:type "text" :id "amount" :name "amount"
                                        :placeholder "Insert the amount to transfer"}]]

      
      [:div.form-group
       [:label.form-label {:for "to"} "To"]
       [:select.form-select.form-control {:id "to" :name "to"}
        (let [participants (auth/list-accounts authenticator {:activated true})]
          (doall (for [p participants]
                   [:option {:value (:email p)} (:name p)])))]]


      [:div.form-group
       [:label.form-label {:for "tags"} "Tags"]
       [:input.form-input.form-control {:type "text" :id "tags" :name "tags"
                                        :placeholder "Add some tags (comma separated)"}]]

      [:div.form-group
       [:label.form-label {:for "description"} "Message"]
       [:textarea.form-input.form-control {:type "text" :id "description" :name "description"
                                           :placeholder "Add a message"
                                           :rows "3"}]]

      [:input {:type "submit" :name "sendto-submit" :value "Send"
               :class "btn btn-primary btn-lg btn-block"
               :style "margin-top: 1em"}]]]])
  ([email]
   [:div.card.container.grid.sm.login-card
    [:div.card-header
     [:div.card-title.h5 (str "Send tokens to " email)]]
    [:div.card-body
     [:form {:action "/sendto"
             :method "post"}

      [:div.form-group
       [:label.form-label {:for "amount"} "Amount"]
       [:input.form-input.form-control {:type "text" :id "amount" :name "amount"
                                        :placeholder "Insert the amount to transfer"}]]

      [:div.form-group
       [:label.form-label {:for "to"} "To"]

       [:select.form-select.form-control {:id "to" :name "to"}
        (let [participants (auth/list-accounts authenticator {:activated true})]
          (doall (for [p participants]
                   [:option {:value (:email p) :selected (if (= email (:email p)) "selected" nil)} (:name p)])))]]


      [:div.form-group
       [:label.form-label {:for "tags"} "Tags"]
       [:input.form-input.form-control {:type "text" :id "tags" :name "tags"
                                        :placeholder "Add some tags (comma separated)"}]]

      [:div.form-group
       [:label.form-label {:for "description"} "Message"]
       [:textarea.form-input.form-control {:type "text" :id "description" :name "description"
                                           :placeholder "Add a message"
                                           :rows "3"}]]

      [:input {:type "submit" :name "sendto-submit" :value "Send"
               :class "btn btn-primary btn-lg btn-block"
               :style "margin-top: 1em"}]]]]))

