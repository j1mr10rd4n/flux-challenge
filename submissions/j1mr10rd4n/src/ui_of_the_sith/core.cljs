(ns ui-of-the-sith.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(println "moo")

(defui PlanetMonitor
  Object
  (render [this]
    (dom/h1 nil "Obi-Wan currently on Earth")))

(def planet-monitor (om/factory PlanetMonitor))

(js/ReactDOM.render (planet-monitor) (gdom/getElement "app"))
