(ns xw.swing.common
  (:import
     (java.awt Color)
     (javax.swing BorderFactory)))

(def ared   (new Color 255 0 0 192))
(def ablue  (new Color 0 0 255 192))
(def agreen (new Color 0 128 0 64))
(def pale-yellow (new Color 255 255 192 192))
(def pale-blue (new Color 192 192 255 192))

(def red-border
  (. BorderFactory createLineBorder Color/RED))

(def black-border
  (. BorderFactory createLineBorder Color/BLACK))
