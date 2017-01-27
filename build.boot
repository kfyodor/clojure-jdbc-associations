(set-env! :resource-paths #{"src"}
          :dependencies '[[org.clojure/clojure "1.9.0-alpha14"]
                          [org.clojure/java.jdbc "0.7.0-alpha1"]
                          [camel-snake-kebab "0.4.0"]

                          [org.postgresql/postgresql "9.4.1212"]

                          [org.clojure/tools.logging "0.3.1"]
                          [org.slf4j/log4j-over-slf4j "1.7.7"]
                          [ch.qos.logback/logback-classic "1.1.3"
                           :exclusions [org.slf4j/slf4j-api]]
                          [org.bgee.log4jdbc-log4j2/log4jdbc-log4j2-jdbc4 "1.16"]

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

(System/setProperty "log4jdbc.spylogdelegator.name"
                    "net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator")

(deftask test []
  (merge-env! :source-paths  #{"test"})
  (merge-env! :resource-paths #{"test_resources"})
  (test/test))
