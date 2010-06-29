(ns xw.main
  (:gen-class)
  (:use xw.board)
  (:use xw.swing.ui))
                                        
; -----------------------------------------
; main
; -----------------------------------------
;; Populate the board with empty cells

(defn -main []
  (new-board 15)
  (init-gui 30))
