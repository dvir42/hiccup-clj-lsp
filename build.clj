(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]))

(def class-dir "target/classes")
(def standalone-file "target/hiccup-clj-lsp-standalone.jar")
(def basis {:project "deps.edn"})

(defn clean [_]
  (b/delete {:path "target"}))

(defn- build-uberjar [opts]
  (clean opts)
  (println "Building uberjar...")
  (let [basis (b/create-basis (update basis :aliases concat (:extra-aliases opts)))
        src-dirs (into ["src" "resources"] (:extra-dirs opts))]
    (b/copy-dir {:src-dirs src-dirs
                 :target-dir class-dir})
    (b/compile-clj {:basis basis
                    :src-dirs src-dirs
                    :java-opts ["-Xmx2g" "-server"]
                    :class-dir class-dir})
    (b/uber {:class-dir class-dir
             :uber-file standalone-file
             :main 'hiccup-clj-lsp.main
             :basis basis})))

(defn- bin [opts]
  (let [jvm-opts (concat (:jvm-opts opts []) ["-Xmx2g" "-server"])]
    ((requiring-resolve 'deps-bin.impl.bin/build-bin)
     {:jar standalone-file
      :name "hiccup-clj-lsp"
      :jvm-opts jvm-opts
      :skip-realign true})))

(defn prod-cli [opts]
  (build-uberjar opts)
  (bin {}))
