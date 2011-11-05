(ns xw.swing.grid
  (:import
     (javax.swing JPanel)
     (java.awt Color Dimension Polygon Font)
     (java.awt.image BufferedImage)
     (java.awt.event FocusAdapter))
  (:use
     (xw board cursor graphics)
     (xw.swing events common)))

(def scale)
(def n)
(def height)
(def width)
(def arrow)
(def downarrow)
(def grid)
(def grid-changed)

(def gridpanel-focused? true)

;;; --------------------------------------------------
;;; board drawing
;;; --------------------------------------------------

;; geometry and styling

(defn resize [sc]
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

;; drawing and text

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

;; cursor drawing

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

;; cell rendering

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

;; board rendering

(defn render [g]
  (let [img (new BufferedImage width height (. BufferedImage TYPE_INT_ARGB))
        bg (. img getGraphics)]

    (doseq [[i j] board-iter]
      (square bg i j))

    (if gridpanel-focused?
      (do
        (draw-cursor bg current-x current-y)
        (doseq [[x y] (symm current-x current-y)]
          (fill-square bg x y agreen)))
      (do
        (doseq [[x y] (current-word-squares)]
          (fill-square bg x y pale-blue))))

    (. bg (setColor (. Color black)))
    (doseq [i (range 0 (+ n scale) scale)]
      (. bg drawLine 0 i n i)
      (. bg drawLine i 0 i n))

    (. g (drawImage img 0 0 nil))
    (. bg (dispose))))

;;; --------------------------------------------------
;;; event handling
;;; --------------------------------------------------

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
    (= c "Enter") (flip-dir))
  (grid-changed))

(defn ctrl-board-action [c]
  (cond
    (= c "Delete") (delete-current-word))
  (grid-changed))

(defn handle-key-event [e]
  (let [c (char-of e)]
    (cond
      (ctrl? e) (ctrl-board-action c)
      (alt? e)  nil ; defer to menubar
      true      (board-action c))))

(defn handle-mouse-event [e]
  (let [x (.getX e)
        y (.getY e)
        cx (int (/ x scale))
        cy (int (/ y scale))]
    (if (and (= cx current-x) (= cy current-y))
      (flip-dir)
      (move-to cx cy)))
  (grid-changed))

;;; --------------------------------------------------
;;; constructor
;;; --------------------------------------------------

(defn make [scale on-key grid-change]
  ; on-key: chainable keyboard handler
  ; grid-change: callback so ui can deal with grid changes
  (resize scale)
  (let [gpanel (proxy [JPanel] [] (paint [g] (render g)))]
    (doto gpanel
      (.setFocusable true)
      (.setBackground (. Color white))
      (.setPreferredSize (new Dimension width height))

      (.addFocusListener
        (proxy [FocusAdapter] []
          (focusGained [e]
                       (def gridpanel-focused? true)
                       (.repaint gpanel))
          (focusLost [e]
                     (def gridpanel-focused? false)
                     (.repaint gpanel))))

      (add-key-pressed-listener
        (fn [e]
          (handle-key-event e)
          (.repaint gpanel)
          (on-key e)))

      (add-mouse-click-listener
        (fn [e]
          (if gridpanel-focused?
            (handle-mouse-event e)
            (. gpanel requestFocus))
          (.repaint gpanel))))

    (def grid gpanel)
    (def grid-changed grid-change)
    gpanel))

(defn repaint [] (.repaint grid))

(defn request-focus [] (.requestFocus grid))
