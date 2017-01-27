(ns thdr.associations.spec
  (:require [clojure.spec :as s]))

(s/def ::cardinality #{::one ::many})
(s/def ::fkey keyword?)
(s/def ::pkey keyword?)
(s/def ::table keyword?)
(s/def ::name keyword?)

(s/def ::association (s/keys :req [::name]
                             :opt [::cardinality ::fkey ::pkey ::table]))
