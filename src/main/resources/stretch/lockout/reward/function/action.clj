(ns stretch.lockout.reward.function.action
  (:require [stretch.lockout.util.entity :as ent])
  (:import (java.util.function Consumer)))

(def boom (reify Consumer
            (accept [this human-entity]
              (let [world (.getWorld human-entity)
                    loc (.getLocation human-entity)]
                (do
                  (.createExplosion world (.getX loc) (.getY loc) (.getZ loc) 1 true true)
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
