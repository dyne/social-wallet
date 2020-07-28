(ns social-wallet.components.login
  (:require  [failjure.core :as f]
             [just-auth.core :as auth]
             [social-wallet.authenticator :refer [authenticator]]
             [social-wallet.webpage :refer [render-error]]
             [clavatar.core :as clavatar]
             [social-wallet.swapi :as swapi]
             [social-wallet.swapi :as swapi]))

(defn login-form [swapi-params]
  [:div.login-card
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
      ]]
    [:div.column.col-6.col-md-12
     [:div.card.info-login
      [:div.card-image
       [:img.img-responsive {:src "http://xl.repubblica.it/wp-content/gallery/alberico/4.jpg"}]]
      [:div.card-header
       [:div.card-title.h5.login-title (f/if-let-ok? [currency (swapi/label-request swapi-params)]
                                                     (str (:currency currency) " wallet")
                                                     (render-error currency))]
       [:div.card-subtitle.text-gray "Freedom to create and manage your complementary currency"]]
      [:div.card-body
       "The social wallet toolkit is a set of tools to let groups design and/or manage currencies in a simple and secure way.
It is made for participatory and democratic organisations who want to incentivise participation - unlike centralised banking databases and experiment with different economic models - unlike centralised nation state."]
      [:div.admins_list
       [:h6 "Administrated by:"]
       (let [admins (auth/list-accounts authenticator {:flags "admin"})]
         (doall (for [p admins]
                  [:div.list_item
                   [:div.tile-icon
                    [:figure.avatar.avatar-lg
                     [:img {:src (clavatar/gravatar (:email p) :size 87 :default :mm)}]]]
                   [:div.admin_info
                    [:h5.title-name
                     (:name p)]
                    [:h4.title-mail
                     (:email p)]]])))]
      [:div.admins_list
       [:h6.stats_title "statistics"]
       (let [users (auth/list-accounts authenticator {})
             txs (swapi/list-transactions swapi-params {})]
         [:div.flex
          [:div.stat
           [:h3 (count users)]
           [:span "users"]]
          [:div.stat
           [:h3 (:total-count txs)]
           [:span "users"]]])]]]]])
          