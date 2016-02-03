(ns ui-of-the-sith.scrollable-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defn scroll-button-css-class
  [direction homeworld-alert?]
  (let [button-class (str "css-button-" direction)]
    (if homeworld-alert?
      (str button-class " css-button-disabled")
      button-class)))

(defn scroll [direction]
  (.log js/console " - scroll detected " direction)
  (om/transact! ui-of-the-sith.core/reconciler `[(dark-jedis/scroll {:direction ~direction})]))

(defn scroll-button-click
  [scroll-button direction homeworld-alert? e]
  (if homeworld-alert?
    (doto e (.preventDefault) (.stopPropagation))
    (scroll direction)
  ))

(defui ScrollButton
  Object
  (render [this]
    (let [props (om/props this)
          direction (props :direction)
          homeworld-alert? (props :homeworld-alert?)]
    (dom/button #js {:className (scroll-button-css-class direction homeworld-alert?)
                     :onClick #(scroll-button-click this direction homeworld-alert? %)}))))

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
    [:sith/id :sith/name :sith/homeworld :sith/remote-id])
  Object
  (componentDidUpdate [this prevProps prevState]
    (let [{:keys [sith/id 
                  sith/remote-id
                  sith/name
                  sith/apprentice-remote-id
                  sith/apprentice-id] :as sith} (om/props this)
          prev-remote-id (:sith/remote-id prevProps)
          prev-name (:sith/name prevProps)
          prev-apprentice-remote-id (:sith/apprentice-remote-id prevProps)
          set-remote-id-callback (:set-remote-id-callback (om/get-computed this))]
      (when-not (= remote-id prev-remote-id)
        (om/transact! this `[(sith/populate-from-remote ~{:sith sith})
                             [~[:siths/by-id id]]]))
      (when-not (= name prev-name)
        (set-remote-id-callback apprentice-id apprentice-remote-id))))
  (render [this]
    (let [{:keys [sith/id sith/remote-id sith/name sith/homeworld]} (om/props this)]
      (dom/li #js {:className (slot-css-class false)}
          (dom/h3 nil (str remote-id " " name))
          (dom/h6 nil (str "Homeworld: " homeworld))))))

(def slot (om/factory Slot {:keyfn :sith/id}))

(defui ScrollableList
  Object
  (render [this]
    (let [list (om/props this)
          ;{:keys [:siths/list]} props
          ;;slot-data (map #(merge (select-keys props [:obi-wan-planet]) (select-keys % [:name :homeworld :id])) dark-jedis)
          ;;; do i put the homeworld alert in the application state as a derived signal?
          ;;homeworld-alert? (some #(= % obi-wan-planet) (map #(% :homeworld) dark-jedis))
          slots (map #(let[slot' (om/computed % (om/get-computed this))] (slot slot')) list)
          ]
      (dom/section #js {:className "css-scrollable-list"} 
        (apply dom/ul #js {:className "css-slots"} slots)
        ;(apply dom/div #js {:className "css-scroll-buttons"} 
               ;(map scroll-button (map #(merge {:homeworld-alert? homeworld-alert?} (hash-map :direction %)) ["up" "down"])))
))))

(def scrollable-list (om/factory ScrollableList))
