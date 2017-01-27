(ns thdr.associations.core-test
  (:require [thdr.associations
             [core :refer [includes
                           defassociation
                           with-association
                           with-associations]]
             [test :refer [wrap-tests conn]]]
            [clojure.test :refer :all]
            [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]))

(use-fixtures :once wrap-tests)

(defassociation release->label
  {:table :labels
   :pkey :id
   :fkey :label_id
   :cardinality :one
   :key :label})

(defassociation release->record
  {:table :records
   :pkey :id
   :fkey :record_id
   :cardinality :one
   :key :record})

(defassociation record->band
  {:table :bands
   :pkey :id
   :fkey :band_id
   :key :band
   :cardinality :one})


(defassociation record->releases
  {:table :releases
   :pkey :id
   :fkey :record_id
   :cardinality :many
   :key :releases})

(defn select-all [table]
  (jdbc/query @conn [(str "select * from " (name table))]))

(deftest one-to-one-associations-test
  (testing "simple one to one"
    (is (= [{:id 1
             :name "Women"
             :band_id 1
             :band {:id 1
                    :name "Women"}}
            {:id 2
             :name "Public Strain"
             :band_id 1
             :band {:id 1
                    :name "Women"}}
            {:id 3
             :name "Service Animal"
             :band_id 1
             :band {:id 1
                    :name "Women"}}]
           (-> (select-all :records)
               (with-association @conn record->band))))

    (is (= [{:id 1
             :record_id 1
             :record {:id 1
                      :name "Women"
                      :band_id 1}
             :format "CD"
             :year 2009
             :label_id 1
             :label {:id 1
                     :name "Jagjaguwar"}}
            {:id 2
             :record_id 2
             :record {:id 2
                      :name "Public Strain"
                      :band_id 1}
             :format "LP"
             :year 2009
             :label_id 1
             :label {:id 1
                     :name "Jagjaguwar"}}
            {:id 3
             :record_id 1
             :record {:id 1
                      :name "Women"
                      :band_id 1}
             :format "CD"
             :year 2009
             :label_id 2
             :label {:id 2
                     :name "Flemish Eye"}}
            {:id 4
             :record_id 2
             :record {:id 2
                      :name "Public Strain"
                      :band_id 1}
             :format "LP"
             :year 2009
             :label_id 2
             :label {:id 2
                     :name "Flemish Eye"}}]
           (-> (select-all :releases)
               (with-associations @conn [release->record release->label])))))
  (testing "nested one to one"
    (is (= [{:id 1
             :record_id 1
             :record {:id 1
                      :name "Women"
                      :band_id 1
                      :band {:id 1
                             :name "Women"}}
             :format "CD"
             :year 2009
             :label_id 1}
            {:id 2
             :record_id 2
             :record {:id 2
                      :name "Public Strain"
                      :band_id 1
                      :band {:id 1
                             :name "Women"}}
             :format "LP"
             :year 2009
             :label_id 1}
            {:id 3
             :record_id 1
             :record {:id 1
                      :name "Women"
                      :band_id 1
                      :band {:id 1
                             :name "Women"}}
             :format "CD"
             :year 2009
             :label_id 2}
            {:id 4
             :record_id 2
             :record {:id 2
                      :name "Public Strain"
                      :band_id 1
                      :band {:id 1
                             :name "Women"}}
             :format "LP"
             :year 2009
             :label_id 2}]
           (-> (select-all :releases)
               (with-association @conn [release->record record->band]))
           (-> (select-all :releases)
               (with-associations @conn [[release->record record->band]]))))))

(deftest one-to-many-association-test
  (testing "simple one to many"
    (is (= [{:id 1
             :name "Women"
             :band_id 1
             :releases [{:id 1
                         :record_id 1
                         :format "CD"
                         :year 2009
                         :label_id 1}
                        {:id 3
                         :record_id 1
                         :format "CD"
                         :year 2009
                         :label_id 2}]}
            {:id 2
             :name "Public Strain"
             :band_id 1
             :releases [{:id 2
                         :record_id 2
                         :format "LP"
                         :year 2009
                         :label_id 1}
                        {:id 4
                         :record_id 2
                         :format "LP"
                         :year 2009
                         :label_id 2}]}
            {:id 3
             :name "Service Animal"
             :band_id 1
             :releases []}]
           (-> (select-all :records)
               (with-association @conn record->releases))))))
