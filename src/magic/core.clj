(ns magic.core
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use ring.util.response)
  (:require [compojure.route :as route])
  (:require [appengine-magic.core :as ae]))

(defn index-page [req]
  (do
    (println "req" (req :session))
    (->
      (response
        (html
          [:h2 "Hello World body Нидерланды!"]))
      (content-type "text/html")
      (assoc :session nil)
      )))
   
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
  (GET "/index" [:as r] (index-page r))
  (route/files "/static" {:root "static"})
  (route/not-found "Page not found"))

(defn wrap-ae-session [app]
  (fn [req]
    (let [http-session (.getSession (req :request))
          ;build-up ring like session map from httpservlet
          names-in (enumeration-seq (.getAttributeNames http-session))
          session-in (into {} (for [name names-in] [name (.getAttribute http-session name)]))]
      ;pass the session map in the request
      (let [resp (app (assoc req :session session-in))]
        ;check for :session in response map to process changes to session
        (if (contains? resp :session)
          ;changes to session present
          (if (resp :session)
            ;non-nil session, add/change or delete
            (let [session-out (resp :session)]
              ;check for updates or removed
              (doseq [name names-in]
                (if (contains? session-out name)
                  (when-not (= (session-in name) (session-out name))
                    ;update value
                    (.setAttribute http-session name (session-out name)))
                  ;no longer there, there delete it
                  (.removeAttribute http-session name)))
              ;check for additions
              (doseq [name (keys session-out)]
                (when-not (contains? session-in name)
                  (.setAttribute http-session name (session-out name))))
              (dissoc resp :session))
            ;nil session, delete all
            (do
              (.invalidate http-session)
              (dissoc resp :session)))
          ;no session changes
          resp)))))

;ring app
(def app (-> main-routes
           (wrap-ae-session)))

(ae/def-appengine-app magic-app (var app))

