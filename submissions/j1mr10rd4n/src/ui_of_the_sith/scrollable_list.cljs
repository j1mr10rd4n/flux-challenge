(ns ui-of-the-sith.scrollable-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [ui-of-the-sith.config :as cfg]))

(defn set-scroll-button-state
  [button {:keys [direction obi-wan-planet-match? at-start? at-end? scroll-button-callback]}]
   (let [enabled? (and (nil? obi-wan-planet-match?)
                      (condp = direction
                        :up (not at-start?)
                        :down (not at-end?)))

         css-class (let [button-class (str "css-button-" (name direction))]
                     (if-not enabled?
                       (str button-class " css-button-disabled")
                       button-class)) 
         on-click (if enabled? 
                    (fn [e] 
                      (.log js/console "CLICK!")
                      (scroll-button-callback direction)
                    )
                    (fn [e] (doto e (.preventDefault) (.stopPropagation))))]
    (om/set-state! button {:enabled? enabled?
                           :css-class css-class
                           :on-click on-click})))

(defui ScrollButton
  Object
  (componentWillMount [this]
    (set-scroll-button-state this (om/props this)))
  (componentWillReceiveProps [this nextProps]
    (set-scroll-button-state this nextProps))
  (render [this]
    (let [{:keys [direction]} (om/props this)
          {:keys [enabled? css-class on-click]} (om/get-state this)]
    (dom/button #js {:className css-class
                     :onClick on-click}))))

(defn scroll-button
  [state direction]
  ((om/factory ScrollButton) (merge state {:direction direction})))

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

(defn set-button-state
  [scrollable-list {:keys [obi-wan-planet siths/list] :as props}]
  (om/set-state! scrollable-list {:at-start? (nil? (get-in list [0 :sith/master-remote-id]))
                                  :at-end? (nil? (get-in list [(- cfg/list-size 1) :sith/apprentice-remote-id]))
                                  :obi-wan-planet-match? (seq (filter #(and (= obi-wan-planet %) (not (nil? %))) (map #(:sith/homeworld %) list)))
                                  :scroll-button-callback (:scroll-callback (om/get-computed list))}))

(defui ScrollableList
  Object
  (componentWillMount
    [this]
      (set-button-state this (om/props this)))
  (componentWillReceiveProps 
    [this nextProps]
    (set-button-state this nextProps))
  (render [this]
    (let [{:keys [obi-wan-planet siths/list]} (om/props this)
          {:keys [populate-from-remote-callback scroll-callback]} (om/get-computed list)
          scrollable-list-props (map #(merge % {:obi-wan-planet obi-wan-planet :matching-planet-in-list? (seq (homeworlds-matching-planet list obi-wan-planet))}) list)
          ; DONT DO THIS! YOU'LL LOSE ALL YOUR META! - possible om improvement? on blank path
          ;scrollable-list-props-wrong (map #(merge {:obi-wan-planet "miaow"} %) list-with-computed)
          scrollable-list-props-with-callback (map #(om/computed % {:populate-from-remote-callback populate-from-remote-callback}) scrollable-list-props)]
      (dom/section #js {:className "css-scrollable-list"}
        (apply dom/ul #js {:className "css-slots"} (map slot scrollable-list-props-with-callback))
        (apply dom/div #js {:className "css-scroll-buttons"} (map #(scroll-button (om/get-state this) %) [:up :down]))))))

(def scrollable-list (om/factory ScrollableList))
