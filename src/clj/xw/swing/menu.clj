(ns xw.swing.menu
  (:import (java.awt.event KeyEvent))
  (:require [xw.swing.grid :as grid])
  (:require [xw.swing.file :as file])
  (:require [xw.swing.statusbar :as statusbar])
  (:use (xw.swing widgets common))
  (:use (xw board cursor))
  (:use (clojure.contrib [swing-utils :only (make-menubar make-action)])))

(def mf)

(defn reinit-ui []
  (grid/resize grid/scale)
  (goto-origin)
  (statusbar/init)
  (statusbar/update)
  (grid/repaint))

(defn save-file-handler [_]
  (file/save-file-dialog mf))

(defn load-file-handler [_]
  (when (file/load-file-dialog mf)
    (reinit-ui)))

(defn new-file-handler [n]
  (init-board n)
  (reinit-ui))

(defn show-about [_]
  (message-dialog mf "Hello World" "About Crossword Editor"))

(defn exit [_] (. System exit 0))

(defn make [frame]
  (def mf frame)
  (let [menubar-spec
        [{:name     "File"
          :mnemonic KeyEvent/VK_F
          :items
          [{:name       "New"
            :mnemonic   KeyEvent/VK_N
            :items
            [{:name "11x11" :handler (fn [_] (new-file-handler 11))}
             {:name "13x13" :handler (fn [_] (new-file-handler 13))}
             {:name "15x15" :handler (fn [_] (new-file-handler 15))}]}
           {:name       "Open"
            :mnemonic   KeyEvent/VK_O
            :short-desc "Open crossword"
            :long-desc  "Open a saved crossword"
            :handler    load-file-handler}
           {:name       "Save"
            :mnemonic   KeyEvent/VK_S
            :short-desc "Save file"
            :long-desc  "Save the file to disk. No-op if file not modified"
            :handler    save-file-handler}
           {} ; <- adds a separator
           {:name     "Exit"
            :mnemonic KeyEvent/VK_X
            :handler  exit}]}
         {:name     "Help"
          :mnemonic KeyEvent/VK_H
          :items    [{:name     "About"
                      :mnemonic KeyEvent/VK_A
                      :handler  show-about}]}]
        menubar      (make-menubar menubar-spec)]
    (doto frame
      (.setJMenuBar menubar)
      (.pack)
      (.setVisible true))))
