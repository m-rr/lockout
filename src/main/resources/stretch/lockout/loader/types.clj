(ns stretch.lockout.loader.types
  (:require [stretch.lockout.util.cljutil :as util])
  (:import (org.bukkit CropState DyeColor GrassSpecies Material TreeSpecies)
           (org.bukkit Tag)
           (org.bukkit.block BlockFace Biome)
           (org.bukkit.material CocoaPlant$CocoaPlantSize)
           (org.bukkit.potion PotionEffectType)
           (stretch.lockout.reward RewardType)
           (org.bukkit.enchantments Enchantment)
           (org.bukkit.event.entity EntityDamageEvent$DamageCause)))

(def materials (util/map-enums Material))
(def treespecies (util/map-enums TreeSpecies))
(def blockfaces (util/map-enums BlockFace))
(def grassspecies (util/map-enums GrassSpecies))
(def dyecolors (util/map-enums DyeColor))
(def cropstates (util/map-enums CropState))
(def cocoaplantsizes (util/map-enums CocoaPlant$CocoaPlantSize))
(def disctypes (util/map-material-tags Tag/ITEMS_MUSIC_DISCS))
(def trapdoortypes (util/map-material-tags Tag/TRAPDOORS))
(def signtypes (util/map-material-tags Tag/STANDING_SIGNS))
;(def throwableprojectiletypes (util/map-material-tags Tag/ENTITY_TYPES_IMPACT_PROJECTILES))
;(def arrowtypes (util/map-material-tags Tag/ENTITY_TYPES_ARROWS))
(def potioneffecttypes (util/map-fields PotionEffectType))
(def biomes (util/map-enums Biome))
(def blockface (util/map-enums BlockFace))
(def rewardtypes (util/map-enums RewardType))
(def enchanttypes (util/map-fields Enchantment))
(def damagetypes (util/map-enums EntityDamageEvent$DamageCause))

(def snow-biome (select-keys biomes [:snowy_taiga :snowy_slopes :snowy_beach
                                     :snowy_plains :ice_spikes :frozen_ocean
                                     :frozen_peaks :frozen_river :grove
                                     :jagged_peaks]))
(def snow-biomes [:snowy_taiga :snowy_slopes :snowy_beach
                  :snowy_plains :ice_spikes :frozen_ocean
                  :frozen_peaks :frozen_river :grove
                  :jagged_peaks])
(def jungle-biome (select-keys biomes [:jungle :spare_jungle :bamboo_jungle]))
