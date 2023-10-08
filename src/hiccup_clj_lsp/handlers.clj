(ns hiccup-clj-lsp.handlers
  (:require [hiccup-clj-lsp.html :as html]
            [hiccup-clj-lsp.db :as db]
            [taoensso.timbre :as timbre]))

(defn did-open [{:keys [text-document]}]
  (let [{:keys [uri text]} text-document]
    (timbre/info "Document open" uri)
    (db/upsert-file uri text)))

(defn did-change [{:keys [text-document content-changes]}]
  (let [{uri :uri} text-document
        {text :text} (first content-changes)]
    (timbre/info "Document change" uri)
    (db/upsert-file uri text)))

(defn did-save [{:keys [text-document text]}]
  (let [{uri :uri} text-document]
    (timbre/info "Document save" uri)
    (db/upsert-file uri text)))

(defn did-close [{:keys [text-document]}]
  (let [{uri :uri} text-document]
    (timbre/info "Document close" uri)
    (db/remove-file uri)))

(defn completion [{:keys [text-document position context] :as params}]
  (timbre/info "Completion:" params)
  (when (and (= (:trigger-kind context) 2) (= (:trigger-character context) ":"))
    (let [{:keys [uri]} text-document
          {line :line index :character} position
          text (db/get-file uri)]
      (timbre/debug "Completion file:" uri "text:" text)
      (map (fn [e] {:label (str e) :kind :keyword}) (keys html/html-elements)))))
