(ns magic.member
  (:require [appengine-magic.services.datastore :as ds])
  (:require [clojure.string :as string]))

(ds/defentity Member [identifier, first-name, middle-name, last-name, email])

(defn full-name [member]
  (string/join " " [(:first-name member) (:middle-name member) (:last-name member)]))
  
(defn find-member-by-identifier [identifier]
  (first (ds/query :kind Member
                   :filter (= :identifier identifier))))

(def *logged-in-member* nil)

(defn wrap-logged-in-member [app]
  (fn [req]
    (let [session (req :session)
          session-logged-in-member-id (session :lm)
          session-logged-in-member (when session-logged-in-member-id (ds/retrieve Member session-logged-in-member-id))]
      (if session-logged-in-member
        (binding [*logged-in-member* session-logged-in-member]
          (app req))
        ;no logged in member:
        (app req)))))

(defn get-logged-in []
  *logged-in-member*)

(defn is-logged-in []
  (not= *logged-in-member* nil))
  

(defn create-henk []
  (let [henk (Member. "https://www.google.com/accounts/o8/id?id=AItOawkRzHryaHOxHHHnTGubXO3YOKF0LLDq4Bg" 
                      "Henk" "", "Punt", "henkpunt@gmail.com")]
    (ds/save! henk)))

(defn testx []
  (let [henk (find-member-by-identifier "https://www.google.com/accounts/o8/id?id=AItOawkRzHryaHOxHHHnTGubXO3YOKF0LLDq4Bg")]
    (println (ds/key-id henk))))

(defn testy []
  (let [henk (ds/retrieve Member 2)]
    henk))
              
              
