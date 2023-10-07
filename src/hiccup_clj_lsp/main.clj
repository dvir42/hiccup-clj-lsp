(ns hiccup-clj-lsp.main
  (:require
   [hiccup-clj-lsp.server :as server]
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]
   [clojure.java.io :as io])
  (:gen-class))

(def log-file-name "/home/dvir/code/hiccup-clj-lsp/log.txt")
(io/delete-file log-file-name :quiet)
(timbre/refer-timbre)
(timbre/merge-config! {:appenders {:println {:enabled? false}}})
(timbre/merge-config! {:appenders {:spit (appenders/spit-appender {:fname log-file-name})}})
(timbre/set-level! :info)

(defn run []
  (if (= :done @(server/run-lsp-io-server!)) 0 1))

(defn -main []
  (-> (run)
      (System/exit)))
