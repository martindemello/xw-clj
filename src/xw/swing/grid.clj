(ns xw.swing.grid
  (:import
     (javax.swing JPanel SwingUtilities BorderFactory)
     (javax.swing.event DocumentListener)
     (java.awt BasicStroke Color Dimension Graphics Polygon Font Graphics2D RenderingHints)
     (java.awt.geom AffineTransform Ellipse2D FlatteningPathIterator GeneralPath
                    Line2D PathIterator Point2D)
     (java.awt.image BufferedImage)
     (java.awt.event WindowAdapter WindowEvent KeyListener KeyAdapter KeyEvent
                     InputEvent MouseAdapter FocusListener FocusAdapter)
     (java.awt.font TextLayout FontRenderContext))
  (:use (xw board cursor graphics)))

(def scale)
(def n)
(def height)
(def width)
(def arrow)
(def downarrow)

(def gridpanel-focused? true)

(defn resize-grid [sc]
  (def scale sc)
  (def n (* N scale))
  (def height (+ n 5))
  (def width (+ n 5))

  (def arrow
    (let [f (- scale 2) ; full, half, one-third, two-thirds
          h (/ f 2)
          o (/ f 3)
          t (/ (* f 2) 3)]
      [[h 0] [f h] [h f] [h t] [0 t] [0 o] [h o] [h 0]]))

  (def downarrow (map rot-90 arrow)))

(def letter-font (new Font "Arial" (. Font PLAIN) 18))
(def number-font (new Font "Serif" (. Font PLAIN) 9))
(def text-color (. Color black))
(def ared   (new Color 255 0 0 192))
(def ablue  (new Color 0 0 255 192))
(def agreen (new Color 0 128 0 64))
(def pale-yellow (new Color 255 255 192 192))

;; keyboard handling
(defn char-of [e] (. KeyEvent getKeyText (. e getKeyCode)))
(defn modifier [e] (. e getModifiers))
(defn modtext [e] (. KeyEvent getKeyModifiersText (modifier e)))
(def CTRL (. InputEvent CTRL_MASK))
(def ALT (. InputEvent ALT_MASK))
(defn ctrl? [e] (= CTRL (bit-and (modifier e) CTRL)))
(defn alt? [e] (= ALT (bit-and (modifier e) ALT)))

(defn board-action [c]
  (cond
    (re-matches #"^[A-Za-z]$" c) (do (place-letter c) (move-forward))
    (= c "Space") (do (place-symm :black) (move-forward))
    (= c "Backspace") (do (move-back) (place-letter :empty))
    (= c "Delete") (place-letter :empty)
    (= c "Down")  (move-down)
    (= c "Up")    (move-up)
    (= c "Right") (move-right)
    (= c "Left")  (move-left)
    (= c "Enter") (flip-dir)))

(defn topleft [x y]
  [(* x scale) (* y scale)])

(defn topleft-inner [x y]
  (add2 (topleft x y) [1 1]))

(defn inner-square [i j]
  (+ i 1) (+ j 1) (- scale 1) (- scale 1))

(defmacro in-square [[i j] bg x y & body]
  `(let [[~i ~j] (topleft ~x ~y)]
     (doto ~bg
       ~@body)))

(defn draw-letter [bg x y l]
  (in-square [i j] bg x y
             (.setColor text-color)
             (.setFont letter-font)
             (.drawString l (+ i 11) (+ j (- scale 6)))))

(defn draw-number [bg x y n]
  (in-square [i j] bg x y
             (.setColor text-color)
             (.setFont number-font)
             (.drawString (pr-str n) (+ i 2) (+ j 12))))

(defn fill-square [bg x y color]
  (in-square [i j] bg x y
             (.setColor color)
             (.fillRect (+ i 1) (+ j 1) (- scale 1) (- scale 1))))

(defn border-square [bg x y color]
  (in-square [i j] bg x y
             (.setColor color)
             (.drawRect (+ i 1) (+ j 1) (- scale 2) (- scale 2))))

(defn add-poly [bg poly col]
  (let [po (new Polygon)]
    (doseq [[i j] poly] (. po addPoint i j))
    (. bg setColor col)
    (. bg fillPolygon po)))

(defn arrow-ac [bg x0 y0]
  (add-poly bg (translate arrow x0 y0) ared))

(defn arrow-dn [bg x0 y0]
  (add-poly bg (translate downarrow (+ x0 scale) y0) ablue))

(defn draw-cursor [bg x y]
  (let [[i j] (topleft-inner x y)]
    ((if (across?) arrow-ac arrow-dn) bg i j)))

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

    (doseq [[i j] board-iter]
      (square bg i j))

    (when gridpanel-focused?
      (draw-cursor bg current-x current-y)
      (doseq [[x y] (symm current-x current-y)]
        (fill-square bg x y agreen)))

    (. bg (setColor (. Color black)))
    (doseq [i (range 0 (+ n scale) scale)]
      (. bg drawLine 0 i n i)
      (. bg drawLine i 0 i n))

    (. g (drawImage img 0 0 nil))
    (. bg (dispose))))

(defn make-grid [scale on-key] ; chainable keyboard handler
  (resize-grid scale)
  (let [gpanel  (proxy [JPanel] [] (paint [g] (render g)))]
    (doto gpanel
      (.setFocusable true)
      (.setBackground (. Color white))
      (.setPreferredSize (new Dimension width height))

      (.addKeyListener
        (proxy [KeyAdapter] []
          (keyPressed [e]
                      (let [c (char-of e)]
                        (cond
                          (ctrl? e)  nil ; defer to parent
                          (alt? e)   nil ; defer to menubar
                          true      (board-action c))
                        (.repaint gpanel)
                        (on-key e)))))

      (.addMouseListener
        (proxy [MouseAdapter] []
          (mouseClicked [e]
                        (if (not gridpanel-focused?) (. gpanel requestFocus)
                          (let [x (.getX e)
                                y (.getY e)]
                            (move-to (int (/ x scale)) (int (/ y scale)))
                            (.repaint gpanel))))))

      (.addFocusListener
        (proxy [FocusAdapter] []
          (focusGained [e]
                       (def gridpanel-focused? true)
                       (.repaint gpanel))
          (focusLost [e]
                     (def gridpanel-focused? false)
                     (.repaint gpanel)))))

    gpanel))
