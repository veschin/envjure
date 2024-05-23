(ns envjure
  (:gen-class)
  (:require [clojure.string :as str]))

;; TODO: read env
;; TODO: read yaml
;; TODO: read json
;; TODO: validate by schema
;; TODO: logs
;; TODO: generate vars
;; TODO: generate config
;; TODO: reload config fn
(declare normalize read-env! read-json! read-yaml!)

(defn normalize [key*]
  (let [camel-case-regex #"(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"]
    (as-> key* k
         (str/split k camel-case-regex)
         (str/join "-" k)
         (str/replace k #"_" "-")
         (str/lower-case k)
         (keyword k))))

(defn coerce [schema config]
  )

(defn read-env! [env-str]
  (->> (str/split-lines env-str)
       (mapv (comp (fn [[key val]] (when (every? not-empty [key val]) {(normalize key) val}))
                   #(str/split % #"\=")))
       (into {})))

(defn config []
  (read-env!)
  (read-json!)
  (read-yaml!)
  )

(defn -main
  ""
  [& args]
  )
