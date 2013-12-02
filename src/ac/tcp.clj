(ns ac.tcp
  (:require [clojure.core.async :as async :refer [<! >! chan go go-loop]]
            [aleph.tcp :as tcp]
            [lamina.core :as lamina :refer [enqueue receive-all]]
            ))

(defn- make-handler
  [connections]
  (fn [aleph-ch client-info]
    (let [in (chan) out (chan)]
      (go (>! connections [client-info in out]))
      (go-loop [msg (<! out)] (enqueue aleph-ch msg) (recur (<! out)))
      (receive-all aleph-ch (fn [msg] (go (>! in msg)))))))

(defn start-tcp-server
  "Mirrors aleph.tcp.start-tcp-serves, takes an options hash
    :port - port to bind to
    :frame - framing of the tcp stream
    ...

  returns a channel that contains [client-info [in, out]] where
  in and out are both channels.

  `client-info` is a hash of client connection information
  `in` is composed of msg's off of the tcp stream
  `out` is a channel that's wired up to the outgoing socket pushing messages onto out will send them over the wire

  Example: 
    (defn -main
      []
      (let [opts {:port 1234 :frame (string :utf-8 :delimiters ["\r\n"])}
            connections (start opts)]
        (println "started")
        (go-loop [[client-info in out] (<! connections)]
          (println "connection from: " client-info)
          (go-loop [msg (<! in)]
            (println msg)
            (>! out msg)
            (recur (<! in)))
          (recur (<! connections)))))
  "
  [opts]
  (let [connections (chan)]
    (tcp/start-tcp-server (make-handler connections) opts)
    connections))
