(ns leiningen.heroku.util
  (:require [clojure.java.io :as io])
  (:import (com.heroku.api.command.login BasicAuthLogin)
           (com.heroku.api.connection HttpClientConnection)
           (com.heroku.api HerokuAPI)))

(defn- space-key [k longest-key]
  (apply str k ": " (repeat (- longest-key (count k)) " ")))

(defn print-map [m]
  (let [longest-key (apply max (map count (keys m)))]
    (doseq [[k v] m]
      (println (space-key k longest-key) v))))

(defn- read-password []
  (.readPassword (System/console) "%s" (into-array ["Password: "])))

(defn- credentials-file []
  (io/file (System/getProperty "user.home") ".heroku" "credentials"))

(defn- get-credentials []
  (-> (credentials-file)
      (slurp)
      (.split "\n")))

(defn login [_ & args]
  (println "Enter your Heroku credentials.")
  (print "Email: ") (flush)
  (let [email (read-line)
        password (read-password)
        command (BasicAuthLogin. email password)]
    ;; TODO: save to credentials-file
    ))

(defn api []
  (when-not (.exists (credentials-file))
    (login))
  (let [[email key] (get-credentials)]
    (-> (BasicAuthLogin. email key)
        (HttpClientConnection.)
        (HerokuAPI/with))))

(defn current-app-name []
  ;; TODO: depends on current directory; should we pass project args around?
  (->> (io/file "/home/phil/src/clojars" ".git/config")
       slurp
       (re-find #"git@heroku.com:(.+).git")
       second))

(defn app-api []
  (.app (api) (current-app-name)))