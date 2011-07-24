(ns magic.login
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use ring.util.response)
  (:import [org.openid4java.consumer ConsumerManager])
  (:import [com.google.inject Guice])
  (:import [org.openid4java.appengine AppEngineGuiceModule]))

(defn login-page []
  (html
    [:span "openid login:"]
    [:form {:method "POST" :action "/request-openid"}
      "Login"
      [:input {:type "text"}]
      [:input {:type "submit"}]]))

(defn auth-openid [req]
  (println "openid get resp return")
  (println req))

(defn auth-openid-post [req]
  (println "openid post resp return")
  (println req))

(defn request-openid [req]
  "Perform OpenID process and build redirect that goes to Google for authentication and requests email address in return"
  (let [injector (Guice/createInjector [(new AppEngineGuiceModule)])
        cm (.getInstance injector ConsumerManager)
        return-url "http://localhost:8080/auth-openid"
        user-supplied-string "https://www.google.com/accounts/o8/id"
        discoveries (.discover cm user-supplied-string)
        discovered (.associate cm discoveries)
        auth-req (.authenticate cm discovered return-url)
        redirect-response (redirect (.getDestinationUrl auth-req true))]
    (update-in redirect-response [:headers "Location"] str
               "&openid.ns.ax=http://openid.net/srv/ax/1.0"
               "&openid.ax.mode=fetch_request"
               "&openid.ax.required=email"
               "&openid.ax.type.email=http://axschema.org/contact/email")))
