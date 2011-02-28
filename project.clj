(defproject xw-clj "1.0"
  :description "crossword editor"
  :source-path "src/clj"
  :java-source-path "src/java"
  :javac-fork "true"
  :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0-SNAPSHOT"]
                 [commons-io "1.2"]
                 [commons-cli "1.1"]
                 [com.h2database/h2 "1.2.137"]
                 ;[com.l2fprod/l2fprod-common-all "7.3"]
                 [com.seaglasslookandfeel/seaglasslookandfeel "0.1.7.3"]
                 [com.miglayout/miglayout "3.7.3.1"]]
  :dev-dependencies [[org.clojars.mmcgrana/lein-clojars "0.5.0"]
                     [lein-javac "1.2.0-SNAPSHOT"]]

:main xw.main
:namespaces [xw.main])
 

