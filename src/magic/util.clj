(ns magic.util
  (:use compojure.core)
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use ring.util.response)
  (:require [clojure.string :as string]))

(defn str-map [m]
  (string/join "\n" (for [[k v] m] (str k "=>" v))))

(defn base-url [req]
  (let [scheme (name (req :scheme ":http"))
        server-name (req :server-name "localhost")
        server-port (req :server-port 80)]
    (str scheme "://" server-name (when (not= server-port 80) (str ":" server-port)))))


(def a {:a 10 :b 20 :c 30})

(def b {:a 11 :b 21 :d 40})

(defn x []
  (if-let [y (> 3 2)]
    	y))