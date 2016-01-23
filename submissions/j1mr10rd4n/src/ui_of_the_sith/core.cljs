(ns ui-of-the-sith.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [goog.events :as ev]
            [goog.object :as o]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ui-of-the-sith.parser :as p]
            [ui-of-the-sith.planet-monitor :as pm]
            [ui-of-the-sith.scrollable-list :as sl])
  (:import [goog Uri]
           [goog.net XhrIo]))

(def dark-jedis [{:id 5956 :name "Darth Tenebrous"   :homeworld "Clak\"dor VII" :master-id 1121 :apprentice-id 2350}
                 {:id 2350 :name "Darth Plagueis"    :homeworld "Mygeeto"       :master-id 5956 :apprentice-id 3616}
                 {:id 3616 :name "Darth Sidious"     :homeworld "Naboo"         :master-id 2350 :apprentice-id 1489}
                 {:id 1489 :name "Darth Vader"       :homeworld "Tatooine"      :master-id 3616 :apprentice-id 1330}
                 {:id 1330 :name "Antinnis Tremayne" :homeworld "Coruscant"     :master-id 1489}])
(def base-url "http://localhost:3000/dark-jedis/")

(def app-state (atom {:sith/list initial-siths }))

(defn dark-jedi-service-loop [c]
  (go
    (loop [[remote-id cb] (<! c)]
      (let [url (str base-url remote-id)
            uri (Uri. url)
            xhr (XhrIo.)]
        (ev/listen xhr 
                   #js [goog.net.EventType.COMPLETE]
                   (fn [e] 
                     (if (= (o/get e "type") goog.net.EventType.COMPLETE)
                       (let [xhr (o/get e "target")]
                         (if-let [status (= 200 (-> xhr .getStatus))]
                           (let [jedi-data (-> xhr .getResponseJson js->clj)]
                             (.log js/console "GOT RESPONSE FOR " (jedi-data "name") " WITH REMOTE ID " (jedi-data "id"))))))))
                           
        (.send xhr uri))
      (recur (<! c)))))

(def send-chan (chan))

(def reconciler
  (om/reconciler {:state app-state
                  :parser (om/parser {:read p/read :mutate p/mutate})}))

(dark-jedi-service-loop send-chan)

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
