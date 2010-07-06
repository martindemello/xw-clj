(defproject xw-clj "1.0"
  :description "crossword editor"
  :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0-SNAPSHOT"]
                 [commons-io "1.2"]
                 [commons-cli "1.1"]
                 [com.h2database/h2 "1.2.137"]
                 [com.miglayout/miglayout "3.7.3.1"]]
  :main xw.main
  :namespaces [xw.main])
 

