(ns hiccup-clj-lsp.completion
  (:require [clojure.string :as string]
            [hiccup-clj-lsp.html :as html]))

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

(defn completions [text line column]
  (concat
   (get html/html-elements
        (get-parent-html-element text line column))
   (keys html/html-elements)))
