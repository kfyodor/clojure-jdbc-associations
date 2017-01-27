(ns thdr.associations.test
  (:require  [thdr.associations.jdbc :as db]
             [clojure.tools.logging :as log]
             [clojure.test :as t]
             [clojure.java.jdbc :as jdbc]))

(def db-spec
  {:connection-uri
   (or (System/getenv "JDBC_URL")
       (str "jdbc:log4jdbc:postgresql://localhost:5432/test_jdbc_associations?user="
            (System/getenv "USER")))})

(def conn (atom nil))

(def test-tables
  [[:labels   [[:id "SERIAL" "PRIMARY KEY"]
               [:name "TEXT"]]]
   [:bands    [[:id "SERIAL" "PRIMARY KEY"]
               [:name "TEXT"]]]
   [:records  [[:id "SERIAL" "PRIMARY KEY"]
               [:name "TEXT"]
               [:band_id :int]]]
   [:releases [[:id "SERIAL" "PRIMARY KEY"]
               [:format "TEXT"]
               [:record_id :int]
               [:year :int]
               [:label_id :int]]]])

;; cases

;; one to one
;; one to many
;; many to many
;; one to many through
;; polymorphic

;; release belongs to label
;; label has many releases
;; label has many records through releases
;; release belongs to record
;; band has many records
;; band has many releases through records


(def data
  {:bands
   [{:id 1
     :name "Women"}
    {:id 2
     :name "Preoccupations"}
    {:id 3
     :name "Cindy Lee"}]

   :labels
   [{:id 1
     :name "Jagjaguwar"}
    {:id 2
     :name "Flemish Eye"}
    {:id 3
     :name "CCQSK Records"}]

   :records
   [{:id 1
     :name "Women"
     :band_id 1}
    {:id 2
     :name "Public Strain"
     :band_id 1}
    {:id 3
     :name "Service Animal"
     :band_id 1}]

   :releases
   [{:id 1
     :record_id 1
     :format "CD"
     :year 2009
     :label_id 1}
    {:id 2
     :record_id 2
     :format "LP"
     :year 2009
     :label_id 1}
    {:id 3
     :record_id 1
     :format "CD"
     :year 2009
     :label_id 2}
    {:id 4
     :record_id 2
     :format "LP"
     :year 2009
     :label_id 2}]})

(defn create-tables!
  [db]
  (jdbc/db-do-commands db
   (map (fn [[table-name specs]]
          (jdbc/create-table-ddl table-name specs)) test-tables)))

(defn populate-data!
  [db]
  (doseq [[table-name data] data]
    (jdbc/insert-multi! db table-name data)))

(defn wrap-tests [f]
  (jdbc/with-db-transaction [tr db-spec]
    (jdbc/db-set-rollback-only! tr)
    (reset! conn tr)
    (create-tables! tr)
    (populate-data! tr)
    (f)))
