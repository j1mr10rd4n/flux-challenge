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
  (ident [this {:keys [id]}]
    [:siths/by-id id])
  static om/IQuery
  (query [this]
    [:sith/id :sith/name :sith/homeworld :sith/remote-id])
  Object
  (componentWillReceiveProps [this nextProps]
    (let [{:keys [id remote-id]} nextProps]
      (when (not (= remote-id (:remote-id (om/props this))))
        (om/transact! this `[(sith/populate-from-remote ~{:id id :remote-id remote-id})]))))
  (render [this]
    (let [{:keys [:id :remote-id :name :homeworld :pending]} (om/props this)]
      (dom/li #js {:className (slot-css-class false)}
          (dom/h3 nil (str remote-id " " name))
          (dom/h6 nil (str "Homeworld: " homeworld))))))

(def slot (om/factory Slot {:keyfn :id}))

(defui ScrollableList
  Object
  (render [this]
    (let [list (om/props this)
          ;{:keys [:siths/list]} props
          ;;slot-data (map #(merge (select-keys props [:obi-wan-planet]) (select-keys % [:name :homeworld :id])) dark-jedis)
          ;;; do i put the homeworld alert in the application state as a derived signal?
          ;;homeworld-alert? (some #(= % obi-wan-planet) (map #(% :homeworld) dark-jedis))
          ]
      (dom/section #js {:className "css-scrollable-list"} 
        (apply dom/ul #js {:className "css-slots"} (map slot list))
        ;(apply dom/div #js {:className "css-scroll-buttons"} 
               ;(map scroll-button (map #(merge {:homeworld-alert? homeworld-alert?} (hash-map :direction %)) ["up" "down"])))
))))

(def scrollable-list (om/factory ScrollableList))
