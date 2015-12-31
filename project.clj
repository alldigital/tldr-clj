(defproject tldr-clj "1.0.0"
  :description "TL;DR"
  :url "http://iovxw.net"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.3"]]
  :main ^:skip-aot tldr-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
