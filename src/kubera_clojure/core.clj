(ns kubera-clojure.core
  (:gen-class)
  (:require [kubera-clojure.databases :as databases]
            [kubera-clojure.dropbox :as dropbox]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.jobs :as jobs]
            [clojurewerkz.quartzite.triggers :as triggers]
            [clojurewerkz.quartzite.schedule.cron :as cron]
            )
  (:import (java.time.format DateTimeFormatter)
           (java.time LocalDateTime)))

(defn databases
  []
  (-> (System/getenv "DATABASES")
      (string/split #" ")
      ))

(jobs/defjob DumpJob
             [ctx]
             (let [filenames (map databases/dump! (databases))]
               (run! dropbox/upload-file filenames)
               (run! io/delete-file filenames)
               ))

(defn scheduler
  []
  (-> (qs/initialize) qs/start))

(def job
  (jobs/build (jobs/of-type DumpJob)))

(defn schedule
  []
  (cron/schedule (cron/cron-schedule (System/getenv "SCHEDULE"))))

(defn trigger
  []
  (triggers/build (triggers/start-now) (triggers/with-schedule (schedule)) ))

(defn -main
  [& args]
  (qs/schedule (scheduler) job (trigger)))
