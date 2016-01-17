(ns ui-of-the-sith.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ui-of-the-sith.parser :as p]
            [ui-of-the-sith.planet-monitor :as pm]
            [ui-of-the-sith.scrollable-list :as sl]))

(def app-state (atom {:obi-wan-planet "Earth"}))

(def reconciler
  (om/reconciler {:state app-state
                  :parser (om/parser {:read p/read :mutate p/mutate})}))

(defui App
  Object
  (render [this] 
    (let [props (om/props this)
          {:keys [:obi-wan-planet]} props]
      (dom/div #js {:className "css-root"}
        (pm/planet-monitor obi-wan-planet)
        (sl/scrollable-list props)))))

(om/add-root! reconciler
              App (gdom/getElement "app"))
