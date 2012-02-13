(ns xw.swing.thumbnail
  (:import
     (javax.swing JPanel)
     (java.awt Color Dimension Polygon Font)
     (java.awt.image BufferedImage)
     (java.awt.event FocusAdapter))
  (:use
     (xw.swing events common)))

;;; --------------------------------------------------
;;; board drawing
;;; --------------------------------------------------

(def scale 4)

;; geometry and styling

(defn topleft [x y]
  [(* x scale) (* y scale)])

(defmacro in-square [[i j] bg x y & body]
  `(let [[~i ~j] (topleft ~x ~y)]
     (doto ~bg
       ~@body)))

;; square filling
(defn fill-square [bg x y color]
  (in-square
    [i j] bg x y
    (.setColor color)
    (.fillRect (+ i 0) (+ j 0) (- scale 1) (- scale 1))))

;; cell rendering

(defn square [bg x y s]
  (let [color (if (= s \#) black white)]
    (fill-square bg x y color)))

;; board rendering

(defn render [g board]
  (let [x (board :x)
        y (board :y)
        grid (board :grid)
        width  (* x scale)
        height (* y scale)
        img (new BufferedImage width height (. BufferedImage TYPE_INT_ARGB))
        bg (. img getGraphics)]

    (doseq [[i j s]
            (for [i (range x)
                  j (range y)]
              [i j (get-in grid [j i])])]
      (square bg i j s))

    (. g (drawImage img 0 0 nil))
    (. bg (dispose))))
;;; --------------------------------------------------
;;; constructor
;;; --------------------------------------------------

(defn make [grid]
  (let [gpanel (proxy [JPanel] [] 
                 (paint [g] (render g grid)))
        x (grid :x)
        y (grid :y)
        width  (* x scale)
        height (* y scale)]
    (doto gpanel
      (.setFocusable true)
      (.setBackground white)
      (.setPreferredSize (new Dimension width height))

      (.addFocusListener
        (proxy [FocusAdapter] []
          (focusGained [e]
                       (.setBorder gpanel red-border)
                       (.repaint gpanel))
          (focusLost [e]
                     (.setBorder gpanel black-border)
                     (.repaint gpanel)))))
    gpanel))
