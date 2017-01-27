(ns thdr.associations.jdbc
  (:require [clojure.java.jdbc :as jdbc]
            [camel-snake-kebab.core :refer [->kebab-case ->snake_case]]
            [clojure.string :as str]))

(defn- get-name [kw]
  (-> kw name ->snake_case))

(defn- table-str [t]
  (#'jdbc/table-str t identity))

(defn- as-sql-name [c]
  (#'jdbc/as-sql-name identity c))

(defn- gen-in-query
  [table column ids]
  (concat [(str "select * from "
                 (table-str table)
                 " where "
                 (as-sql-name column)
                 " in ("
                 (str/join ", " (repeat (count ids) "?"))
                 ")")]
          ids))

(defn find-by-ids
  ([db table column ids]
   (find-by-ids db table column ids {}))
  ([db table column ids opts]
   (jdbc/query db
               (gen-in-query table column ids)
               opts)))

(defn group-by-pkey
  [{:keys [pkey] :as opts} result]
  (into {}
        (map #(vector (pkey %) %) result)))

(defn group-by-fkey
  [{:keys [fkey] :as opts} result]
  (group-by fkey result))

(defn find-by-ids->group-by-pkey
  [db table column ids opts]
  (find-by-ids db
               table
               column
               ids
               {:result-set-fn (partial group-by-pkey opts)}))

(defn find-by-ids->group-by-fkey
  [db table column ids opts]
  (find-by-ids db
               table
               column
               ids
               {:result-set-fn (partial group-by-fkey opts)}))
