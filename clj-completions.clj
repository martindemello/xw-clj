(defmacro with-out-file [pathname & body]
  `(with-open stream# (new java.io.FileWriter ~pathname)
     (binding [*out* stream#]
       ~@body)))

(def completions (keys (ns-publics (find-ns 'clojure))))
(with-out-file "clj-keys.txt" (doseq x completions (println x)))
