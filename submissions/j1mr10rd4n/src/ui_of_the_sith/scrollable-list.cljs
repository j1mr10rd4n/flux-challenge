(ns ui-of-the-sith.scrollable-list
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui ScrollButtons
  Object
  (render [this] 
          (dom/div #js {:className "css-scroll-buttons"}
                   (dom/button #js {:className "css-button-up"})
                   (dom/button #js {:className "css-button-down"}))))

(def scroll-buttons (om/factory ScrollButtons nil))

(defui Slot
  Object
  (render [this]
    (dom/li #js {:className "css-slot"}
            (dom/h3 nil "Jorak Uln")
            (dom/h6 nil "Homeworld: Korriban"))))

(def slot (om/factory Slot nil))

(defui ScrollableList
  Object
  (render [this]
    (dom/section #js {:className "css-scrollable-list"} 
                 (apply dom/ul #js {:className "css-slots"} (map slot (range 5)))
                 (scroll-buttons {}))))

(def scrollable-list (om/factory ScrollableList nil))
