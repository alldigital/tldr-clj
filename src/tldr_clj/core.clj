(ns tldr-clj.core
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]])
  (:alias io clojure.java.io)
  (:gen-class))

(def remote "https://raw.githubusercontent.com/tldr-pages/tldr/master/pages")

(def cache-dir (str (System/getProperty "java.io.tmpdir") "/tldr-clj"))

(defn to-abs-path [file]
  (str cache-dir file))

(defn write-cache [path data]
  (let [file (io/file (to-abs-path path))
        parent (.getParentFile file)]
    (when-not (.exists parent)
      (.mkdirs parent))
    (spit file data)))

(defn cache-valid? [file]
  ; 5 day
  (< (- (System/currentTimeMillis) (.lastModified file)) 432000000))

(defn read-cache [path]
  (let [file (io/file (to-abs-path path))]
    (if (and (.exists file) (cache-valid? file))
      (slurp file)
      nil)))

(defn get-update [path]
  (let [body (slurp (str remote path))]
    (write-cache path body)
    body))

(defn get-page [page update-cache]
  (let [cache (read-cache page)]
    (if (or update-cache (= cache nil))
      (get-update page)
      cache)))

(def os (case (System/getProperty "os.name")
          "Linux"    "linux"
          "Mac OS X" "osx"
          "Mac OS"   "osx"
          "Sun OS"   "sunos"
          "common"))

(defn choose-platform [command]
  (let [platforms (get command "platform")]
    (if (some #(= % os) platforms)
      os
      (first platforms))))

(defn color [code]
  (format "\033[%sm" code))

(def reset-color (color 0))
(def bold (color 1))
(def red (color 31))
(def green (color 32))
(def white (color 37))
(def on-blue (color 44))
(def on-grey (color 40))

(defn r-cmd [cmd]
  (-> (str "  " on-grey red cmd)
      (string/replace "{{" white)
      (string/replace "}}" red)
      (str reset-color)))

(defn render [markdown]
  (->> (map (fn [line]
              (some #(let [r (re-find (first %) line)]
                       (when r ((second %) r)))
                    [[#"`(.*)`" #(r-cmd (second %))]
                     [#"#\s?(.*)" #(str on-blue white bold (second %))]
                     [#">\s?(.*)" #(str on-blue white (second %))]
                     [#"-\s?(.*)" #(str on-blue green "● " (second %))]
                     [#".*" #(str reset-color on-blue %)]]))
            (string/split-lines markdown))
       (string/join \newline)
       (format (str "%s" reset-color))))

(defn tldr
  ([name]
   (tldr name false))
  ([name u]
    (let [index (get (json/read-str (get-page "/index.json" u)) "commands")
          command (some #(when (= (get % "name") name) %) index)
          platform (choose-platform command)]
      (if (not= command nil)
        (println (render (get-page (str "/" platform "/" name ".md") u)))
        (println "NOT FOUND")))))

(defn help [summary]
  (->> ["Usage: tldr [options] command"
        ""
        "Options:"
        summary]
       (string/join \newline)))

(defn -main [& args]
  (let [opts (parse-opts args [["-u" "--update" "Update cache"]
                               ["-h" "--help"]])
        arguments (opts :arguments)
        options (opts :options)
        u (true? (options :update))
        h (true? (options :help))]
    (if (or h (= (count arguments) 0))
      (println (help (opts :summary)))
      (doseq [command arguments]
        (tldr command u)))))
