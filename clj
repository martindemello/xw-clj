#!/bin/bash

WD=`pwd`
CLASSPATH="$WD/lib/clojure.jar:$WD/lib/*:$WD/classes/*:$WD/src"   # update for your env

if [ $# -eq 0 ] ; then
    exec rlwrap java -cp "$CLASSPATH" clojure.main --repl
else
    while [ $# -gt 0 ] ; do
        case "$1" in
        -cp|-classpath)
            CLASSPATH="$CLASSPATH:$2"
            shift ; shift
            ;;
        -e) tmpfile="/tmp/`basename $0`.$$.tmp"
            echo "$2" > "$tmpfile"
            shift ; shift
            set "$tmpfile" "$@"
            break # forces any -cp to be before any -e
            ;;
        *)  break
            ;;
        esac
    done
    java -cp "$CLASSPATH" clojure.main "$@"
    if [ -n "$tmpfile" -a -f "$tmpfile" ] ; then
        rm -f "$tmpfile"
    fi
fi
