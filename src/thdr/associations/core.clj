(ns thdr.associations.core
  "A sneaky way to include belongs-to associations
   via transducers."
  (:require [thdr.associations.jdbc :as db]))

(defmacro defassociation
  ;; todo: validate keys and compute defaults from name
  ;; todo: :select options to specify selectable association keys
  ;; https://github.com/r0man/inflections-clj/blob/master/src/inflections/core.cljc
  [name form]
  `(def ~name ~form))

(defn- fetch-association! ;; todo: pkey
  "Fetches association and returns map of pkey -> record"
  [db-conn coll {:keys [pkey fkey table] :as a}]
  (let [coll (if (associative? coll)
               (vals coll)
               coll)]
    (some->> coll
             (map fkey)
             (seq)
             (db/find-by-ids->map db-conn table))))

(defn- fetch-all-associations!
  "Recursively fetches nested associations"
  [db-conn coll assocs]
  (next
   (reduce (fn [colls a]
             (conj colls
                   (fetch-association! db-conn (last colls) a)))
           [coll]
           assocs)))

(defn includes
  "Returns a transducer which fetches association on init
   and assocs associated records (nested assocs are possible)
   into coll."
  [db-conn coll association]
  (fn [xf]
    (let [association (if (vector? association) association [association])
          colls (fetch-all-associations! db-conn coll association)]
      (fn
        ([]
         (xf))
        ([result]
         (xf result))
        ([result input]
         (xf result
             (loop [assocs association
                    colls  colls
                    input  input
                    path   []]
               (if (seq assocs)
                 (let [{:keys [fkey key pkey]} (first assocs)
                       records (first colls)
                       record (->> (get-in input (conj path fkey)) (get records))
                       path (conj path key)]
                   (recur (rest assocs)
                          (rest colls)
                          (assoc-in input path record)
                          path))
                 input))))))))

(defn with-associations ;; todo ability to provide additional transducers?
  "Fetches and merges associated records into coll."
  [coll db-conn assocs]
  (let [xf (apply comp
                  (map (partial includes db-conn coll) assocs))]
    (transduce xf conj coll)))
