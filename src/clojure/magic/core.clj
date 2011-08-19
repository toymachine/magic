(ns magic.core
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use ring.util.response)
  (:use ring.middleware.reload)
  (:use ring.middleware.cookies)
  (:use ring.middleware.stacktrace)
  (:use ring.middleware.session)
  (:use ring.middleware.params)
  (:use ring.middleware.session.cookie)
  (:use magic.util)
  (:require [magic.session :as session])
  (:require [clojure.string :as string])
  (:require [magic.login :as login])
  (:require [magic.member :as member])
  (:require [compojure.route :as route])
  (:require [appengine-magic.core :as ae]))

;TODO put in external file (key store?)
(def SESSION_COOKIE_SECRET "pi@tbrakj00pbr$k")

(defn index-page [req]
  (html
    [:h2 "Hello World body3 Нидерланды!"]
    [:div "request"
     [:pre (str req)]]
		[:div "cookies" (req :cookies)]
  	[:div "session" (req :session)]
    [:div "ae-session" (req :ae-session)]
    [:div "params" (req :params)]))      
   
(defn test-page [req]
  (let [x '(+ 1 2)
        resp
        (-> 
          (response (html
                      [:h2 "Test"]
                      [:div "request"
                       [:pre (str-map req)]]
                      [:pre "cookies: " (str-map (req :cookies))]
                      [:pre "session: " (str-map (req :session))]
                      [:pre "ae-session: " (str-map (req :ae-session))]
                      [:pre "params: " (str-map (req :params))]
                      [:pre "base-url: " (base-url req)]
                      [:pre "logged in member: " (member/get-logged-in)]
                      [:pre "logged in member name: " (member/full-name (member/get-logged-in))]
                      [:pre "evalled: " (eval x)]
                      ))
          (content-type "text/html"))]
    resp))

(defn skeleton-page []
  (html5 	
    [:head
     [:title "Chiller App!"]
     [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
     (include-css "/static/css/reset.css" "/static/css/main.css")]
    [:body
     [:h1 "Hello World!"]
     (when (member/is-logged-in)
       [:h2 "Hello loggedin, your name = " (member/full-name (member/get-logged-in))])
     [:div {:id "page-body"}]
     (include-js "/static/js/jquery-1.6.2.min.js" "/static/js/main.js")
     ]))

(defroutes main-routes
  (GET "/" [] (skeleton-page))
  (GET "/index" [:as r] (index-page r))
  (GET "/test" [:as r] (test-page r))
  (GET "/auth-login" [] (login/login-page))
  (GET "/auth-openid" [:as r] (login/auth-openid r))
  (POST "/auth-openid" [:as r] (login/auth-openid r))
  (POST "/request-openid" [:as r] (login/request-openid r))
  (route/files "/static" {:root "static"})
  (route/not-found "Page not found"))

;ring app (wrappers execute from outer (wrap-stacktrace) to inner (wrap-logged-in-member)
(def app (-> main-routes
           (member/wrap-logged-in-member)
           (session/wrap-stateful-sessions)
           (wrap-session {:store (cookie-store {:key SESSION_COOKIE_SECRET}) :cookie-name "RS"})
           (session/wrap-app-engine-session {:session-key :ae-session})
           (wrap-params)
           (wrap-reload '(magic.core magic.login magic.member magic.session))
           (wrap-stacktrace)))

(ae/def-appengine-app magic-app (var app))

