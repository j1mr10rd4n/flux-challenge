(ns ui-of-the-sith.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ui-of-the-sith.parser :as p]
            [ui-of-the-sith.planet-monitor :as pm]
            [ui-of-the-sith.scrollable-list :as sl]))

(def dark-jedis [{:id 5956 :name "Darth Tenebrous"   :homeworld "Clak\"dor VII" :master-id 1121 :apprentice-id 2350}
                 {:id 2350 :name "Darth Plagueis"    :homeworld "Mygeeto"       :master-id 5956 :apprentice-id 3616}
                 {:id 3616 :name "Darth Sidious"     :homeworld "Naboo"         :master-id 2350 :apprentice-id 1489}
                 {:id 1489 :name "Darth Vader"       :homeworld "Tatooine"      :master-id 3616 :apprentice-id 1330}
                 {:id 1330 :name "Antinnis Tremayne" :homeworld "Coruscant"     :master-id 1489}])

(def app-state (atom {:sith/list initial-siths }))

(def reconciler
  (om/reconciler {:state app-state
                  :parser (om/parser {:read p/read :mutate p/mutate})}))

(defui App
  static om/IQuery
  (query [this]
     '[:obi-wan-planet {:dark-jedis/list (om/get-query sl/Slot)}])
  Object
  (render [this] 
    (let [props (om/props this)]
      (dom/div #js {:className "css-root"}
        (pm/planet-monitor)
        (sl/scrollable-list props)))))

(om/add-root! reconciler
              App (gdom/getElement "app"))
