(import '(java.io File BufferedInputStream IOException RandomAccessFile))
(import '(org.apache.commons.io FileUtils))

; Dawg node: 
; -------------------------------------------------------
; |76543210|76543210|76543210|76543210|76543210|76543210|
; |ssssssss|ssssssss|cccccccc|cccccccc|sssscccc|WSslllll|
; -------------------------------------------------------
; l = letter (0-25)
; W = end-of-word?
; S = end-of-siblings?
; c = index of child [b3 b2 b1]
; s = index of next sibling [b5 b4 b1]

(def dawg (FileUtils/readFileToByteArray (File. "words.dawg")))
(def ?a 97)
(def ?z 122)
(def b<< bit-shift-left)
(def b>> bit-shift-right)
(def b& bit-and)
(def b? bit-test)

(defn letter-byte [ix] (aget dawg (* ix 6)))

(defn letter-n [ix] (b& (letter-byte ix) 0x1F))
(defn letter-c [ix] (char (+ A (letter-n ix))))
(defn letter? [ix l] (= (letter-n ix) l))

(defn eos? [ix] (b? (letter-byte ix) 6))
(defn eow? [ix] (b? (letter-byte ix) 7))
(defn show-bits [by] (map #(b? by %) (range 8)))
(defn ff [by] (b& by 0xff))
(defn ffd [off] (ff (aget dawg off)))

(defn child [ix]
  (let [l (* ix 6)
        v1 (ffd (+ l 1))
        v2 (ffd (+ l 2))
        v3 (ffd (+ l 3))]
    (+ (b<< (b& v1 0x0f) 16) (b<< v2 8) v3)))

(defn next-sib [ix]
  (let [l (* ix 6)
        v1 (ffd (+ l 1))
        v2 (ffd (+ l 4))
        v3 (ffd (+ l 5))]
    (+ (b<< (b>> v1 4) 16) (b<< v2 8) v3)))

(defn find-letter [ix letter]
  (cond
    (letter? ix letter) ix
    (> (letter-n ix) letter) false
    (eos? ix) false
    :else (recur (next-sib ix) letter)))

(defn sibs [ix]
  (loop [i ix acc (list ix)]
    (let [n (next-sib i)]
      (cond
        (zero? n) (reverse acc)
        :else (recur n (cons n acc))))))

(defn alpha? [c] (and (<= (int c) 25) (>= (int c) 0)))

(defn to-word [trail]
   (apply str (map #(char (+ (int %) ?a)) trail)))

(defn to-nums [word]
  (map #(- (int %) ?a) word))

(defn terminate [trail wordp acc]
  (if wordp (cons (to-word (reverse trail)) acc) acc))

(declare follow)

(defn patt [p ix trail wordp acc]
  ;(print [(map #(char (+ % ?a)) p) ix trail wordp acc])
  (if (empty? p) 
    (terminate trail wordp acc)
    (let [h (first p)
          t (rest p)]
      (cond
        (alpha? h) (let [n (find-letter ix h)]
                     (if n (follow h t n trail acc) '()))
        :else (reduce concat acc (map #(follow (letter-n %) t % trail acc) (sibs ix))) ))))

(defn follow [h t n trail acc]
  (patt t (child n) (cons h trail) (eow? n) acc))

(defn pattern [p]
  (patt p 0 '() false '()))
