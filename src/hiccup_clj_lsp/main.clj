(ns hiccup-clj-lsp.main
  (:require
   [hiccup-clj-lsp.server :as server])
  (:gen-class))

(defn run []
  (if (= :done @(server/run-lsp-io-server!)) 0 1))

(defn -main []
  (-> (run)
      (System/exit)))
