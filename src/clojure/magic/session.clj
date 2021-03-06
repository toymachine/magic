(ns magic.session
  (:use magic.util))

(def ^{:private true} *memory-session* nil)

(defn get-value [k]
	(get *memory-session* k))

(defn put-value! [k v]
  (set! *memory-session* (assoc *memory-session* k v)))

(defn remove-value! [k]
  (set! *memory-session* (dissoc *memory-session* k)))


(defn wrap-stateful-session-app-engine [app]
  (fn [req]
    (binding [*memory-session* (req :ae-session)]
      (-> (app req)
        (assoc-if-not-same req :ae-session *memory-session*)))))        

(defn wrap-app-engine-session 
  ([app]
    (wrap-app-engine-session app {}))
  ([app options] 
    (fn [req]
      (let [session-key (options :session-key :session)
            http-session (.getSession (req :request))
            ;build-up ring like session map from httpservlet
            names-in (enumeration-seq (.getAttributeNames http-session))
            session-in (into {} (for [name names-in] [name (.getAttribute http-session name)]))]
        ;pass the session map in the request
        (let [resp (app (assoc req session-key session-in))]
          ;check for session-key in response map to process changes to session
          (if (contains? resp session-key)
            ;changes to session present
            (if (resp session-key)
              ;non-nil session, add/change or delete
              (let [session-out (resp session-key)]
                ;check for updates or removed
                (doseq [name names-in]
                  (if (contains? session-out name)
                    (when-not (= (session-in name) (session-out name))
                      ;update value
                      (.setAttribute http-session name (session-out name)))
                    ;no longer there, delete it
                    (.removeAttribute http-session name)))
                ;check for additions
                (doseq [name (keys session-out)]
                  (when-not (contains? session-in name)
                    (.setAttribute http-session name (session-out name))))
                (dissoc resp session-key))
              ;nil session, delete all
              (do
                (.invalidate http-session)
                (dissoc resp session-key)))
            ;no session changes
            resp))))))
