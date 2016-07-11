(def project 'iotivity.sensor/led)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [org.clojure/tools.namespace "0.3.0-alpha3"]
                            [org.clojure/core.async "0.2.385"]
                            ;; [iotivity/iotivity-api "1.1.1-SNAPSHOT"]
                            ;; [adzerk/boot-jar2bin "1.1.0" :scope "test"]
                            [adzerk/boot-test "RELEASE" :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/yourname/iot"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 repl {:port 48080})

(require '[adzerk.boot-test :refer [test]])
