(in-ns 'main)
(clojure/refer 'clojure)

(import 
  '(java.awt BasicStroke Color Dimension Graphics Graphics2D RenderingHints)
  '(java.awt.geom AffineTransform Ellipse2D FlatteningPathIterator GeneralPath
                 Line2D PathIterator Point2D) 
  '(java.awt.image BufferedImage)
  '(java.awt.event WindowAdapter WindowEvent)
  '(java.awt.font TextLayout FontRenderContext)
  '(javax.swing JFrame JPanel))

(def N 15)
(def board (hash-map))

(def board-iter (for [i (range N) j (range N)] [i j]))

(defn letter [i j] ((board [i j]) 0))

(defn black? [i j] (= (letter i j) :black))

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
  (def board (assoc board [i j] [" " nil])))

(def board (assoc board [0 9] [:black nil]))

; Graphics

  '(java.awt.font TextLayout FontRenderContext)

(def scale 40)
(def height 800)
(def width 800)
(def n (* N scale))

(defn render [g]
  (let [img (new BufferedImage width height (. BufferedImage TYPE_INT_ARGB))
        bg (. img getGraphics)]
    (. bg (setColor (. Color black)))
    (doseq i (range 0 (+ n scale) scale)
      (. bg drawLine 0 i n i)
      (. bg drawLine i 0 i n))

    (. g (drawImage img 0 0 nil))
    (. bg (dispose))))


(def panel (doto (proxy [JPanel] [] (paint [g] (render g)))
             (setBackground (. Color white))
             (setPreferredSize (new Dimension width height))))

(def frame
  (doto (new JFrame "xwe")
    (add panel) 
    (pack)
    (addWindowListener
      (proxy [WindowAdapter] [] (windowClosing [e] (. System exit 0))))))

(defn main [args]
  (. frame show))
