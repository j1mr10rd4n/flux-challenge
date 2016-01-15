(ns ui-of-the-sith.core
  (:require [goog.dom :as gdom]
            [goog.events :as ev]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ui-of-the-sith.parser :as p])
  (:import goog.net.WebSocket))

(def base-url "ws://localhost:4000")

(def app-state (atom {:obi-wan-planet "Earth"}))

(defn planet-monitor-text
  [planet]
  (str "Obi-Wan currently on " planet))

(def reconciler
  (om/reconciler {:state app-state
                  :parser (om/parser {:read p/read :mutate p/mutate})}))

(def socket
  (let [socket (WebSocket.)]
    (ev/listen socket
               #js [WebSocket.EventType.CLOSED
                    WebSocket.EventType.ERROR
                    WebSocket.EventType.MESSAGE
                    WebSocket.EventType.OPENED]
               (fn [e]
                 (let [log-message-content (condp = (.-type e)
                                                  WebSocket.EventType.MESSAGE (.-message e)
                                                  WebSocket.EventType.ERROR (.-data e)
                                                  nil)]
                   (if (= (.-type e) WebSocket.EventType.MESSAGE)
                     (let [planet-name (.-name (JSON.parse (.-message e)))]
                       (om/transact! reconciler `[(update-planet {:obi-wan-planet ~planet-name})])))
                   (.log js/console 
                         (clojure.string/join " " [(.-type e) log-message-content])))))
    socket))

(defui PlanetMonitor
  static om/IQuery
  (query [this]
         [:obi-wan-planet])
  Object
  (componentWillMount [this]
    (.log js/console "componentWillMount - this: " this)
    (.open socket base-url))
  (componentWillUnmount [this]
    (.log js/console "componentWillUnmount - this: " this)
    (.close socket))
  (render [this]
    (.log js/console "render - this: " this)
    (.log js/console "render - this: " this)
    (let [{:keys [obi-wan-planet]} (om/props this)]
      (dom/h1 #js {:className "css-planet-monitor"} 
              (planet-monitor-text (get (om/props this) :obi-wan-planet))))))

(om/add-root! reconciler
              PlanetMonitor (gdom/getElementByClass "css-root" (gdom/getElement "app")))
