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
  (let [initial-sith {:id (om/tempid)
                      :name "Unknown"
                      :homeworld "unknown"
                      :master-id nil
                      :apprentice-id nil
                      :remote-id nil
                      :master-remote-id nil
                      :apprentice-remote-id nil
                      :pending true}]
    (u/fill-siths :apprentice [initial-sith])))

(def app-state (atom {:siths/list initial-siths}))

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
                             (.log js/console "GOT RESPONSE FOR " (jedi-data "name") " WITH REMOTE ID " (jedi-data "id"))
                             (let [siths (@app-state :siths/list)
                                   remote-id (jedi-data "id")
                                   master-remote-id (get-in jedi-data ["master" "id"])
                                   apprentice-remote-id (get-in jedi-data ["apprentice" "id"])
                                   name (jedi-data "name")
                                   homeworld (get-in jedi-data ["homeworld" "name"])
                                   apprentice-id (reduce (fn [result sith]
                                                           (if (= (sith :remote-id) remote-id)
                                                             (sith :apprentice-id)
                                                             result))
                                                         nil
                                                         siths)
                                   master-id (reduce (fn [result sith]
                                                       (if (= (sith :remote-id) remote-id)
                                                         (sith :master-id)
                                                         result))
                                                     nil
                                                     siths)
                                   siths' (map (fn [sith]
                                                 (cond
                                                   (= (sith :remote-id) remote-id) (assoc sith :name name
                                                                                               :homeworld homeworld
                                                                                               :master-remote-id master-remote-id
                                                                                               :apprentice-remote-id apprentice-remote-id
                                                                                               :pending false)
                                                   (= (sith :id) apprentice-id) (assoc sith :remote-id apprentice-remote-id :master-remote-id remote-id)
                                                   (= (sith :id) master-id) (assoc sith :remote-id master-remote-id :apprentice-remote-id remote-id)
                                                   :else sith))
                                               siths)
                                   filled-siths (cond
                                                  (u/contains-sith-with-no-apprentice? siths') (u/fill-siths :master (filterv (fn [{:keys [pending]}] (not pending)) siths'))
                                                  (u/contains-sith-with-no-master? siths') (u/fill-siths :apprentice (filterv (fn [{:keys [pending]}] (not pending)) siths'))
                                                  :else siths')]
                               (cb {:siths/list filled-siths}))))))))
        (.send xhr uri))
      (recur (<! c)))))

(def send-chan (chan))

(defn send-to-chan [c]
  (fn [{:keys [dark-jedi-service]} cb]
    (when dark-jedi-service
      (let [{[dark-jedi-service] :children} (om/query->ast dark-jedi-service)
            remote-id (get-in dark-jedi-service [:params :remote-id])]
        (put! c [remote-id cb])))))

(def reconciler
  (om/reconciler
    {:state {:siths/list initial-siths} ;giving reconciler degenerate data not in atom
     :parser (om/parser {:read p/read :mutate p/mutate})
     :send (send-to-chan send-chan) 
     :remotes [:dark-jedi-service]
     :logger logger}))

(dark-jedi-service-loop send-chan)

(defui App
  static om/IQuery
  (query [this]
    `[{:siths/list ~(om/get-query sl/Slot)}])
  Object
  (componentDidMount [this]
    (om/transact! this `[(siths/init-list ~{:remote-id initial-sith-remote-id})]))
  (render [this] 
    (let [{:keys [:siths/list]} (om/props this)]
      (dom/div #js {:className "css-root"}
        (pm/planet-monitor)
        (sl/scrollable-list list)))))

(om/add-root! reconciler
              App (gdom/getElement "app"))
