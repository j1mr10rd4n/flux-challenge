(ns ui-of-the-sith.scrollable-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defn scroll-button-css-class
  [direction homeworld-alert?]
  (let [button-class (str "css-button-" direction)]
    (if homeworld-alert?
      (str button-class " css-button-disabled")
      button-class)))

(defui ScrollButton
  Object
  (render [this]
    ;(.log js/console "foo")
    ;(.log js/console (om/props this))
    (let [props (om/props this)
          direction (props :direction)
          homeworld-alert? (props :homeworld-alert?)]
    (dom/button #js {:className (scroll-button-css-class direction homeworld-alert?)}))))

(def scroll-button (om/factory ScrollButton))

(defn slot-css-class [homeworld-alert?]
  (if homeworld-alert?
    "css-slot homeworld-alert"
    "css-slot"))

(defui Slot
  Object
  (render [this]
    (let [props (om/props this)
          name (props :name)
          homeworld (props :homeworld)
          homeworld-alert? (= homeworld (props :obi-wan-planet))]
      (dom/li #js {:className (slot-css-class homeworld-alert?)}
          (dom/h3 nil name)
          (dom/h6 nil (str "Homeworld: " homeworld))))))

(def slot (om/factory Slot))

(defui ScrollableList
  static om/IQuery
  (query [this]
         [:dark-jedis :obi-wan-planet])
  Object
  (render [this]
    (let [props (om/props this)
          dark-jedis (props :dark-jedis)
          obi-wan-planet (props :obi-wan-planet)
          slot-data (map #(merge (select-keys props [:obi-wan-planet]) (select-keys % [:name :homeworld])) dark-jedis)
          homeworld-alert? (some #(= % obi-wan-planet) (map #(% :homeworld) dark-jedis))]
      (dom/section #js {:className "css-scrollable-list"} 
        (apply dom/ul #js {:className "css-slots"} (map slot slot-data))
        (apply dom/div #js {:className "css-scroll-buttons"} 
               (map scroll-button (map #(merge {:homeworld-alert? homeworld-alert?} (hash-map :direction %)) ["up" "down"])))))))

(def scrollable-list (om/factory ScrollableList))
