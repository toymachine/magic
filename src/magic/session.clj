(ns magic.session)

(def *cookie-session* nil)
(def *memory-session* nil)

(defn update-cookie [fn & args]
  (set! *cookie-session* (apply fn (concat [*cookie-session*] args))))

(defn get-from-cookie [key]
  (*cookie-session* key))

(defn set-in-cookie [key value]
  (set! *cookie-session* (assoc *cookie-session* key value)))

(defn get-from-memory [key]
  (*memory-session* key))

(defn set-in-memory [key value]
  (set! *memory-session* (assoc *memory-session* key value)))

(defmulti myset (fn [t k v] t))
(defmulti myget (fn [t k] t))

(defn def-methods [key]
  (defmethod myset key [t k v]
    (println "set cookie!" key ":" k "=>" v))
  (defmethod myget key [t k]
    (println "get cookie!" key ":" k)))

(def-methods :cookie)
(def-methods :memory)

(defn- assoc-if-not-same [resp req session-key session-state]
  (if (not= (req session-key) session-state) (assoc resp session-key session-state) resp))

(defn wrap-stateful-sessions [app]
  (fn [req]
    (binding [*cookie-session* (req :session)
              *memory-session* (req :ae-session)]
      (-> (app req)
        (assoc-if-not-same req :session *cookie-session*)
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
