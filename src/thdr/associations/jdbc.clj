(ns thdr.associations.jdbc
  (:require [clojure.java.jdbc :as jdbc]
            [camel-snake-kebab.core :refer [->kebab-case ->snake_case]]))

(defn- get-name [kw]
  (-> kw name ->snake_case))

(defn gen-find-query [{:keys [pkey table]} ids]
  [(str "select * from "
        (get-name table)
        " where "
        (get-name pkey)
        " in (?)")
   ids])

(defn find-by-ids->map ;; check jdbc version here
  [db-conn assoc ids]
  (jdbc/query db-conn
              (gen-find-query assoc ids)
              {:result-set-fn (fn [r]
                                (into {} (map #(vector (key %) %) r)))
               :identifiers ->kebab-case}))
