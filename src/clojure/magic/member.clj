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

(defn retrieve-by-id [member-id]
  (ds/retrieve Member member-id))

(defn get-id [member]
  (ds/key-id member))

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
              
              
