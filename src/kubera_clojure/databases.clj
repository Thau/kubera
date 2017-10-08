(ns kubera-clojure.databases
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string])
  (:import (java.time.format DateTimeFormatter)
           (java.time LocalDateTime)))

(def run-pgdump (partial shell/sh "pg_dump" "-Fc"))

(defn- pg_dump!
  "Run pg_dump and return the resulting dump"
  [db-name]
  (-> db-name
      (run-pgdump)
      (get :out)))

(defn- db-name-from-url
  "Return the name of a database defined by a URL"
  [db-url]
  (last (string/split db-url #"/")))

(defn- now
  "Get a formatted timestamp suitable as a filename"
  []
  (.format (DateTimeFormatter/ISO_LOCAL_DATE_TIME) (LocalDateTime/now)))

(defn- filename
  "Get a filename based on the database name"
  [db-name]
  (str db-name (now) ".dump"))

(defn dump!
  "Dump a database based on a URL"
  [db-url]
  (do
    (println (str "Dumping " db-url))
    (let [filename (filename (db-name-from-url db-url))]
      (spit filename (pg_dump! db-url))
      filename)
    )
  )

