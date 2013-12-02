# ac

A Clojure library designed to bridge Aleph and core.async

## Usage

```clojure
(use 'ac.core)

(defn echo-server [port]
  (let [connections (start-tcp-server { :port port})]
    (go-loop [[client-info in out] (<! connections)]
      (>! out (<! in))
      (recur (<! connections)))))
```
