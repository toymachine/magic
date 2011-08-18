(ns magic.member
  (:require [appengine-magic.services.datastore :as ds])
  (:require [clojure.string :as string])
  (:require [magic.cookie :as cookie]))

(ds/defentity Member [identifier, first-name, middle-name, last-name, email])

(defn full-name 
  "gets the full-name of given member"
  [member]
  (string/join " " [(:first-name member) (:middle-name member) (:last-name member)]))
  
(defn find-by-identifier [identifier]
  "find a member by his identifier"
  (first (ds/query :kind Member
                   :filter (= :identifier identifier))))

(def *logged-in-member* nil)

(defn set-logged-in-member-by-identifier! [identifier]
  (if-let [member (find-by-identifier identifier)]
    (cookie/put-value! :lm (ds/key-id member))))

(defn- get-logged-in-member []
  (if-let [logged-in-member-id (cookie/get-value :lm)]
    (ds/retrieve Member logged-in-member-id)))

(defn wrap-logged-in-member [app]
  (fn [req]
    (if-let [logged-in-member (get-logged-in-member)]			    
      ;bind logged in member for this request		
      (binding [*logged-in-member* logged-in-member]
        (app req))	
      ;no logged in member:
      (app req))))

(defn get-logged-in []
  *logged-in-member*)

(defn is-logged-in []
  (not= *logged-in-member* nil))
  

(defn create-henk []
  (let [henk (Member. "https://www.google.com/accounts/o8/id?id=AItOawlbMCGmVgXarambOMUhAgTVr9xkrLUwSYY" 
                      "Henk" "", "Punt", "henkpunt@gmail.com")]
    (ds/save! henk)))

(defn testx []
  (let [henk (find-by-identifier "https://www.google.com/accounts/o8/id?id=AItOawkRzHryaHOxHHHnTGubXO3YOKF0LLDq4Bg")]
    (println (ds/key-id henk))))

(defn testy []
  (let [henk (ds/retrieve Member 2)]
    henk))
              
              
