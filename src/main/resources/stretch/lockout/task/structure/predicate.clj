(ns stretch.lockout.task.structure.predicate
  (:import (java.util.function Predicate)))

(def panic-bin reify Predicate
  (test [this block]
        (let [block-type (.getType block)] )))
