(ns ui-of-the-sith.core
  (:require [goog.dom :as gdom]
            [goog.events :as ev]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom])
  (:import goog.net.WebSocket))

(def base-url "ws://localhost:4000")

(def app-state (atom {:obi-wan-planet "Earth"}))

(defn read [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defn mutate [{:keys [state] as :env} key params]
  (if (= 'ui-of-the-sith.core/update-planet key)
    {:value {:keys [:obi-wan-planet]}
     :action #(swap! app-state assoc-in [:obi-wan-planet] (state :obi-wan-planet))}
    {:value :not-found}))

(defn planet-monitor-text
  [planet]
  (str "Obi-Wan currently on " planet))

(def reconciler
  (om/reconciler {:state app-state
                  :parser (om/parser {:read read :mutate mutate})}))

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
    (.open socket base-url))
  (componentWillUnmount [this]
    (.close socket))
  (render [this]
    (let [{:keys [obi-wan-planet]} (om/props this)]
      (dom/h1 #js {:className "css-planet-monitor"} 
              (planet-monitor-text (get (om/props this) :obi-wan-planet))))))

(om/add-root! reconciler
              PlanetMonitor (gdom/getElementByClass "css-root" (gdom/getElement "app")))
