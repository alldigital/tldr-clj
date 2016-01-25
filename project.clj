(defproject tldr-clj "1.0.0"
  :description "TL;DR"
  :url "https://github.com/iovxw/tldr-clj"
  :license {:name "The MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.3"]]
  :main ^:skip-aot tldr-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
