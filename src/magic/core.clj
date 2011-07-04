(ns magic.core
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:require [compojure.route :as route])
  (:require [appengine-magic.core :as ae]))

(defn index-page []
  (html
    [:h2 "Hello World body!"]))

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
  (GET "/index" [] (index-page))
  (route/files "/static" {:root "static"})
  (route/not-found "Page not found"))

(ae/def-appengine-app magic-app #'main-routes)

