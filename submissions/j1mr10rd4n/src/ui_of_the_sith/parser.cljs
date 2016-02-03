(ns ui-of-the-sith.parser
  (:require [om.next :as om]
            [ui-of-the-sith.util :as u]))

;; =============================================================================
;; Reads

(defmulti read 
  (fn [env key params]
    key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [value (get-in st key)]
      {:value value}
      {:value :not-found})))

(defn get-siths
  [state key]
  ; (get st key) - this is being called with key of :siths/list and is getting the
  ; now normalized array of siths/by-id references
  ; the get-in st % function is being called with [:siths/by-id <<id>>] which is
  ; the `table` of normalized data in the state!
  ; i.e. the original denormalized list has been converted to a list of references
  ; that can be passed directly to get-in st
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod read :siths/list
  [{:keys [state] :as env} key params]
  {:value (get-siths state key)})

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
(defmethod mutate 'sith/set-remote-id
  [{:keys [state] :as env} key {:keys [id remote-id] :as params}]
    {:action #(swap! state assoc-in [:siths/by-id id :sith/remote-id] remote-id)
     :value {:keys `[~[:siths/by-id id]]}})

(defmethod mutate 'sith/populate-from-remote
  [{:keys [ast] :as env} key params]
  {:dark-jedi-query ast})
