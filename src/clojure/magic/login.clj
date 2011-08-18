(ns magic.login
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use ring.util.response)
  (:use magic.util)
  (:require [magic.member :as member])  
  (:require [magic.cookie :as cookie])
  (:require [crypto.random :as cr])
  (:import [org.openid4java.consumer ConsumerManager])
  (:import [com.google.inject Guice])
  (:import [org.openid4java.appengine AppEngineGuiceModule AppEngineNonceVerifier])
  (:import [org.openid4java.message ParameterList]))

(defn login-page []
  (html
    [:span "openid login:"]
    [:form {:method "POST" :action "/request-openid"}
     "Login"
     [:input {:type "text"}]
     [:input {:type "submit"}]]))

(def *logged-in-member* nil)

(defn set-logged-in-member-by-identifier! [identifier]
  (if-let [member (member/find-by-identifier identifier)]
    (cookie/put-value! :lm (member/get-id member))))

(defn get-logged-in-member []
  (if-let [logged-in-member-id (cookie/get-value :lm)]
    (member/retrieve-by-id logged-in-member-id)))

(defn get-logged-in []
  *logged-in-member*)

(defn is-logged-in []
  (not= *logged-in-member* nil))

(defn wrap-logged-in-member [app]
  (fn [req]
    (if-let [logged-in-member (get-logged-in-member)]			    
      ;bind logged in member for this request		
      (binding [*logged-in-member* logged-in-member]
        (app req))	
      ;no logged in member:
      (app req))))

