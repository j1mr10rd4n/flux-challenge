(ns ui-of-the-sith.util
  (:require [om.next :as om :refer-macros [defui]]))

(defn create-master-of
  [apprentice]
    {:id (om/tempid)
     :name (str "Master of " (apprentice :name))
     :homeworld "unknown"
     :master-id nil
     :apprentice-id (apprentice :id)
     :remote-id (apprentice :master-remote-id)
     :master-remote-id nil
     :apprentice-remote-id (apprentice :remote-id)
     :pending true})

(defn create-apprentice-of
  [master]
    {:id (om/tempid)  
     :name (str "Apprentice of " (master :name))
     :homeworld "unknown"
     :master-id (master :id)
     :apprentice-id nil
     :remote-id (master :apprentice-remote-id)
     :master-remote-id (master :remote-id)
     :apprentice-remote-id nil
     :pending true})

(defn append-apprentice-to [siths]
  (let [last-master (last siths)
        apprentice (create-apprentice-of last-master)
        siths' (assoc-in siths [(- (count siths) 1) :apprentice-id] (apprentice :id))]
        (into [] (conj siths' apprentice))))

(defn prepend-master-to [siths]
  (let [first-apprentice (first siths)
        master (create-master-of first-apprentice)
        siths' (assoc-in siths [0 :master-id] (master :id))]
        (into [] (cons master siths'))))

(defn fill-siths [relationship siths]
  (let [fill-count (- 5 (count siths))
        fill-function (condp = relationship
                        :master prepend-master-to 
                        :apprentice append-apprentice-to)]
    (reduce (fn [siths' _] (fill-function siths'))
            siths
            (range fill-count))))

(defn first-pending-sith? [list]
  (let [pending-sith (filter (fn [{:keys [remote-id pending]}] 
                               (and (= true pending) 
                                    (not (= nil remote-id))))
                             list)]
    (if (empty? pending-sith)
      nil
      (first pending-sith))))k

(defn contains-sith-with-no-apprentice? [siths]
  (not-empty (filter (fn [{:keys [pending apprentice-remote-id]}] 
                         (and (not pending) (nil? apprentice-remote-id)))
                       siths)))

(defn contains-sith-with-no-master? [siths]
  (not-empty (filter (fn [{:keys [pending master-remote-id]}] 
                         (and (not pending) (nil? master-remote-id)))
                       siths)))
