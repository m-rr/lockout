(ns stretch.lockout.loader.default-task-builder
  (:require [stretch.lockout.loader.task-loader :as t]))

(def bad-stand-on [:sand :coal_ore])
(def bad-pickup [:wheat_seeds :gravel])
(def test-block [:stone :sand :grass_block :oak_log :cobblestone :clay])

(def create-stand
  #(let [material (rand-nth bad-stand-on)]
    (t/stand-on material 1 (str "Stand on " material) material)))

(def create-pickup
  #(let [material (rand-nth bad-pickup)]
    (t/pickup material 1 (str "Pick up " material) material)))

;; Should return a sequence of tasks of a type

;; Returns func only taking material as parameter
(defn task-builder [func value desc]
  (fn [material]
    (func material value (str desc material) material)))

(defn next-task-sequence [built-task-func material-vec]
  (built-task-func (rand-nth material-vec)))

(defn task-builder-composer [func value desc material-vec]
  (comp (task-builder func value desc) #(rand-nth material-vec)))

(defn task-seq [func value description-prefix material-vec]
  (let [built-task (task-builder-composer func value description-prefix material-vec)]
    (repeatedly built-task)))
