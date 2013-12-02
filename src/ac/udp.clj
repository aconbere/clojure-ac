(ns ac.udp
  (:require [clojure.core.async :as async :refer [<! >! chan go go-loop]]
            [aleph.udp :as udp]
            [lamina.core :as lamina :refer [enqueue receive-all]]))

(defn udp-socket
  "Mirrors the aleph.udp.udp-socket interfaces takes an options hash
    :port - port to bind to, if none is given only listen
    :frame - must encode the entire datagram body

  Example: 
    (defn -main
      []
      (let [opts {:port 1234 :frame (string :utf-8)}
            [in out] (udp-socket opts)]
        (println "started")
        (go-loop [msg (<! in)]
          (println msg)
          (>! out msg)
          (recur (<! in)))))
  "
  [opts]
  (let [aleph-ch (deref (udp/udp-socket opts))
        in (chan)
        out (chan)]
    (go-loop [msg (<! out)] (enqueue aleph-ch msg) (recur (<! out)))
    (receive-all aleph-ch (fn [msg] (go (>! in msg))))
    [in out]))
