(ns social-wallet.components.qrcode
  (:require [hiccup.util :as hu]))

(defn qrcode-page [email]
  [:section
   [:h2.text-center
    (str "Scan the qrcode to send tokens to " email)
    ]
   [:span {:class "qrcode"}
    [:img {:src (hu/url  "/qrcode/" email)}]]])
