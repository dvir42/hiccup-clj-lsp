(ns hiccup-clj-lsp.server
  (:require [lsp4clj.server :as lsp.server]
            [lsp4clj.coercer :as coercer]
            [clojure.java.io :as io]
            [clojure.core.async :as async]
            [lsp4clj.io-server :as lsp.io-server]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [hiccup-clj-lsp.handlers :as handlers]))

(def log-file-name "/home/dvir/code/hiccup-clj-lsp/log.txt")
(io/delete-file log-file-name :quiet)
(timbre/refer-timbre)
(timbre/merge-config! {:appenders {:println {:enabled? false}}})
(timbre/merge-config! {:appenders {:spit (appenders/spit-appender {:fname log-file-name})}})
(timbre/set-level! :debug)

(defn- monitor-server-logs [log-ch]
  (async/go-loop []
    (when-let [[level args] (async/<! log-ch)]
      (timbre/log level args)
      (recur))))

(defn start-server! [server]
  (let [components {:server server}]
    (timbre/info "[SERVER]" "Starting server...")
    (monitor-server-logs (:log-ch server))
    (lsp.server/start server components)))

(defn run-lsp-io-server! []
  (lsp.server/discarding-stdout
   (let [log-ch (async/chan (async/sliding-buffer 20))
         server (lsp.io-server/stdio-server {:log-ch log-ch
                                             :trace-ch log-ch
                                             :trace-level "verbose"})]
     (start-server! server))))

(defn log! [level args fmeta]
  (timbre/log! level :p args {:?line (:line fmeta)
                              :?file (:file fmeta)
                              :?ns-str (:ns-str fmeta)}))

(defmacro conform-or-log [spec value]
  (let [fmeta (assoc (meta &form)
                     :file *file*
                     :ns-str (str *ns*))]
    `(coercer/conform-or-log
      (fn [& args#]
        (log! :error args# ~fmeta))
      ~spec
      ~value)))

(defn capabilities []
  (conform-or-log
   ::coercer/server-capabilities
   {:completion-provider {:resolve-provider true :trigger-characters [":"]}}))

(defmethod lsp.server/receive-request "initialize" [_ _ _]
  (timbre/info "Initializing...")
  {:capabilities (capabilities)})

(defmethod lsp.server/receive-notification "initialized" [_ _ _])

(defmethod lsp.server/receive-notification "textDocument/didOpen" [_ _ _])

(defmethod lsp.server/receive-notification "textDocument/didChange" [_ _ _])

(defmethod lsp.server/receive-notification "textDocument/didSave" [_ _ _])

(defmethod lsp.server/receive-notification "textDocument/didClose" [_ _ _])

(defn- exit [server]
  (timbre/info "Exiting...")
  (lsp.server/shutdown server)
  (shutdown-agents)
  (System/exit 0))

(defmethod lsp.server/receive-notification "exit" [_ {:keys [server]} _]
  (exit server))

(defmethod lsp.server/receive-request "shutdown" [_ _ _])

(defmethod lsp.server/receive-request "textDocument/completion" [_ _ params]
  (->> params
       (handlers/completion)
       (conform-or-log ::coercer/completion-items-or-error)))

(defmethod lsp.server/receive-request "completionItem/resolve" [_ _ _])
