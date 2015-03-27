(set-env!
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.6.0"]
                  [org.clojure/clojurescript "0.0-3126"]
                  [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                  [org.omcljs/om "0.8.8"]])

(require 'cljs.closure)

(deftask build
  []
  (cljs.closure/build "src" {:output-to "out/main.js"}))

(deftask witch
  []
  (cljs.closure/watch "src" {:main 'bingom.core
                             :output-to "out/main.js"}))
