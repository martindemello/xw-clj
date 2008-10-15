(in-ns 'main)
(clojure/refer 'clojure)

(import 
  '(java.awt BasicStroke Color Dimension Graphics Font Graphics2D RenderingHints)
  '(java.awt.geom AffineTransform Ellipse2D FlatteningPathIterator GeneralPath
                 Line2D PathIterator Point2D) 
  '(java.awt.image BufferedImage)
  '(java.awt.event WindowAdapter WindowEvent)
  '(java.awt.font TextLayout FontRenderContext)
  '(javax.swing JFrame JPanel))

(def N 15)
(def board (hash-map))

(def board-iter (for [j (range N) i (range N)] [i j]))

(defn letter [i j] ((board [i j]) 0))

(defn number [i j] ((board [i j]) 1))

(defn black? [i j] (= (letter i j) :black))

(defn blank? [i j] (= (letter i j) :empty))

(defn numbered? [i j] (not (= (number i j) nil)))

(defn set-letter [i j l]
  (def board (assoc board [i j] [l (number i j)])))

(defn set-number [i j n]
  (def board (assoc board [i j] [(letter i j) n])))

; numbering
(defn start-across? [i j] 
  (and 
    (not (black? i j))
    (or (= i 0) (black? (- i 1) j))
    (and (< i (- N 1)) (not (black? (+ i 1) j)))))

(defn start-down? [i j] 
  (and 
    (not (black? i j))
    (or (= j 0) (black? i (- j 1)))
    (and (< j (- N 1)) (not (black? i (+ j 1))))))

(defn renumber []
  (def n 1)
  (doseq [i j] board-iter
    (if (or (start-across? i j) (start-down? i j))
      (do 
        (set-number i j n)
        (def n (+ n 1)))
      (set-number i j nil))))

; Populate the board with empty cells
(doseq [i j] board-iter
  (def board (assoc board [i j] [:empty nil])))

(def board (assoc board [0 9] [:black nil]))
(set-letter 0 1 "A")
(renumber)

; Graphics

(def scale 40)
(def height 800)
(def width 800)
(def n (* N scale))
(def current-x 0)
(def current-y 0)

(defn topleft [x y]
  [(* x scale) (* y scale)])

(defn draw-letter [bg x y l]
  (let [[i j] (topleft x y)]
    (doto bg
      (setColor (. Color black))
      (setFont (new Font "Serif" (. Font PLAIN) 24))
      (drawString l (+ i 15) (+ j (- scale 10))))))

(defn draw-number [bg x y n]
  (let [[i j] (topleft x y)]
    (doto bg
      (setColor (. Color black))
      (setFont (new Font "Serif" (. Font PLAIN) 12))
      (drawString (pr-str n) (+ i 2) (+ j 12)))))

(defn fill-square [bg x y color]
  (let [[i j] (topleft x y)]
    (doto bg
      (setColor color)
      (fillRect (+ i 1) (+ j 1) (- scale 1) (- scale 1)))))

(defn border-square [bg x y color]
  (let [[i j] (topleft x y)]
    (doto bg
      (setColor color)
      (drawRect (+ i 1) (+ j 1) (- scale 1) (- scale 1)))))

(defn draw-cursor [bg x y]
  (border-square bg x y (. Color red)))

(defn black-square [bg x y]
  (fill-square bg x y (. Color black)))

(defn white-square [bg x y]
  (fill-square bg x y (. Color white))
  (when-not (blank? x y)
    (draw-letter bg x y (letter x y)))
  (when (numbered? x y)
    (draw-number bg x y (number x y))))

(defn square [bg x y]
  (if (black? x y) 
    (black-square bg x y)
    (white-square bg x y)))
      
(defn render [g]
  (let [img (new BufferedImage width height (. BufferedImage TYPE_INT_ARGB))
        bg (. img getGraphics)]
    (. bg (setColor (. Color black)))
    (doseq i (range 0 (+ n scale) scale)
      (. bg drawLine 0 i n i)
      (. bg drawLine i 0 i n))

    (doseq [i j] board-iter
      (square bg i j))

    (draw-cursor bg current-x current-y)

    (. g (drawImage img 0 0 nil))
    (. bg (dispose))))


(def panel (doto (proxy [JPanel] [] (paint [g] (render g)))
             (setBackground (. Color white))
             (setPreferredSize (new Dimension width height))))

(def frame
  (doto (new JFrame "xwe")
    (add panel) 
    (pack)
    (show)
    (addWindowListener
      (proxy [WindowAdapter] [] (windowClosing [e] (. System exit 0))))))
  

(defn main [args]
  (. frame show))
