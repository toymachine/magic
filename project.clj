(defproject magic "1.0.0-SNAPSHOT"
  :description "testing clojure on app-engine"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [compojure "0.6.4"]
                 [hiccup "0.3.6"]
                 [digest "1.2.1"]
                 [fs "0.8.1"]
                 [aehttpfetcher "1.1"]
                 [org.openid4java/openid4java-nodeps "0.9.6"]
                 [org.apache.httpcomponents/httpclient "4.1.1"]
                 [com.google.collections/google-collections "1.0"]
                 [com.google.inject/guice "2.0"]]
  :dev-dependencies [[appengine-magic "0.4.2"]
                     [lein-eclipse "1.0.0"]
                     [ring/ring-devel "0.3.10"]
                     [lein-localrepo "0.3"]]
  :repl-init start)
