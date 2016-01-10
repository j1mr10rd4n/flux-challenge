(ns ui-of-the-sith.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def app-state (atom {:obi-wan-planet "Earth"}))

(defn read [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defn planet-monitor-text
  [planet]
  (str "Obi-Wan currently on " planet))

(defui PlanetMonitor
  static om/IQuery
  (query [this]
         [:obi-wan-planet])
  Object
  (render [this]
    (let [{:keys [obi-wan-planet]} (om/props this)]
      (dom/h1 nil (planet-monitor-text (get (om/props this) :obi-wan-planet))))))

(def reconciler
  (om/reconciler {:state app-state
                  :parser (om/parser {:read read})}))

(om/add-root! reconciler
              PlanetMonitor (gdom/getElement "app"))
