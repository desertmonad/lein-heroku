(ns leiningen.heroku
  (:require [clojure.java.io :as io]
            [clj-http.core :as http]
            [leiningen.help :as help])
  (:import (com.heroku.api.command CommandConfig)
           (com.heroku.api.command.login BasicAuthLogin)
           (com.heroku.api.connection Connection HttpClientConnection)
           (com.heroku.api HerokuAPI HerokuStack)))

(try ; Leiningen 2.0 compatibility
  (use '[leiningen.core :only [abort]])
  (catch Exception _
    (use '[leiningen.main :only [abort]])))

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

(defn app-api []
  ;; TODO: depends on current directory; should we pass project args around?
  (let [git-config (slurp (io/file ".git/config"))
        [_ app-name] (re-find #"git@heroku.com:(.+).git" git-config)]
    (.app (api) app-name)))

(defn ^{:no-project-needed true} heroku
  "Manage Heroku apps."
  [command & args]
  (let [command-ns (str "leiningen.heroku." (first (.split command ":")))
        _ (require (symbol command-ns))
        subtask (ns-resolve (symbol command-ns) (symbol command))]
    (try
      ;; TODO: help
      (apply subtask args)
      (catch Exception e
        (abort (.getMessage e))))))