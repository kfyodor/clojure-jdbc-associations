(ns thdr.associations.core
  "A sneaky way to include belongs-to associations
   via transducers."
  (:require [thdr.associations.jdbc :as db]
            [clojure.tools.logging :as log]
            [clojure.spec :as s]))

;; :db.cardinality/one
;; :db.cardinality/many

(defmacro defassociation
  ;; todo: validate keys and compute defaults from name
  ;; todo: :select options to specify selectable association keys
  ;; https://github.com/r0man/inflections-clj/blob/master/src/inflections/core.cljc
  [name form]
  `(def ~name ~form))

(defn- fetch-fn-for [coll {:keys [cardinality]}]
  (case cardinality
    :one (db/find-by-ids->group-by-pkey)
    :many (db/find-by-ids->group-by-fkey)))

(defn- prepare-coll
  [coll]
  (if (associative? coll)
    (vals coll)
    coll))

(defn- fetch [fetch-fn db table column opts ids]
  (fetch-fn db table column ids opts))

(defmulti fetch-association!
  (fn [_ _ {:keys [cardinality] :as assoc-map}]
    cardinality))

(defmethod fetch-association! :one
  [db coll {:keys [pkey fkey table] :as assoc-map}]
  (some->> coll
           (prepare-coll)
           (map fkey)
           (set)
          ;; too dirty: think about interface
           (fetch db/find-by-ids->group-by-pkey db table pkey assoc-map)))

(defmethod fetch-association! :many
  [db coll {:keys [pkey fkey table] :as assoc-map}]
  (some->> coll
           (prepare-coll)
           (map pkey)
           (fetch db/find-by-ids->group-by-fkey db table fkey assoc-map)))

(defn- fetch-all-associations!
  "Recursively fetches nested associations"
  [db-conn coll assocs]
  (next
   (reduce (fn [colls a]
             (conj colls
                   (fetch-association! db-conn (last colls) a)))
           [coll]
           assocs)))

(defn- build-key-path
  [{:keys [fkey pkey cardinality]} path]
  (let [new-path (conj path
                       (case cardinality
                         :one fkey
                         :many pkey))]
    new-path))

(defn- null-entity
  [{:keys [cardinality]}]
  (case cardinality
    :one nil
    :many []))

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
                 (let [{:keys [fkey key pkey] :as a} (first assocs)
                       records (first colls)
                       record (or (some->> (build-key-path a path)
                                           (get-in input)
                                           (get records))
                                  (null-entity a))
                       path (conj path key)]
                   (recur (rest assocs)
                          (rest colls)
                          (assoc-in input path record)
                          path))
                 input))))))))

(defn with-association
  [coll db-conn assoc-map]
  (transduce (includes db-conn coll assoc-map)
             conj
             coll))

(defn with-associations ;; todo ability to provide additional transducers?
  "Fetches and merges associated records into coll."
  [coll db-conn assocs]
  (let [xf (apply comp
                  (map (partial includes db-conn coll) assocs))]
    (transduce xf conj coll)))
