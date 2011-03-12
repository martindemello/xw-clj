(ns xw.swing.file
  (:import (javax.swing JFileChooser))
  (:require [xw.swing.statusbar :as statusbar])
  (:use (xw board cursor wordlist clues words))
  (:use (xw.swing widgets)))

(defn file-dialog [parent arg f]
  (let [fc (JFileChooser.)
        sel (if (= arg :load)
              (.showOpenDialog fc parent)
              (.showSaveDialog fc parent))]
    (when (= sel JFileChooser/APPROVE_OPTION)
      (f (.getSelectedFile fc)))))

(defn save-file-dialog [parent]
  (file-dialog
    parent :save
    (fn [f]
      (save-to-file f)
      (statusbar/update))))

(defn load-file-dialog [parent]
  (file-dialog
    parent :load
    (fn [f]
      (let [retval (load-from-file f)]
        (when (not retval)
          (error-dialog parent "Could not load file"))
        retval))))
      
