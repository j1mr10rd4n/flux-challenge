(ns ui-of-the-sith.parser
  (:require [om.next :as om]))

;; =============================================================================
;; Reads

(defn read [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))


;; =============================================================================
;; Mutations

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [_ _ _] {:value :not-found})
   
(defmethod mutate 'ui-of-the-sith.planet-monitor/update-planet
  [{:keys [state] :as env} key {:keys [obi-wan-planet] :as params}]
    {:value {:keys [:obi-wan-planet]}
     :action #(swap! state assoc :obi-wan-planet obi-wan-planet) })
