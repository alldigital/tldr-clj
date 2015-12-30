(ns tldr-clj.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client])
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
  (let [response (client/get (str remote path))
        status-code (response :status)
        body (response :body)]
    (when (not= status-code 200)
      (throw (Exception. (format "Unexpected status code: %s" status-code))))
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

(defn -main
  ([name]
   (-main name false))
  ([name u]
    (let [index (get (json/read-str (get-page "/index.json" u)) "commands")
          command (some #(when (= (get % "name") name) %) index)
          platform (choose-platform command)]
      (if (not= command nil)
        (println (get-page (str "/" platform "/" name ".md") u))
        (println "NOT FOUND")))))
