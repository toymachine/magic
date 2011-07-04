(defproject magic "1.0.0-SNAPSHOT"
  :description "testing clojure on app-engine"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [compojure "0.6.4"]
                 [hiccup "0.3.6"]
                 [digest "1.2.1"]
                 [fs "0.8.1"]
                 [org.openid4java/openid4java-consumer "0.9.5"]]
  :dev-dependencies [[appengine-magic "0.4.2"]
                     [lein-eclipse "1.0.0"]])
