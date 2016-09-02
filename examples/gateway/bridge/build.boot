(def project 'iotivity-bridge)
(def version "0.1.0-SNAPSHOT")

(set-env! :source-paths   #{"src" "test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [org.clojure/tools.namespace "0.3.0-alpha3"]
                            [org.clojure/core.async "0.2.385"]
                            [iotivity/iotivity-api "1.1.3-SNAPSHOT"]
                            [aleph "0.4.2-alpha1"]
                            ;; [aleph "0.4.1"]
                            [gloss "0.2.5"]
                            [compojure "1.3.3"]
                            [fogus/ring-edn "0.3.0"]
                            [adzerk/boot-jar2bin "1.1.0" :scope "test"]
                            [adzerk/boot-test "RELEASE" :scope "test"]])

(require '[adzerk.boot-jar2bin :refer :all])

(def jlp (System/getenv "JLP"))
(println (str "JLP: " jlp))

(def jvm-opts #{(str "-Djava.library.path=" jlp)
                "-Xms512m"
                "-Xmx1024m"})

(task-options!
 aot {:namespace   #{'iotivity.example.gateway.bridge.core}}
 bin {:jvm-opt jvm-opts}
 exe {:jvm-opt jvm-opts}
 jar {:main        'iotivity.example.gateway.bridge.core
      :file        (str "iot-gw-bridge-" version "-standalone.jar")}
 repl {:port 8088}
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/yourname/iot"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot) (pom) (uber) (jar) (target :dir dir))))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (require '[iot.core :as app])
  (apply (resolve 'app/-main) args))

(require '[adzerk.boot-test :refer [test]])
