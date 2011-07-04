(ns start)

(require '[appengine-magic.core :as ae])
(use 'magic.core :reload) 
(ae/serve magic-app)      
