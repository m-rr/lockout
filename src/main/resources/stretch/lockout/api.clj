(ns stretch.lockout.api
  (:gen-class)
  (:require (clojure [set])
            [stretch.lockout.builder.types :as i]
            [stretch.lockout.reward.function.action :as act]
            [stretch.lockout.util.entity :as ent])
  (:import (org.bukkit Bukkit)
           (org.bukkit.block BlockFace)
           (org.bukkit.inventory ItemStack)
           (org.bukkit.potion PotionEffect)
           (org.bukkit.inventory ItemStack)
           (org.bukkit.entity LivingEntity)
           (stretch.lockout.reward RewardItem)
           (stretch.lockout.reward RewardPotion)
           (stretch.lockout.reward RewardAction)
           (stretch.lockout.reward RewardComposite RewardChance$WeightedReward RewardChance RewardComponent)
           (stretch.lockout.task TaskComponent)
           (stretch.lockout.task TaskComposite)
           (stretch.lockout.task Task)
           (stretch.lockout.task TaskORComposite)
           (stretch.lockout.task.player TaskDamageFromSource)
           (stretch.lockout.task TaskRepeat)
           (stretch.lockout.task TaskMob)
           (stretch.lockout.task TaskMaterial TaskTHENComposite TaskANDComposite)
           (stretch.lockout.task.player TaskPotion)
           (stretch.lockout.task.structure TaskStructure)
           (java.util.function Predicate)
           (clojure.lang PersistentVector)
           (org.bukkit.attribute Attribute)))



(defn taskrace []
  (.getTaskRaceContext (.getPlugin (Bukkit/getPluginManager) "Lockout")))

(defn load-script [file]
  (load-file (str file)))

(defn load-data [data]
  (load-string (str data)))

(defn potion-reward-time []
  (.getRewardPotionTicks (taskrace)))

;; Gets static instance of class name with "org.bukkit.event." appended to it
(defn event-class [event-name]
  (let [event (str "org.bukkit.event." event-name)]
    (Class/forName event)))

(defn set-score [score]
  (.setMaxScore (taskrace) score))

(defn add-task [^TaskComponent task]
  (do
    (.addTask (.getTaskManager (taskrace)) task)
    task))

(defn remove-task [^TaskComponent task]
  (do
    (.removeTask (.getTaskManager (taskrace)) task)
    task))

(defn add-task-component [^TaskComposite task-composite ^TaskComponent task-component]
  (do
    (when (not (nil? task-component)) (.addTaskComponent task-composite task-component))
    task-composite))

(defmacro one-of [^TaskComponent first & rest]
  (eval (rand-nth (cons first rest))))

;; Wrapper
(defn add-enchants [^ItemStack item enchant-map]
  (if (empty? enchant-map) item
                           (do
                             (.addEnchantment item (get i/enchanttypes (key (first enchant-map))) (val (first enchant-map)))
                             (add-enchants item (rest enchant-map)))))

(defn make-reward-potion [potion-key amplifier reward-type description potion-time]
  (let [potion-ticks (if (nil? potion-time) (potion-reward-time) potion-time)]
    (new RewardPotion (new PotionEffect (get i/potioneffecttypes potion-key) potion-ticks (dec amplifier)) (get i/rewardtypes reward-type) description)))

(defn make-reward-item [material-key amount reward-type description enchantment-map]
  (cond (nil? enchantment-map) (new RewardItem (new ItemStack (get i/materials material-key) amount) (get i/rewardtypes reward-type) description)
        :else (let [item (new ItemStack (get i/materials material-key) amount)]
                (new RewardItem (add-enchants item enchantment-map) (get i/rewardtypes reward-type) description))))

;; not implemented
(defn make-reward-action [action-key reward-type description]
  (new RewardAction (get act/actiontypes action-key) (get i/rewardtypes reward-type) description))

; enchantment is used as potion time for reward potion...
(defn make-reward [reward-key amount reward-type description enchantment]
  (cond (contains? i/potioneffecttypes reward-key) (make-reward-potion reward-key amount reward-type description enchantment)
        (contains? act/actiontypes reward-key) (make-reward-action reward-key reward-type description)
        :else (make-reward-item reward-key amount reward-type description enchantment)))

(defn reward-vector [reward-vec]
  (let [[reward-key amount reward-type description enchantment] reward-vec]
    (make-reward reward-key amount reward-type description enchantment)))

(defn make-reward-list [reward-key-vec-list]
  (if (empty? reward-key-vec-list) nil
                                   (cons (reward-vector (first reward-key-vec-list)) (make-reward-list (rest reward-key-vec-list)))))


(defn make-weighted-reward [reward-vec weight]
  (new RewardChance$WeightedReward (reward-vector reward-vec) weight))

(defn reward-chance [description reward-map]
  (new RewardChance description (into [] (map (fn [[vec f]]
                                                (make-weighted-reward vec f)) reward-map))))

(defn make-reward-composite [reward-key-vec-list]
  (new RewardComposite (make-reward-list reward-key-vec-list)))

(defn with-reward-ugly [task reward-key-vec-list]
  (let [reward (if (nil? (second reward-key-vec-list)) (reward-vector (first reward-key-vec-list))
                                                       (make-reward-composite reward-key-vec-list))]
    (do
      (.setReward task reward)
      task)))

(defmulti with-reward (fn [task reward & rewards] (type reward)))

(defmethod with-reward PersistentVector [^TaskComponent task reward & rewards]
  (with-reward-ugly task (cons reward rewards)))

(defmethod with-reward RewardComponent [^TaskComponent task reward & rewards]
  (let [rewardComposite (new RewardComposite (cons reward rewards))]
    (do
      (.setReward task rewardComposite)
      task)))

(defn with-gui-item [^TaskComponent task item]
  (do
    (.setGuiItemStack task (new ItemStack (get i/materials item)))
    task))

(defn make-predicate [pred]
  (reify Predicate
    (test [this actor]
      (pred actor))))

(defn with-player-predicate [task pred]
  (do
    (.setPlayerPredicate task (make-predicate pred))
    task))

(defn with-entity-predicate [task pred]
  (do
    (.setEntityPredicate task (make-predicate pred))
    task))

(defn make-damaged-by-task [damage-cause event value description item]
  (let [task (new TaskDamageFromSource event (get i/damagetypes damage-cause) value description)]
    (do
      (with-gui-item task item)
      task)))

(defn make-material-task [material event value description item]
  (let [task (new TaskMaterial event (get i/materials material) value description)]
    (do
      (with-gui-item task item)
      task)))

(defn make-entity-task [entity event value description item]
  (let [task (new TaskMob event (get ent/entitytypes entity) value description)]
    (do
      (with-gui-item task item)
      task)))


(defn task-or [item value description first & rest]
  (-> (new TaskORComposite (map remove-task (cons first rest)) value description)
      (with-gui-item item)
      (add-task)))

(defn task-and [item value description first & rest]
  (-> (new TaskANDComposite (map remove-task (cons first rest)) value description)
      (with-gui-item item)
      (add-task)))

(defn task-then [item value description first & rest]
  (-> (new TaskTHENComposite (map remove-task (cons first rest)) value description)
      (with-gui-item item)
      (add-task)))

;; Predicates
(defn in-main-hand? [^LivingEntity entity material]
  (let [ent-item (.getType (.getItemInMainHand (.getEquipment entity)))]
    (if (keyword? material) (= ent-item (get i/materials material))
                            (contains? (set (vals material)) ent-item))))

(defn has-max-health? [^LivingEntity entity]
  (= (.getValue (.getAttribute entity Attribute/GENERIC_MAX_HEALTH)) (.getHealth entity)))

(defn wearing-armor? [^LivingEntity entity armor]
  (let [ent-armor (set (map #(.getType %) (.getArmorContents (.getEquipment entity))))]
    (if (keyword? armor) (contains? ent-armor (get i/materials armor))
                         (not (empty? (clojure.set/intersection ent-armor (set (vals armor))))))))

(defmulti in-biome? (fn [entity biome] (set? biome)))

(defmethod in-biome? false [^LivingEntity entity biome]
  (= (.getBiome (.getBlock (.getLocation entity))) (get i/biomes biome)))

(defmethod in-biome? true [^LivingEntity entity biome]
  (contains? biome (keyword (.toLowerCase (.name (.getBiome (.getBlock (.getLocation entity))))))))

(defn on-block? [^LivingEntity entity material]
  (= (.getType (.getRelative (.getBlock (.getLocation entity)) (get i/blockface :down))) (get i/materials material)))

(defn above-y? [^LivingEntity entity y-val]
  (>= (.getY (.getLocation entity)) y-val))

(defn has-potion-effect? [^LivingEntity entity potion-effect]
  (not (nil? (.getPotionEffect entity (get i/potioneffecttypes potion-effect)))))

(defn on-fire? [^LivingEntity entity]
  (< 0 (.getFireTicks entity)))

(defn in-water? [^LivingEntity entity]
  (.isInWater entity))

(defn in-rain? [^LivingEntity entity]
  (.isInRain entity))

(defn is-day? [^LivingEntity entity]
  (> 13000 (.getTime (.getWorld entity))))

(defn is-night? [^LivingEntity entity]
  (not (is-day? entity)))

(defn on-surface? [^LivingEntity entity]
  (<= 15 (.getLightFromSky (.getBlock (.getLocation entity)))))

;; Only in paper api
(defn is-lefty? [entity]
  (.isLeftHanded entity))

(defn is-entity? [^LivingEntity entity entity-type]
  (= (get ent/entitytypes entity-type) (.getType entity)))

(defn is-material? [block-item material]
  (= (get i/materials material) (.getType block-item)))

;; DSL
(defn place [material value description item]
  (let [task (make-material-task material (event-class "block.BlockPlaceEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn break [material value description item]
  (let [task (make-material-task material (event-class "block.BlockBreakEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn pickup [material value description item]
  (let [task (make-material-task material (event-class "entity.EntityPickupItemEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn drop-item [material value description item]
  (let [task (make-material-task material (event-class "player.PlayerDropItemEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn tame [entity value description item]
  (let [task (make-entity-task entity (event-class "entity.EntityTameEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn shear [entity value description item]
  (let [task (make-entity-task entity (event-class "player.PlayerShearEntityEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn make-then [value]
  (new TaskTHENComposite value))

(defn make-obtain [material value description item]
  (let [task (new TaskORComposite
                  (list (new TaskMaterial (event-class "inventory.InventoryClickEvent") (get i/materials material) value description)
                        (new TaskMaterial (event-class "inventory.FurnaceExtractEvent") (get i/materials material) value description)
                        (new TaskMaterial (event-class "inventory.CraftItemEvent") (get i/materials material) value description)
                        (new TaskMaterial (event-class "entity.EntityPickupItemEvent") (get i/materials material) value description))
                  value description)]
    (do
      (with-gui-item task item)
      task)))

(defn obtain [material value description item]
  (let [task (make-obtain material value description item)]
    (do
      (add-task task)
      task)))

(defn make-obtain-any [material-type value description item]
  (cond (not (list? material-type)) (make-obtain-any (list material-type) value description item)
        (or (empty? material-type) (nil? material-type)) nil
        :else (add-task-component
                (new TaskORComposite
                     (reduce conj () (map #(make-obtain % value description item) (keys (first material-type)))) value description)
                (make-obtain-any (rest material-type) value description item))))

(defn obtain-any [material-type value description item]
  (let [task (make-obtain-any material-type value description item)]
    (do
      (with-gui-item task item)
      (add-task task)
      task)))

(defn make-quest [event value description item]
  (let [task (new Task (event-class event) value description)]
    (do
      (with-gui-item task item)
      task)))

(defn quest [event value description item]
  (let [task (make-quest event value description item)]
    (do
      (add-task task)
      task)))

(defn make-acquire [potion-effect value description item]
  (let [task (new TaskPotion (get i/potioneffecttypes potion-effect) value description)]
    (do
      (with-gui-item task item)
      task)))

(defn eat [food value description item]
  (let [task (make-material-task food (event-class "player.PlayerItemConsumeEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn acquire [potion-effect value description item]
  (let [task (make-acquire potion-effect value description item)]
    (do
      (add-task task)
      task)))

(defn kill [entity value description item]
  (let [task (make-entity-task entity (event-class "entity.EntityDeathEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn smelt [material value description item]
  (let [task (make-material-task material (event-class "inventory.FurnaceExtractEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn bucket [entity value description item]
  (let [task (make-entity-task entity (if (= entity :axolotl) (event-class "player.PlayerBucketEntityEvent") (event-class "player.PlayerBucketFishEvent")) value description item)]
    (do
      (add-task task)
      task)))

(defn hit [entity value description item]
  (let [task (make-entity-task entity (event-class "entity.EntityDamageByEntityEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn breed [entity value description item]
  (let [task (make-entity-task entity (event-class "entity.EntityBreedEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn damaged-by [entity-block damage-cause value description item]
  (let [event (if (contains? ent/entitytypes entity-block) (event-class "entity.EntityDamageByEntityEvent")
                                                           (event-class "entity.EntityDamageByBlockEvent"))
        task (make-damaged-by-task damage-cause event value description item)]
    (do
      (add-task task)
      task)))

(defn take-damage [damage-cause value description item]
  (let [task (make-damaged-by-task damage-cause (event-class "entity.EntityDamageEvent") value description item)]
    (do
      (add-task task)
      task)))

(defn stand-on [material value description item]
  (-> (quest "player.PlayerMoveEvent" value description item)
      (with-player-predicate #(on-block? % material))))

(defn interact [entity-block value description item]
  (let [task (if (contains? ent/entitytypes entity-block) (make-entity-task entity-block (event-class "player.PlayerInteractEntityEvent") value description item)
                                                          (make-material-task entity-block (event-class "player.PlayerInteractEvent") value description item))]
    (do
      (add-task task)
      task)))

;; BellRingEvent is in paper api and will not work with quest fn
(defn ring-bell [value description item]
  (let [task (new Task (Class/forName "io.papermc.paper.event.block.BellRingEvent") value description)]
    (do
      (with-gui-item task item)
      (add-task task)
      task)))

(defn do-times [n task]
  (let [repeat-task (new TaskRepeat task n)]
    (do
      (remove-task task)
      (add-task repeat-task)
      repeat-task)))

;; Remove later
(defn structure [value description item pred]
  (let [task (new TaskStructure (event-class "block.BlockPlaceEvent") (make-predicate pred) value description)]
    (do
      (with-gui-item task item)
      (add-task task)
      task)))


