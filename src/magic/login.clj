(ns magic.login
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use ring.util.response)
  (:use magic.util)
  (:import [org.openid4java.consumer ConsumerManager])
  (:import [com.google.inject Guice])
  (:import [org.openid4java.appengine AppEngineGuiceModule AppEngineNonceVerifier])
  (:import [org.openid4java.message ParameterList]))

(def *consumer-manager*
  (let [injector (Guice/createInjector [(new AppEngineGuiceModule)])
        cm (.getInstance injector ConsumerManager)]
    (.setNonceVerifier cm (new AppEngineNonceVerifier 60))
    cm))
    
(defn login-page []
  (html
    [:span "openid login:"]
    [:form {:method "POST" :action "/request-openid"}
      "Login"
      [:input {:type "text"}]
      [:input {:type "submit"}]]))

(defn auth-openid [req]
  (let [cm *consumer-manager*
        http-request (req :request)
        parameter-map (new ParameterList (.getParameterMap http-request))
        ae-session (req :ae-session)
        discovered (ae-session "openid-disc")
        query-string (str (.getQueryString http-request))
        receiving-url (str (.getRequestURL http-request) (when-not (empty? query-string) (str "?" query-string)))
        verification (.verify cm receiving-url parameter-map discovered)
        verified (.getVerifiedId verification)]
    (-> (response (html
                    [:h2 "auth-openid"]
                    [:h3 "rec url" receiving-url]
                    [:h3 "verified: " verified]
                    [:pre "params" (str-map (req :params))]))
      (content-type "text/html")
      (assoc :ae-session (dissoc ae-session "openid-disc"))
      )))
  
(defn request-openid [req]
  "Perform OpenID process and build redirect that goes to Google for authentication and requests email address in return"
  (let [cm *consumer-manager*
        return-url (str (base-url req) "/auth-openid")
        user-supplied-string "https://www.google.com/accounts/o8/id"
        discoveries (.discover cm user-supplied-string)
        discovered (.associate cm discoveries)
        auth-req (.authenticate cm discovered return-url)]
    (-> 
      (redirect (.getDestinationUrl auth-req true))
      (assoc-in [:ae-session "openid-disc"] discovered)
      (update-in [:headers "Location"] str
                 "&openid.ns.ax=http://openid.net/srv/ax/1.0"
                 "&openid.ax.mode=fetch_request"
                 "&openid.ax.required=email"
                 "&openid.ax.type.email=http://axschema.org/contact/email"))))
