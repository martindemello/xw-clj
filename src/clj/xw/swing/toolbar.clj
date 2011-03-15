(ns xw.swing.toolbar
  (:import (javax.swing JToolBar JButton))
  (:use (xw.swing events common)))

(declare toolbar)

(defn make [buttons]
  (def toolbar (JToolBar. "Toolbar"))
  (doseq [b buttons] (.add toolbar b))
  toolbar)
