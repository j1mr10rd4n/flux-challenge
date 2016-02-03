(ns ui-of-the-sith.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [goog.events :as ev]
            [goog.object :as o]
            [goog.log :as glog]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ui-of-the-sith.parser :as p]
            [ui-of-the-sith.planet-monitor :as pm]
            [ui-of-the-sith.scrollable-list :as sl]
            [ui-of-the-sith.util :as u])
  (:import [goog Uri]
           [goog.net XhrIo]))

(defonce logger 
  (let [logger (glog/getLogger "sith.ui")]
    (.setLevel logger goog.debug.Logger.Level.FINEST)
    logger))

(def base-url "http://localhost:3000/dark-jedis/")

(def initial-sith-remote-id 3616)

(def initial-siths
  (let [initial-sith {:sith/id (om/tempid)
                      :sith/name "Unknown"
                      :sith/homeworld "unknown"
                      :sith/master-id nil
                      :sith/apprentice-id nil
                      :sith/remote-id nil
                      :sith/master-remote-id nil
                      :sith/apprentice-remote-id nil}]
    (u/fill-siths :apprentice [initial-sith])))

(def send-chan (chan))

(defn send-to-chan [c]
  (fn [{:keys [dark-jedi-query]} cb]
    (when dark-jedi-query
      (let [{[dark-jedi-query] :children} (om/query->ast dark-jedi-query)
            {:keys [sith]} (:params dark-jedi-query)]
        (put! c [sith cb])))))

(def reconciler
  (om/reconciler
    {:state {:siths/list initial-siths} ;giving reconciler degenerate data not in atom
     :parser (om/parser {:read p/read :mutate p/mutate})
     :send (send-to-chan send-chan) 
     :remotes [:dark-jedi-query]
     :logger logger}))

(defn dark-jedi-service-loop [c]
  (go
    (loop [[{:keys [sith/id sith/remote-id]:as sith} cb] (<! c)]
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
                             (.log js/console "GOT RESPONSE FOR " (jedi-data "name") " WITH REMOTE ID " (jedi-data "id"))
                             (let [name (jedi-data "name")
                                   homeworld (get-in jedi-data ["homeworld" "name"])
                                   apprentice-remote-id (get-in jedi-data ["apprentice" "id"])
                                   master-remote-id (get-in jedi-data ["master" "id"])
                                   populated-sith (assoc sith :sith/name name
                                                              :sith/homeworld homeworld
                                                              :sith/apprentice-remote-id apprentice-remote-id
                                                              :sith/master-remote-id master-remote-id)
                                   siths-by-id (:siths/by-id @reconciler)
                                   siths-by-id' (assoc siths-by-id id populated-sith)]
                               (cb {:siths/by-id siths-by-id'})
                               
                               
                               )))))))
        (.send xhr uri))
      (recur (<! c)))))

(dark-jedi-service-loop send-chan)

(defn set-remote-id-callback
  [component]
  (fn [id remote-id] 
    (om/transact! component 
                  `[(sith/set-remote-id ~{:id id :remote-id remote-id})
                  [~[:siths/by-id id]]])))

(defui App
  static om/IQuery
  (query [this]
    `[{:siths/list ~(om/get-query sl/Slot)}])
  Object
  (componentDidMount [this]
    (let [initial-sith-id (get-in (om/props this) [:siths/list 0 :sith/id])]
      ((set-remote-id-callback this) initial-sith-id initial-sith-remote-id)))
  (render [this] 
    (let [{:keys [:siths/list]} (om/props this)
          list' (om/computed list
                             {:set-remote-id-callback (set-remote-id-callback this)})]
      (dom/div #js {:className "css-root"}
        (pm/planet-monitor)
        (sl/scrollable-list list')))))

(om/add-root! reconciler
              App (gdom/getElement "app"))
