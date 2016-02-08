(ns ui-of-the-sith.scrollable-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ui-of-the-sith.config :as cfg]))

(defn scroll-button-css-class
  [direction enabled?]
  (let [button-class (str "css-button-" (name direction))]
    (if-not enabled?
      (str button-class " css-button-disabled")
      button-class)))

(defn scroll-button-click
  [scroll-button direction enabled? e]
  (let [callback (:scroll-callback (om/get-computed (om/props scroll-button)))]
    (if enabled?
      (callback direction)
      (doto e (.preventDefault) (.stopPropagation)))))

(defui ScrollButton
  Object
  (render [this]
    (let [{:keys [direction enabled?]} (om/props this)]
    (dom/button #js {:className (scroll-button-css-class direction enabled?)
                     :onClick #(scroll-button-click this direction enabled? %)}))))

(def scroll-button (om/factory ScrollButton))

(defn slot-css-class 
  [{:keys [sith/homeworld obi-wan-planet]}]
  (if (and (not (nil? homeworld)) (= homeworld obi-wan-planet))
    "css-slot homeworld-alert"
    "css-slot"))

(defn abort-xhr [{:keys [xhr]}]
    (if xhr
      (.abort xhr)))

(defn abort-and-restart-xhr-if-required
  [{:keys [xhr]} matching-planet-in-list?]
  (if xhr
    (if matching-planet-in-list?
      ; abort request if there is a match
      (.abort xhr)
      ; restart request if there is no match and an aborted request
      (if (= 7 (.getLastErrorCode xhr))
        (.send xhr (.getLastUri xhr))))))

(defui Slot
  static om/Ident
  (ident [this {:keys [sith/id]}]
    [:siths/by-id id])
  static om/IQuery
  (query [this]
    [:sith/id
     :sith/name
     :sith/homeworld
     :sith/remote-id
     :sith/apprentice-id
     :sith/apprentice-remote-id
     :sith/master-id
     :sith/master-remote-id])
  Object
  (componentDidMount [this]
    (let [{:keys [sith/id sith/remote-id] :as sith} (om/props this)]
      (if (not (nil? remote-id))
        (om/transact! this
                      `[(sith/populate-from-remote ~{:sith sith})
                      [~[:siths/by-id id]]]))))    
  (componentDidUpdate [this prevProps prevState]
    (let [{:keys [sith/id 
                  sith/remote-id
                  sith/name
                  matching-planet-in-list?] :as sith} (om/props this)
          prev-remote-id (:sith/remote-id prevProps)
          prev-name (:sith/name prevProps)
          remote-id-changed? (and (not (= remote-id prev-remote-id)) 
                                  (not (nil? remote-id)))
          populated-from-remote? (not (= name prev-name))
          populate-from-remote-callback (:populate-from-remote-callback (om/get-computed this))]
      (if remote-id-changed?
        (om/transact! this
                      `[(sith/populate-from-remote ~{:sith sith})
                      [~[:siths/by-id id]]]))
      (if populated-from-remote?
        (populate-from-remote-callback id))
      (abort-and-restart-xhr-if-required (om/get-state this) matching-planet-in-list?)))
  (componentWillUnmount [this]
    (abort-xhr (om/get-state this)))
  (render [this]
    (let [{:keys [sith/id sith/remote-id sith/name sith/homeworld obi-wan-planet] :as props} (om/props this)]
      (dom/li #js {:className (slot-css-class props)}
          (dom/h3 nil name)
          (if-not (nil? homeworld)
            (dom/h6 nil (str "Homeworld: " homeworld)))))))

(def slot (om/factory Slot {:keyfn :sith/id}))

(defn homeworlds-matching-planet
  [siths planet]
  (filter #(and (= planet %) (not (nil? %))) (map #(:sith/homeworld %) siths)))

(defn can-scroll?
  [list obi-wan-planet direction]
  (and
  (empty? (homeworlds-matching-planet list obi-wan-planet))
  (condp = direction
    :up (not (nil? (get-in list [0 :sith/master-remote-id])))
    :down (not (nil? (get-in list [(- cfg/list-size 1) :sith/apprentice-remote-id]))))))

(defui ScrollableList
  Object
  (render [this]
    (let [{:keys [obi-wan-planet siths/list]} (om/props this)
          {:keys [populate-from-remote-callback scroll-callback]} (om/get-computed list)
          scrollable-list-props (map #(merge % {:obi-wan-planet obi-wan-planet :matching-planet-in-list? (seq (homeworlds-matching-planet list obi-wan-planet))}) list)
          ; DONT DO THIS! YOU'LL LOSE ALL YOUR META! - possible om improvement? on blank path
          ;scrollable-list-props-wrong (map #(merge {:obi-wan-planet "miaow"} %) list-with-computed)
          scrollable-list-props-with-callback (map #(om/computed % {:populate-from-remote-callback populate-from-remote-callback}) scrollable-list-props)
          scroll-button-props (map #(hash-map :direction % :enabled? (can-scroll? list obi-wan-planet %)) [:up :down])
          scroll-button-props-with-callback (map #(om/computed % {:scroll-callback scroll-callback}) scroll-button-props)]
      (dom/section #js {:className "css-scrollable-list"} 
        (apply dom/ul #js {:className "css-slots"} (map slot scrollable-list-props-with-callback))
        (apply dom/div #js {:className "css-scroll-buttons"} (map scroll-button scroll-button-props-with-callback))))))

(def scrollable-list (om/factory ScrollableList))
