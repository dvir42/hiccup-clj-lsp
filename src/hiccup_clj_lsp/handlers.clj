(ns hiccup-clj-lsp.handlers
  (:require [hiccup-clj-lsp.html :as html]
            [hiccup-clj-lsp.db :as db]
            [clojure.string :as string]
            [taoensso.timbre :as timbre]))

(defn did-open [{:keys [text-document]}]
  (let [{:keys [uri text]} text-document]
    (timbre/info "Document open" uri)
    (db/upsert-file uri text)))

(defn did-change [{:keys [text-document content-changes]}]
  (let [{uri :uri} text-document
        {text :text} (first content-changes)]
    (timbre/debug "Document change" uri)
    (db/upsert-file uri text)))

(defn did-save [{:keys [text-document text]}]
  (let [{uri :uri} text-document]
    (timbre/info "Document save" uri)
    (db/upsert-file uri text)))

(defn did-close [{:keys [text-document]}]
  (let [{uri :uri} text-document]
    (timbre/info "Document close" uri)
    (db/remove-file uri)))

(defn- get-parent-html-element [text line column]
  (as-> text a
    (string/split-lines a)
    (vec (take (+ line 1) a))
    (conj (pop a) (subs (last a) 0 column))
    (string/join "\n" a)
    (let [elements (string/join "|" (keys html/html-elements))]
      (re-find
       (re-pattern
        (format "(?s)\\[(%s)(?=[\\s\\(\\{\\[,])[^}\\]]*?\\{[^}\\]]*$" elements)) a))
    (last a)
    (keyword (apply str (next a)))))

(defn- format-completions [completions]
  (map (fn [e] {:label (str e) :kind :keyword}) completions))

(defn completion [{:keys [text-document position context] :as params}]
  (when (and (= (:trigger-kind context) 2) (= (:trigger-character context) ":"))
    (let [{:keys [uri]} text-document
          {line :line column :character} position
          text (db/get-file uri)]
      (timbre/debug "Completion file:" uri "line:" line "column:" column "text:" text)
      (format-completions
       (concat
        (get html/html-elements
             (get-parent-html-element text line column))
        (keys html/html-elements))))))
