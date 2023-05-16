(ns stretch.lockout.reward.function.action
  (:require [stretch.lockout.util.entity :as ent])
  (:import (java.util.function Consumer)))

(defn make-consumer [consume]
  (reify Consumer
    (accept [this player]
      (consume player))))

(def boom (reify Consumer
            (accept [this human-entity]
              (let [world (.getWorld human-entity)
                    loc (.getLocation human-entity)]
                (do
                  (.createExplosion world (.getX loc) (.getY loc) (.getZ loc) (float 0.5) true true)
                  (.strikeLightning world loc))))))

(def give-xp (reify Consumer
               (accept [this player]
                 (.giveExpLevels player 30))))

(def spawn-vexs (reify Consumer
                  (accept [this human-entity]
                    (let [world (.getWorld human-entity)
                          loc (.getLocation human-entity)]
                      (dotimes [n 5]
                        (ent/spawn-entity loc :vex))))))

(def actiontypes {:boom boom :xp give-xp :vexs spawn-vexs})
