(ns magic.app_servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use magic.core)
  (:use [appengine-magic.servlet :only [make-servlet-service-method]]))


(defn -service [this request response]
  ((make-servlet-service-method magic-app) this request response))
