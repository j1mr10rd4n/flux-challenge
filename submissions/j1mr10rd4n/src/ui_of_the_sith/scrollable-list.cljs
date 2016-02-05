(ns ui-of-the-sith.scrollable-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

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

(defn slot-css-class [homeworld-alert?]
  (if homeworld-alert?
    "css-slot homeworld-alert"
    "css-slot"))

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
                  sith/name] :as sith} (om/props this)
          prev-remote-id (:sith/remote-id prevProps)
          prev-name (:sith/name prevProps)
          remote-id-changed? (not (= remote-id prev-remote-id))
          populated-from-remote? (not (= name prev-name))
          populate-from-remote-callback (:populate-from-remote-callback (om/get-computed this))]
      (if remote-id-changed?
        (om/transact! this
                      `[(sith/populate-from-remote ~{:sith sith})
                      [~[:siths/by-id id]]]))
      (if populated-from-remote?
        (populate-from-remote-callback id))))
  (render [this]
    (let [{:keys [sith/id sith/remote-id sith/name sith/homeworld]} (om/props this)]
      (dom/li #js {:className (slot-css-class false)}
          (dom/h3 nil (str remote-id " " name))
          (dom/h6 nil (str "Homeworld: " homeworld))))))

(def slot (om/factory Slot {:keyfn :sith/id}))

(defn can-scroll?
  [list direction]
  (condp = direction
    :up (not (nil? (get-in list [0 :sith/master-remote-id])))
    :down (not (nil? (get-in list [(- ui-of-the-sith.core/list-size 1) :sith/apprentice-remote-id])))))

(defui ScrollableList
  Object
  (render [this]
    (let [list (om/props this)
          ;{:keys [:siths/list]} props
          ;;slot-data (map #(merge (select-keys props [:obi-wan-planet]) (select-keys % [:name :homeworld :id])) dark-jedis)
          ;;; do i put the homeworld alert in the application state as a derived signal?
          ;;homeworld-alert? (some #(= % obi-wan-planet) (map #(% :homeworld) dark-jedis))
          slots (map #(let [slot' (om/computed % {:populate-from-remote-callback (:populate-from-remote-callback(om/get-computed this))})] (slot slot')) list)
          button-props (map #(hash-map :direction % :enabled? (can-scroll? list %)) [:up :down])
          scroll-buttons (map #(let [button' (om/computed % {:scroll-callback (:scroll-callback (om/get-computed (om/props this)))})] (scroll-button button')) button-props)]
      (dom/section #js {:className "css-scrollable-list"} 
        (apply dom/ul #js {:className "css-slots"} slots)
        (apply dom/div #js {:className "css-scroll-buttons"} scroll-buttons)))))

(def scrollable-list (om/factory ScrollableList))
