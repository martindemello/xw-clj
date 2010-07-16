(ns xw.swing.events
  (:import
     (javax.swing.event DocumentListener)
     (java.awt.event WindowAdapter WindowEvent KeyListener KeyAdapter KeyEvent
                     InputEvent MouseAdapter FocusListener FocusAdapter)))

;; keyboard handling
(defn char-of [e] (. KeyEvent getKeyText (. e getKeyCode)))
(defn modifier [e] (. e getModifiers))
(defn modtext [e] (. KeyEvent getKeyModifiersText (modifier e)))
(def CTRL (. InputEvent CTRL_MASK))
(def ALT (. InputEvent ALT_MASK))
(defn ctrl? [e] (= CTRL (bit-and (modifier e) CTRL)))
(defn alt? [e] (= ALT (bit-and (modifier e) ALT)))


; after clojure.contrib.swing-utils
(defn add-key-pressed-listener [component f & args]
  (let [listener (proxy [KeyAdapter] []
                   (keyPressed [e] (apply f e args)))]
    (.addKeyListener component listener)
    listener))

(defn add-mouse-click-listener [component f & args]
  (let [listener (proxy [MouseAdapter] []
                   (mouseClicked [e] (apply f e args)))]
    (.addMouseListener component listener)
    listener))

; common action for add/remove/change
(defn add-indifferent-document-listener [component f & args]
  (let [listener 
        (proxy [DocumentListener] []
          (insertUpdate  [e] (apply f e args))
          (removeUpdate  [e] (apply f e args))
          (changedUpdate [e] (apply f e args)))]
    (.addDocumentListener component listener)
    listener))

