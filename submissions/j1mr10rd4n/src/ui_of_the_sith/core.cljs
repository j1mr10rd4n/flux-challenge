(ns ui-of-the-sith.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defn planet-monitor-text
  [planet]
  (str "Obi-Wan currently on " planet))

(defui PlanetMonitor
  Object
  (render [this]
    (dom/h1 nil (planet-monitor-text "Earth"))))

(def planet-monitor (om/factory PlanetMonitor))

(js/ReactDOM.render (planet-monitor) (gdom/getElement "app"))
