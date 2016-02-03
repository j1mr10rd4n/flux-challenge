(ns ui-of-the-sith.util
  (:require [om.next :as om :refer-macros [defui]]))

(defn create-master-of
  [apprentice]
    {:sith/id (om/tempid)
     :sith/name (str "Master of " (apprentice :sith/name))
     :sith/homeworld "unknown"
     :sith/master-id nil
     :sith/apprentice-id (apprentice :sith/id)
     :sith/remote-id (apprentice :sith/master-remote-id)
     :sith/master-remote-id nil
     :sith/apprentice-remote-id (apprentice :sith/remote-id)})

(defn create-apprentice-of
  [master]
    {:sith/id (om/tempid)  
     :sith/name (str "Apprentice of " (master :sith/name))
     :sith/homeworld "unknown"
     :sith/master-id (master :sith/id)
     :sith/apprentice-id nil
     :sith/remote-id (master :sith/apprentice-remote-id)
     :sith/master-remote-id (master :sith/remote-id)
     :sith/apprentice-remote-id nil})

(defn append-apprentice-to [siths]
  (let [last-master (last siths)
        apprentice (create-apprentice-of last-master)
        siths' (assoc-in siths [(- (count siths) 1) :sith/apprentice-id] (apprentice :sith/id))]
        (into [] (conj siths' apprentice))))

(defn prepend-master-to [siths]
  (let [first-apprentice (first siths)
        master (create-master-of first-apprentice)
        siths' (assoc-in siths [0 :sith/master-id] (master :sith/id))]
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
      (first pending-sith))))

(defn contains-sith-with-no-apprentice? [siths]
  (not-empty (filter (fn [{:keys [pending apprentice-remote-id]}] 
                         (and (not pending) (nil? apprentice-remote-id)))
                       siths)))

(defn contains-sith-with-no-master? [siths]
  (not-empty (filter (fn [{:keys [pending master-remote-id]}] 
                         (and (not pending) (nil? master-remote-id)))
                       siths)))
