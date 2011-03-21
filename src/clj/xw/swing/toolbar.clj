(ns xw.swing.toolbar
  (:import (javax.swing JToolBar JButton))
  (:use (xw.swing events common)))

(declare toolbar)

(defn make [buttons]
  (def toolbar (JToolBar. "Toolbar"))
  (doseq [[title f] buttons]
    (let [b (JButton. title)]
      (on-action b ev (f))
      (.add toolbar b)))
  toolbar)
