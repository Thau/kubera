(ns kubera-clojure.dropbox
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def chunk-size (-> 150 (* 1024) (* 1024)))

(defn- bearer
  []
  (str "Bearer " (:dropbox-token env)))

(defn- headers
  [api-args]
  {"Authorization"   (bearer)
   "Dropbox-API-Arg" api-args
   "Content-Type"    "application/octet-stream"})

(def session_start_api_args (json/write-str {:close false}))

(defn- upload-session-start
  [to-write]
  (client/post
    "https://content.dropboxapi.com/2/files/upload_session/start"
    {:headers (headers session_start_api_args)
     :body    to-write}))

(defn- append-api-args
  [session-id offset]
  (-> {:cursor {:session_id session-id :offset offset}
       :close  false}
      json/write-str))

(defn- upload-session-append
  [session-id to-write offset]
  (client/post
    "https://content.dropboxapi.com/2/files/upload_session/append_v2"
    {:headers          (headers (append-api-args session-id offset))
     :throw-exceptions false
     :body             to-write}))

(defn- session-finish-api-args
  [session-id filename offset]
  (-> {:cursor {:session_id session-id :offset offset}
       :commit {:path filename :mode "add" :autorename true :mute false}}
      json/write-str))

(defn- upload-session-finish
  [session-id offset filename]
  (client/post
    "https://content.dropboxapi.com/2/files/upload_session/finish"
    {:headers          (headers (session-finish-api-args session-id (str "/" filename) offset))
     :throw-exceptions false}))

(defn- read-file-chunk
  [in last-n last-offset]
  (let [buf (byte-array chunk-size)
        n   (.read in buf)]
    {:buf buf :n n :offset (+ last-offset last-n)}))

(defn- read-full-file
  [in read-seq last-n last-offset]
  (if (= last-n -1)
    (drop-last read-seq)
    (let [res    (read-file-chunk in last-n last-offset)
          n      (:n res)
          offset (:offset res)]
      (recur in (conj read-seq res) n offset))))

(defn- read-file
  [filename]
  (with-open [in (io/input-stream (io/file filename))]
    (read-full-file in [] 0 0)))

(defn- byte-array-chunk
  [b-arr size]
  (-> b-arr
      vec
      (subvec 0 size)
      byte-array))

(defn- upload-append
  [session-id file-seq]
  (let [{buf :buf offset :offset n :n} file-seq
        to-write (byte-array-chunk buf n)]
    (upload-session-append session-id to-write offset)))

(defn upload-file
  "Upload a file to Dropbox"
  [filename]
  (let [file-seq     (read-file filename)
        {:keys [buf n]} (first file-seq)
        rest-seq     (next file-seq)
        to-write     (byte-array-chunk buf n)
        start-result (upload-session-start to-write)
        body         (json/read-str (get start-result :body))
        session-id   (get body "session_id")
        total-n      (reduce + (map :n file-seq))]
    (run! (partial upload-append session-id) rest-seq)
    (upload-session-finish session-id total-n filename)))
