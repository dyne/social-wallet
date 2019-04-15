;; social-wallet - A generic social wallet UI which uses the social-wallet-api for a beckend

;; Copyright (C) 2019- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti <aspra@dyne.org>

;; This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify this Program, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns social-wallet.authenticator
  (:require [taoensso.timbre :as log]

            [social-wallet.config :refer [config]]
            [social-wallet.stores :refer [stores]]
            [social-wallet.util :refer [exception->failjure]]
            [just-auth.core :as auth]

            social-wallet.spec
            
            [yummy.config :as yc]
            [mount.core :as mount :refer [defstate]]))

(defn create-authenticator [{:keys [auth-admin stub-email]}]
  (log/info "Creating authenticator...")
  (let [config-path (-> config :just-auth :email-config)
        email-config (yc/load-config {:path config-path
                                  :spec (if auth-admin ::email-conf-admin ::email-conf)
                                  :die-fn exception->failjure})]
    (if stub-email
      (auth/new-stub-email-based-authentication
       stores (atom []) {} (-> config :just-auth :throttling))
      (auth/email-based-authentication
       stores email-config (-> config :just-auth :throttling)))))

(defstate authenticator :start (create-authenticator (mount/args)))
