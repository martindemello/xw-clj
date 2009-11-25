#!/bin/bash

BREAK_CHARS="(){}[],^%$#@\"\";:''|\\"
CLOJURE_DIR=/opt/clojure
CLOJURE_JAR=$CLOJURE_DIR/clojure.jar
CLOJURE_CONTRIB_DIR=/opt/clojure-contrib
CLOJURE_CONTRIB_JAR=$CLOJURE_CONTRIB_DIR/clojure-contrib.jar
MIG_JAR=/home/martin/opt/miglayout-3.7.1.jar
CP=$CLOJURE_JAR:$CLOJURE_CONTRIB_JAR:$MIG_JAR:"."

if [ $# -eq 0 ]; then 
     rlwrap --remember -c -b $BREAK_CHARS -f $HOME/.clj_completions \
     java -cp $CP clojure.lang.Repl
else
     java -cp $CP clojure.lang.Script "$@"
fi
