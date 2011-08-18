(ns magic.openid
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use ring.util.response)
  (:use magic.util)
  (:require [magic.login :as login])
  (:require [magic.session :as session])
  (:require [crypto.random :as cr])
  (:import [org.openid4java.consumer ConsumerManager])
  (:import [com.google.inject Guice])
  (:import [org.openid4java.appengine AppEngineGuiceModule AppEngineNonceVerifier])
  (:import [org.openid4java.message ParameterList]))

(def *consumer-manager*
  (let [injector (Guice/createInjector [(new AppEngineGuiceModule)])]
    (doto (.getInstance injector ConsumerManager)
      (.setNonceVerifier (new AppEngineNonceVerifier 60)))))

(defn auth-openid [req]
  (let [cm *consumer-manager*
        http-request (req :request)
        parameter-map (new ParameterList (.getParameterMap http-request))
        auth-req-id (get-in req [:params "auth-req-id"])
        discovered (session/get-value auth-req-id)
        query-string (str (.getQueryString http-request))
        receiving-url (str (.getRequestURL http-request) (when-not (empty? query-string) (str "?" query-string)))
        verification (.verify cm receiving-url parameter-map discovered)
        verified-id (str (.getVerifiedId verification))]
    (session/remove-value! auth-req-id)
    (login/set-logged-in-member-by-identifier! verified-id)
    (-> (response (html
                    [:h2 "auth-openid"]
                    [:h3 "rec url" receiving-url]
                    [:h3 "verified-id: " verified-id]
                    [:pre "params" (str-map (req :params))]))
      (content-type "text/html"))
    ))

(defn request-openid [req]
  "Perform OpenID process and build redirect that goes to Google for authentication and requests email address in return"
  (let [cm *consumer-manager*
        auth-req-id (cr/hex 16) ;identify this oauth cycle, so that we can put some stuff in session for when op returns
        realm (base-url req) ; Make sure this stays stable, otherwise google id will changes and all identities are lost
        return-url (str (base-url req) "/auth-openid?auth-req-id=" auth-req-id)
        user-supplied-string "https://www.google.com/accounts/o8/id"
        discoveries (.discover cm user-supplied-string)
        discovered (.associate cm discoveries)
        auth-req (.authenticate cm discovered return-url realm)]
    (session/put-value! auth-req-id discovered)
    (-> 
      (redirect (.getDestinationUrl auth-req true))
      ;we will need discovered later when op returns to /auth-openid        
      (update-in [:headers "Location"] str
                 "&openid.ns.ax=http://openid.net/srv/ax/1.0"
                 "&openid.ax.mode=fetch_request"
                 "&openid.ax.required=email"
                 "&openid.ax.type.email=http://axschema.org/contact/email"))))
  