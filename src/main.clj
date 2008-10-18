(in-ns 'main)
(clojure/refer 'clojure)

(import 
  '(java.awt BasicStroke Color Dimension Graphics Font Graphics2D RenderingHints
             GridLayout BorderLayout FlowLayout)
  '(java.awt.geom AffineTransform Ellipse2D FlatteningPathIterator GeneralPath
                  Line2D PathIterator Point2D) 
  '(java.awt.image BufferedImage)
  '(java.awt.event WindowAdapter WindowEvent KeyListener KeyAdapter KeyEvent)
  '(java.awt.font TextLayout FontRenderContext)
  '(javax.swing JFrame JPanel JTextField))

(def N 15)
(def M (- N 1)) ; since the squares are numbered from 0 .. n-1
(def board (hash-map))

(def board-iter (for [j (range N) i (range N)] [i j]))

; accessors
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

; cursor movement
(def current-x 0)
(def current-y 0)
(def current-dir :across)
(defn across? [] (= current-dir :across))
(defn inc-pos [i] (rem (+ i 1) N))
(defn dec-pos [i] (if (= i 0) (- N 1) (- i 1)))
(defn move-down  [] (def current-y (inc-pos current-y)))
(defn move-up    [] (def current-y (dec-pos current-y)))
(defn move-right [] (def current-x (inc-pos current-x)))
(defn move-left  [] (def current-x (dec-pos current-x)))
(defn move-forward [] (if (across?) (move-right) (move-down)))
(defn move-back [] (if (across?) (move-left) (move-up)))
(defn flip-dir [] 
  (if (across?) (def current-dir :down) (def current-dir :across)))

; do something in current square
(defn symm [i j] [ [i j] [j (- M i)] [(- M i) (- M j)] [(- M j) i] ])

(defn place-letter [c]
  (when (black? current-x current-y)
    (doseq [i j] (symm current-x current-y) (set-letter i j :empty))
    (renumber))
  (set-letter current-x current-y c))

(defn place-black []
  (doseq [i j] (symm current-x current-y) (set-letter i j :black))
  (renumber))


; Populate the board with empty cells
(doseq [i j] board-iter
  (def board (assoc board [i j] [:empty nil])))

(def board (assoc board [0 9] [:black nil]))
(set-letter 0 1 "A")
(renumber)

; Graphics

(def scale 40)
(def height 620)
(def width 800)
(def n (* N scale))
(def letter-font (new Font "Serif" (. Font PLAIN) 24))
(def number-font (new Font "Serif" (. Font PLAIN) 12))
(def text-color (. Color black))

(defn topleft [x y]
  [(* x scale) (* y scale)])

(defn inner-square [i j]
  (+ i 1) (+ j 1) (- scale 1) (- scale 1))

(defmacro in-square [[i j] bg x y & body]
  `(let [[~i ~j] (topleft ~x ~y)]
     (doto ~bg
       ~@body)))

(defn draw-letter [bg x y l]
  (in-square [i j] bg x y
             (setColor text-color)
             (setFont letter-font)
             (drawString l (+ i 15) (+ j (- scale 10)))))

(defn draw-number [bg x y n]
  (in-square [i j] bg x y
             (setColor text-color)
             (setFont number-font)
             (drawString (pr-str n) (+ i 2) (+ j 12))))

(defn fill-square [bg x y color]
  (in-square [i j] bg x y
             (setColor color)
             (fillRect (+ i 1) (+ j 1) (- scale 1) (- scale 1))))

(defn border-square [bg x y color]
  (in-square [i j] bg x y
             (setColor color)
             (drawRect (+ i 1) (+ j 1) (- scale 2) (- scale 2))))

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

(def output (doto (new JTextField)
              (setColumns 80)))

(defn char-of [e] (. KeyEvent getKeyText (. e getKeyCode)))

(def frame
  (let [j (new JFrame "xwe")
        p (. j getContentPane)]
    (doto p
      (setLayout (new FlowLayout))
      (add panel)
      (add output))
    (doto j
      (setSize 900 750)
      (show)
      (addWindowListener
        (proxy [WindowAdapter] [] (windowClosing [e] (. System exit 0))))
      (setFocusable 'true)
      (addKeyListener
        (proxy [KeyAdapter] []
          (keyPressed 
            [e] 
            (let [c (char-of e)]
              (cond
                (re-matches #"^[A-Za-z]$" c) (do (place-letter c) (move-forward))
                (= c "Space") (do (place-black) (move-forward))
                (= c "Backspace") (do (move-back) (place-letter :empty))
                (= c "Delete") (place-letter :empty)
                (= c "Down")  (move-down)
                (= c "Up")    (move-up)
                (= c "Right") (move-right)
                (= c "Left")  (move-left)
                (= c "Equals") (flip-dir) 

                ) 
              (. output setText (.concat (. output getText) c))
              (. panel repaint)
              )))))))

(defn main [args]
  (. frame show))
