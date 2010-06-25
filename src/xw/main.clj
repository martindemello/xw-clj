(ns xw.main
  (:gen-class)
  (:use xw.globals)
  (:use xw.wordlist)
  (:use xw.board)
  (:use xw.clues)
  (:use xw.cursor)
  (:use xw.swing))
                                        
; -----------------------------------------
; main
; -----------------------------------------
;; Populate the board with empty cells

(defn -main []
  (new-board)
  (init-gui))
