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

(defn create-master-of
  [dark-jedi]
    {:id (dark-jedi :master-id)
     :name (str "Master of " (dark-jedi :name))
     :homeworld "unknown"
     :master-id (om/tempid)
     :apprentice-id (dark-jedi :id)})

(defn create-apprentice-of
  [dark-jedi]
    {:id (dark-jedi :apprentice-id)
     :name (str "Apprentice of " (dark-jedi :name))
     :homeworld "unknown"
     :master-id (dark-jedi :id)
     :apprentice-id (om/tempid)})

(defmethod mutate 'dark-jedis/scroll
  [{:keys [state] :as env} key {:keys [direction] :as params}]
    (condp = direction
      "down" (let [dark-jedis-old (@state :dark-jedis/list)
                   new-jedi-4 (create-apprentice-of (dark-jedis-old 4))
                   new-jedi-5 (create-apprentice-of new-jedi-4)
                   jedi-array (apply conj 
                                     (subvec dark-jedis-old 2 5)
                                     [new-jedi-4 new-jedi-5])]
        {:value {:keys [:dark-jedis/list]}
         :action #(swap! ui-of-the-sith.core/app-state assoc :dark-jedis/list jedi-array)})
      "up" (let [dark-jedis-old (@state :dark-jedis/list)
                 new-jedi-2 (create-master-of (dark-jedis-old 0))
                 new-jedi-1 (create-master-of new-jedi-2)
                 jedi-array (apply conj
                                   [new-jedi-1 new-jedi-2]
                                   (subvec (@state :dark-jedis/list) 0 3))]
        {:value {:keys [:dark-jedis/list]}
         :action #(swap! ui-of-the-sith.core/app-state assoc :dark-jedis/list jedi-array)})))
