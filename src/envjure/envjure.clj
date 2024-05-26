(ns envjure
  (:gen-class)
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [yaml.core :as yaml]))

(defn normalize [key*]
  (let [camel-case-regex #"(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"]
    (as-> key* k
         (str/split k camel-case-regex)
         (str/join "-" k)
         (str/replace k #"_" "-")
         (str/lower-case k)
         (keyword k))))

(defn coerce [schema config]
  (let [aliases {:int #(Integer/parseInt %)}]
    (into {}
     (for [[key val] config
           :let [fn- (or (get schema key) identity)]]
       (try
         {key ((get aliases fn- fn-) val)}
         (catch Exception _
           (log/error (format "Key = %s with val = %s can't be coerced by = %s" key val fn-))
           :error))))))

(defmacro try-read! [type* & body]
  `(try
     (log/info (format "Reading config of type = %s" ~type*) )
     ~@body
     (catch Exception e#
       (log/error (format "Error while reading config of type = %s\n %s" ~type* e#))
       :error)))

(defn read-env! [env-str]
  (try-read! :env
    (->> (str/split-lines env-str)
         (mapv (comp (fn [[key val]] (when (every? not-empty [key val]) {(normalize key) val}))
                     #(str/split % #"\=")))
         (into {}))))

(defn read-json! [json-str]
  (try-read! :json
    (-> json-str
        (json/parse-string)
        (update-keys normalize))))

(defn read-yaml! [yaml-str]
  (try-read! :yaml
    (-> yaml-str
        (yaml/parse-string :keywords false)
        (update-keys normalize))))

(defn read-edn! [edn-str]
  (try-read! :edn
    (-> edn-str
        (read-string)
        (update-keys (comp normalize name)))))

(defmacro gen! [config*]
  `(do
     ~(list 'def 'config- config*)
     ~@(for [[key# val#] config*]
         (list 'def (-> key# name symbol) val#))))
