(ns hiccup-clj-lsp.db
  (:require [taoensso.timbre :as timbre]))

(def ^:private db (atom {}))

(defn upsert-file [filename text]
  (timbre/debug (str "Upsert file: " filename " text: " text))
  (swap! db assoc filename text))

(defn remove-file [filename]
  (timbre/debug (str "Remove file: " filename))
  (swap! db dissoc filename))

(defn get-file [filename]
  (get @db filename))
