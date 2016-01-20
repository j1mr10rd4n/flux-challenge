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

(defmethod mutate 'dark-jedis/scroll
  [{:keys [state] :as env} key {:keys [direction] :as params}]
  (.log js/console "dark-jedis/list: " (str (@state :dark-jedis/list)))
    (condp = direction
      "down" (let [new-jedi-4 {:id 1 :name "steve" :homeworld "mars"}
                   new-jedi-5 {:id 2 :name "bob" :homeworld "jupiter"}
                   jedi-array (apply conj (subvec (@state :dark-jedis/list) 2 5) [new-jedi-4 new-jedi-5])]
        {:value {:keys [:dark-jedis/list]}
         :action #(swap! ui-of-the-sith.core/app-state assoc :dark-jedis/list jedi-array)})
      "up" (let [new-jedi-1 {:id 1 :name "jon" :homeworld "mars"}
                 new-jedi-2 {:id 2 :name "jim" :homeworld "jupiter"}
                 jedi-array (apply conj [new-jedi-1 new-jedi-2] (subvec (@state :dark-jedis/list) 0 3))]
        {:value {:keys [:dark-jedis/list]}
         :action #(swap! ui-of-the-sith.core/app-state assoc :dark-jedis/list jedi-array)})))
