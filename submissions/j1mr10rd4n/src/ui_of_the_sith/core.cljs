(ns ui-of-the-sith.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def app-state (atom {:obi-wan-planet "Earth"}))

(defn planet-monitor-text
  [planet]
  (str "Obi-Wan currently on " planet))

(defui PlanetMonitor
  Object
  (render [this]
    (let [{:keys [obi-wan-planet]} (om/props this)]
      (dom/h1 nil (planet-monitor-text (get (om/props this) :obi-wan-planet))))))

(def planet-monitor (om/factory PlanetMonitor))

(def reconciler
  (om/reconciler {:state app-state}))

(om/add-root! reconciler
              PlanetMonitor (gdom/getElement "app"))
