(ns make
  (:require
   [babashka.deps :as deps]
   [babashka.fs :as fs]
   [babashka.process :as p]))

(defn- clj! [dir cmd]
  (-> (deps/clojure cmd {:dir dir, :inherit true})
      (p/check)))

(defn- build [dir tool] (clj! dir ["-T:build" tool]))

(defn- mv-here [file]
  (fs/move file "." {:replace-existing true}))

(defn prod-cli
  []
  (build "." "prod-cli")
  (mv-here (fs/path "." "hiccup-clj-lsp")))
