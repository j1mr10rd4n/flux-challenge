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

(defn mutate [{:keys [state] as :env} key params]
  (if (= 'ui-of-the-sith.core/update-planet key)
    {:value {:keys [:obi-wan-planet]}
     :action #(swap! state assoc-in [:obi-wan-planet] (params :obi-wan-planet))}
    {:value :not-found}))
