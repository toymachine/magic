(ns magic.session)

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
