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
   
(defmethod mutate 'obi-wan-planet/update
  [{:keys [state] :as env} key {:keys [planet-name] :as params}]
    (.log js/console "mutate :update-planet ")
    {:value {:keys :obi-wan-planet}
     :action #(swap! state assoc :obi-wan-planet planet-name) })

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
