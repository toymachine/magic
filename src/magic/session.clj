(ns magic.session)

(def *cookie-session* nil)
(def *memory-session* nil)

(defn get-from-cookie [key]
  (*cookie-session* key))

(defn set-in-cookie [key value]
  (set! *cookie-session* (assoc *cookie-session* key value)))

(defn get-from-memory [key]
  (*memory-session* key))

(defn set-in-memory [key value]
  (set! *memory-session* (assoc *memory-session* key value)))

(defn- update-response [resp req session-out session-key]
  ;check for new var, updated var, deleted var
  (let [session-in (req session-key)
        session-resp 
        (merge
          ;find updates and deletes by interating over session-in
          (reduce 
            (fn [acc [k v]]
              (if (not= (session-out k) (session-in k))
                (assoc acc k (session-out k))
                acc)) {} session-in)
          ;find additions by iteration over session-out
          (reduce
            (fn [acc [k v]]
              (if (not (contains? session-in k))
                (assoc acc k (session-out k))
                acc)) {} session-out))]
    (if (not-empty session-resp)
      (assoc resp session-key session-resp)
      resp)))

(defn wrap-stateful-sessions [app]
  (fn [req]
    (binding [*cookie-session* (req :session)
              *memory-session* (req :ae-session)]
      (-> (app req)
        (update-response req *cookie-session* :session)
        (update-response req *memory-session* :ae-session)))))

(defn wrap-ae-session 
  ([app]
    (wrap-ae-session app {}))
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
