(ns stretch.lockout.util.entity
  (:require [stretch.lockout.util.cljutil :as util])
  (:import (org.bukkit.entity EntityType)))

(def entitytypes (util/map-enums EntityType))

(defn spawn-entity [location entityname]
  (let [type (get entitytypes (keyword entityname))]
    (when (and type (.isSpawnable type))
      (.spawnEntity (.getWorld location) location type))))
