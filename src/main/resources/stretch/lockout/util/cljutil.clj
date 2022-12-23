(ns stretch.lockout.util.cljutil)

(defmacro map-enums [enumclass]
  `(apply merge (map #(hash-map (keyword (.toLowerCase (.name %))) %) (~(symbol (apply str (name enumclass) "/values"))))))

(defmacro map-fields [static-class]
  `(apply merge (map #(hash-map (keyword (.toLowerCase (.getName %))) %) (~(symbol (apply str (name static-class) "/values"))))))

(defn map-material-tags [material-tag]
  (apply merge (map #(hash-map (keyword (.toLowerCase (.name %))) %) (.getValues material-tag))))


