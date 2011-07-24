(ns magic.core
  (:use compojure.core)
  (:use magic.session)
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use ring.util.response)
  (:use ring.middleware.reload)
  (:use ring.middleware.cookies)
  (:use ring.middleware.stacktrace)
  (:use ring.middleware.session)
  (:use ring.middleware.session.cookie)
  (:require [compojure.route :as route])
  (:require [appengine-magic.core :as ae])
  (:import [org.openid4java.consumer ConsumerManager])
  (:import [com.google.inject Guice])
  (:import [org.openid4java.appengine AppEngineGuiceModule]))

(def SESSION_COOKIE_SECRET "pi@tbrakj00pbr$k")

(defn index-page [req]
  (html
    [:h2 "Hello World body3 Нидерланды!" "cookies" (req :cookies)]))      
   
(defn test-page [req]
  (let [resp
        (-> 
          (response "hello world!")
          (content-type "text/html"))]
    (println "session in" (req :session))
    resp))

;    (let [resp2 (assoc-in resp [:session "aap"] "piet")]
;      (println "session out" resp2)
;      resp2)))

(defn login-page []
  (html
    [:span "openid:"]
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

(defn skeleton-page []
  (html5 	
    [:head
     [:title "Chiller App!"]
     [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
     (include-css "/static/css/reset.css" "/static/css/main.css")]
    [:body
     [:h1 "Hello World!"]
     [:div {:id "page-body"}]
     (include-js "/static/js/jquery-1.6.2.min.js" "/static/js/main.js")
     ]))
  
(defroutes main-routes
  (GET "/" [] (skeleton-page))
  (GET "/login" [] (login-page))
  (GET "/test" [:as r] (test-page r))
  (GET "/auth-openid" [:as r] (auth-openid r))
  (POST "/auth-openid" [:as r] (auth-openid-post r))
  (POST "/request-openid" [:as r] (request-openid r))
  (GET "/index" [:as r] (index-page r))
  (route/files "/static" {:root "static"})
  (route/not-found "Page not found"))


;ring app
(def app (-> main-routes
           (wrap-reload '(magic.core))
           (wrap-stacktrace)
           (wrap-cookies)
           (wrap-session {:store (cookie-store {:key SESSION_COOKIE_SECRET})
                          :cookie-name "RS"})))

(ae/def-appengine-app magic-app (var app))

