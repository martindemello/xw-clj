(ns xw.main
  (:gen-class)
  (:use xw.globals)
  (:use xw.wordlist)
  (:use xw.board)
  (:use xw.cursor)
  (:use xw.swing))
                                        
; -----------------------------------------
; main
; -----------------------------------------
;; Populate the board with empty cells

(defn -main []
  (doseq [[i j] board-iter]
    (set-board i j [:empty nil]))
  (renumber)
  (init-gui))
