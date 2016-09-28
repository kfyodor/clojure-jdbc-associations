(set-env! :resource-paths #{"src"}
          :dependencies '[[org.clojure/clojure "1.9.0-alpha13"]
                          [clojure.java.jdbc "0.6.2-alpha3"]
                          [camel-snake-kebab "0.4.0"]

                          [adzerk/bootlaces "0.1.13" :scope "test"]
                          [adzerk/boot-test "1.1.1" :scope "test"]])

(def +version+ "0.1.0-SNAPSHOT")

(require '[adzerk.boot-test :as test]
         '[adzerk.bootlaces :refer :all])

(bootlaces! +version+ :dont-modify-paths? true)

(task-options!
 pom {:project 'io.thdr/associations
      :version +version+
      :description "Database + Transducers = Fun. Transducers-based db-associations for Clojure."
      :url "https://github.com/konukhov/clojure-jdbc-associations"
      :scm {:url "https://github.com/konukhov/clojure-jdbc-associations"}
      :license {"Eclipse Public License"
                "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask test []
  (merge-env! :resource-paths #{"test"})
  (test/test))
