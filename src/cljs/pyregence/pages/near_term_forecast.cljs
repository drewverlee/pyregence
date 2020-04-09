(ns pyregence.pages.near-term-forecast
  (:require [pyregence.styles :as $]))

;; OpenLayers aliases
(def Map        js/ol.Map)
(def View       js/ol.View)
(def defaults   js/ol.control.defaults)
(def fromLonLat js/ol.proj.fromLonLat)
(def Tile       js/ol.layer.Tile)
(def OSM        js/ol.source.OSM)

(defonce the-map (atom nil))

(defn init-map! []
  (reset! the-map
          (Map.
           #js {:target   "map"
                :layers   #js [(Tile.
                                #js {:title   "OpenStreetMap"
                                     :visible true
                                     :source  (OSM.)})]
                :controls (defaults)
                :view     (View.
                           #js {:projection "EPSG:3857"
                                :center     (fromLonLat #js [-120.8958 38.8375])
                                :zoom       10})})))

(defn root-component [_]
  (init-map!)
  (fn [_]
    [:div {:style ($/root)}
     [:h1 "This is a map. Put something cool on it."]]))
