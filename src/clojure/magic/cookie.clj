(ns magic.cookie
  (:use magic.util))

(def ^{:private true} *cookie-session* nil)

(defn get-value [k]
	(get *cookie-session* k))

(defn put-value! [k v]
  (set! *cookie-session* (assoc *cookie-session* k v)))

(defn remove-value! [k]
  (set! *cookie-session* (dissoc *cookie-session* k)))

(defn wrap-stateful-session-cookie [app]
  (fn [req]
    (binding [*cookie-session* (req :session)]
      (-> (app req)
        (assoc-if-not-same req :session *cookie-session*)))))        

