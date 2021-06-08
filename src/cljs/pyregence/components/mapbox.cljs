(ns pyregence.components.mapbox
  (:require [reagent.core       :as r]
            [reagent.dom.server :as rs]
            [clojure.string :as str]
            [clojure.core.async :refer [go <!]]
            [pyregence.config    :as c]
            [pyregence.utils     :as u]
            [pyregence.geo-utils :as g]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Mapbox Aaliases
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private mapbox       js/mapboxgl)
(def ^:private Map          js/mapboxgl.Map)
(def ^:private LngLatBounds js/mapboxgl.LngLatBounds)
(def ^:private Marker       js/mapboxgl.Marker)
(def ^:private Popup        js/mapboxgl.Popup)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Mapbox map JS instance. See: https://docs.mapbox.com/mapbox-gl-js/api/map/
(defonce the-map       (r/atom nil))
(defonce custom-layers (atom #{}))

(def ^:private the-marker (r/atom nil))
(def ^:private the-popup  (r/atom nil))
(def ^:private events     (atom {}))
(def ^:private hovered-id (atom nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Constants
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private fire-active "fire-active")
(def ^:private mapbox-dem  "mapbox-dem")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Map Information
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-zoom-info
  "Get zoom information. Returns [zoom min-zoom max-zoom]."
  []
  (let [m @the-map]
    [(.getZoom m)
     (.getMinZoom m)
     (.getMaxZoom m)]))

(defn- get-style
  "Returns mapbox style object."
  []
  (-> @the-map .getStyle (js->clj)))

(defn index-of
  "Returns first index of item in collection that matches predicate."
  [pred xs]
  (->> xs
       (keep-indexed (fn [idx x] (when (pred x) idx)))
       (first)))

(defn get-layer-idx-by-id
  "Returns index of layer with matching id."
  [id layers]
  (index-of #(= id (get % "id")) layers))

(defn layer-exists?
  "Returns true if the layer with matching id exists."
  [id]
  (some #(= id (get % "id")) (get (get-style) "layers")))

(defn- is-selectable? [s]
  (@custom-layers s))

(defn get-distance-meters
  "Returns distance in meters between center of the map and 100px to the right.
   Used to define the scale-bar map control."
  []
  (let [y     (-> @the-map .getContainer .-clientHeight (/ 2.0))
        left  (.unproject @the-map #js [0.0 y])
        right (.unproject @the-map #js [100.0 y])]
    (.distanceTo left right)))

(defn get-center
  "Retrives center as `{:lat ## :lon ##}`"
  []
  (let [center (.getCenter @the-map)]
    {:lat (aget center "lat")
     :lng (aget center "lng")}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Modify Map
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn set-zoom!
  "Sets the zoom level of the map to `zoom`."
  [zoom]
  (.easeTo @the-map (clj->js {:zoom zoom :animate true})))

(defn zoom-to-extent!
  "Pans/zooms the map to the provided extents."
  [[minx miny maxx maxy]]
  (let [bounds (LngLatBounds. (clj->js [[minx miny] [maxx maxy]]))]
    (.fitBounds @the-map bounds #js {:linear true})))

(defn set-center!
  "Centers the map on `center` with a minimum zoom value of `min-zoom`."
  [center min-zoom]
  (let [zoom (max (first (get-zoom-info)) min-zoom)]
    (.easeTo @the-map (clj->js {:zoom zoom :center center :animate true}))))

(defn ease-to!
  "Changes the position of the map to `center` given `zoom`, `pitch`, and `bearing`.
   Can also supply `min-zoom`.."
  [{:keys [zoom min-zoom] :as location}]
  (let [new-zoom (or zoom
                     (max (first (get-zoom-info)) (or min-zoom 0)))]
    (.easeTo @the-map (clj->js (-> location
                                   (assoc :zoom new-zoom)
                                   (assoc :animate (or (:animate location) true))
                                   (dissoc :min-zoom))))))

(defn fly-to!
  "Flies the map view to `center` at `zoom` with `bearing` and `pitch`."
  [new-location]
  (.flyTo @the-map (clj->js (merge {:bearing 0 :pitch 0 :zoom 0 :center [0 0]} new-location))))

(defn center-on-overlay!
  "Centers the map on the marker."
  []
  (when (some? @the-marker)
    (set-center! (.getLngLat @the-marker) 12.0)))

(defn set-center-my-location!
  "Sets the center of the map using a geolocation event."
  [event]
  (let [coords (.-coords event)
        lng    (.-longitude coords)
        lat    (.-latitude  coords)]
    (set-center! [lng lat] 12.0)))

(defn resize-map!
  "Resizes the map."
  []
  (when (some? @the-map)
    (.resize @the-map)))

(defn- upsert-layer
  "Inserts `new-layer` into `v` if the 'id' does not already exist, or updates
   the matching row if it does exist."
  [v {:keys [id] :as new-layer}]
  (if-let [idx (get-layer-idx-by-id id v)]
    (assoc v idx new-layer)
    (conj v new-layer)))

(defn- merge-layers [v new-layers]
  (reduce (fn [acc cur] (upsert-layer acc cur)) (vec v) new-layers))

(defn- update-style! [style & {:keys [sources layers new-sources new-layers]}]
  (swap! custom-layers into (map :id new-layers))
  (let [new-style (cond-> style
                    sources     (assoc "sources" sources)
                    layers      (assoc "layers" layers)
                    new-sources (update "sources" merge new-sources)
                    new-layers  (update "layers" merge-layers new-layers)
                    :always     (clj->js))]
    (-> @the-map (.setStyle new-style))))

(defn- add-icon! [icon-id url]
  (when-not (.hasImage @the-map icon-id)
    (.loadImage @the-map
                url
                (fn [_ img] (.addImage @the-map icon-id img #js {:sdf true})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Markers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-overlay-center
  "Returns marker lng/lat coordinates in the form `[lng lat]`."
  []
  (when (some? @the-marker)
    (-> @the-marker .getLngLat .toArray (js->clj))))

(defn get-overlay-bbox
  "Converts marker lng/lat coordinates to EPSG:3857, finds the current
   resolution and returns a bounding box."
  []
  (when (some? @the-marker)
    (let [[lng lat] (get-overlay-center)
          [x y]     (g/EPSG:4326->3857 [lng lat])
          zoom      (get (get-zoom-info) 0)
          res       (g/resolution zoom lat)]
      [x y (+ x res) (+ y res)])))

(defn clear-point!
  "Removes marker from the map."
  []
  (when (some? @the-marker)
    (.remove @the-marker)
    (reset! the-marker nil)))

(defn init-point!
  "Creates a marker at lnglat."
  [lng lat]
  (clear-point!)
  (let [marker (Marker. #js {:color "#FF0000"})]
    (doto marker
      (.setLngLat #js [lng lat])
      (.addTo @the-map))
    (reset! the-marker marker)))

(defn add-point-on-click!
  "Callback for `click` listener."
  [[lng lat]]
  (init-point! lng lat)
  (center-on-overlay!))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Popup
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn clear-popup!
  "Removes popup from the map."
  []
  (when (some? @the-popup)
    (.remove @the-popup)
    (reset! the-popup nil)))

(defn init-popup!
  "Creates a popup at `[lng lat]`, with `body` as the contents. `body` can
   be either HTML string a hiccup style vector."
  [[lng lat] body {:keys [classname width] :or {width "200px" classname ""}}]
  (clear-popup!)
  (let [popup (Popup. #js {:className classname :maxWidth width})]
    (doto popup
      (.setLngLat #js [lng lat])
      (.setHTML (rs/render-to-string body))
      (.addTo @the-map))
    (reset! the-popup popup)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-event!
  "Adds a listener for `event` with callback `f`. Returns the function `f`, which
   must be stored and passed to `remove-event!` when removing the listener.
   Warning: Only one listener per global/layer event can be added."
  [event f & {:keys [layer]}]
  (swap! events assoc (hash f) {:event event :layer layer :func f})
  (if layer
    (.on @the-map event layer f)
    (.on @the-map event f))
  f)

(defn remove-event!
  "Removes the listener for function `f`."
  [f]
  (let [{:keys [event layer func]} (get @events (hash f))]
    (if (some? layer)
      (.off @the-map event layer func)
      (.off @the-map event func))
    (swap! events dissoc (hash f))))

(defn remove-events!
  "Removes all listeners matching `event-name`. Can also supply `layer-name` to
   only remove events for specific layers."
  [event-name & [layer-name]]
  (doseq [[_ {:keys [event layer func]}] @events
          :when (and (= event event-name)
                     (or (nil? layer-name) (= layer layer-name)))]
    (remove-event! func)))

(defn- event->lnglat [e]
  (-> e (aget "lngLat") .toArray (js->clj)))

(defn add-single-click-popup!
  "Creates a marker where clicked and passes xy bounding box to `f` a click event."
  [f]
  (add-event! "click" (fn [e]
                        (let [lnglat (event->lnglat e)]
                          (add-point-on-click! lnglat)
                          (f lnglat)))))

(defn add-mouse-move-xy!
  "Passes `[lng lat]` to `f` on mousemove event."
  [f]
  (add-event! "mousemove" (fn [e] (-> e (event->lnglat) (f)))))

(defn- clear-highlight!
  "Clears the highlight of WFS features."
  [source]
  (when (some? @hovered-id)
    (.setFeatureState @the-map #js {:source source :id @hovered-id} #js {:hover false})
    (reset! hovered-id nil)))

(defn- feature-highlight!
  "Highlights a particular WFS features."
  [source feature-id]
  (clear-highlight! source)
  (reset! hovered-id feature-id)
  (.setFeatureState @the-map #js {:source source :id @hovered-id} #js {:hover true}))

(defn add-feature-highlight!
  "Adds events to highlight WFS features. Optionally can provide a function `f`,
   which will be called on click as `(f <feature-js-object> [lng lat])`"
  [layer source & [f]]
  (remove-events! "mousemove" layer)
  (remove-events! "mouseleave" layer)
  (remove-events! "click" layer)
  (add-event! "mouseenter"
              (fn [e]
                (when-let [feature-id (-> e (aget "features") (first) (aget "id"))]
                  (feature-highlight! source feature-id)))
              :layer layer)
  (add-event! "mouseleave"
              #(clear-highlight! source)
              :layer layer)
  (add-event! "click"
              (fn [e]
                (when-let [feature (-> e (aget "features") (first))]
                  (feature-highlight! source (aget feature "id"))
                  (when f (f feature (event->lnglat e)))))
              :layer layer))

(defn add-map-zoom-end!
  "Passes current zoom level to `f` on zoom-end event."
  [f]
  (add-event! "zoomend" #(f (get (get-zoom-info) 0))))

;; TODO: Implement
(defn add-layer-load-fail! [f])

(defn add-map-move!
  "Calls `f` on 'move' event."
  [f]
  (add-event! "move" f))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Modify Layer Properties
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- symbol-opacity [opacity]
  {"text-opacity" ["step" ["zoom"] 0 6 opacity 22 opacity]})

(defn- circle-opacity [opacity]
  {"circle-opacity"        opacity
   "circle-stroke-opacity" opacity})

(defn- raster-opacity [opacity]
  {"raster-opacity" opacity})

(defn- set-opacity
  "Returns layer with opacity set to `opacity`."
  [layer opacity]
  {:pre [(map? layer) (number? opacity) (<= 0.0 opacity 1.0)]}
  (let [layer-type (get layer "type")
        new-paint  (condp = layer-type
                     "raster" (raster-opacity opacity)
                     "circle" (circle-opacity opacity)
                     "symbol" (symbol-opacity opacity)
                     {})]
    (update layer "paint" merge new-paint)))

(defn set-opacity-by-title!
  "Sets the opacity of the layer."
  [id opacity]
  {:pre [(string? id) (number? opacity) (<= 0.0 opacity 1.0)]}
  (let [style      (get-style)
        layers     (get style "layers")
        pred       #(-> % (get "id") (is-selectable?))
        new-layers (map (u/only pred #(set-opacity % opacity)) layers)]
    (update-style! style :layers new-layers)))

(defn- set-visible
  "Returns layer with visibility set to `visible?`."
  [layer visible?]
  (assoc-in layer ["layout" "visibility"] (if visible? "visible" "none")))

(defn set-visible-by-title!
  "Sets a layer's visibility"
  [id visible?]
  {:pre [(string? id) (boolean? visible?)]}
  (let [style  (get-style)
        layers (get style "layers")]
    (when-let [idx (get-layer-idx-by-id id layers)]
      (let [new-layers (assoc-in layers [idx "layout" "visibility"] (if visible? "visible" "none"))]
        (update-style! style :layers new-layers)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WMS Layers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- wms-source [layer-name]
  {:type     "raster"
   :tileSize 256
   :tiles    [(c/wms-layer-url layer-name)]})

(defn- wms-layer [layer-name source-name opacity]
  {:id     layer-name
   :type   "raster"
   :source source-name
   :layout {:visibility "visible"}
   :paint  {:raster-opacity opacity}})

(defn- build-wms
  "Returns new WMS source and layer in the form `[source [layer]]`.
   `source` must be a valid WMS layer in the geoserver
   `z-index` allows layers to be rendered on-top (positive z-index) or below
   (negative z-index) Mapbox base map layers."
  [id source opacity]
  (let [new-source {source (wms-source source)}
        new-layer  (wms-layer id source opacity)]
    [new-source [new-layer]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WFS Layers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- wfs-source [layer-name]
  {:type       "geojson"
   :data       (c/wfs-layer-url layer-name)
   :generateId true})

(defn- zoom-interp
  "Interpolates a value (vmin to vmax) based on zoom value (from zmin to zmax)."
  [vmin vmax zmin zmax]
  ["interpolate" ["linear"] ["zoom"] zmin vmin zmax vmax])

(defn- on-hover [on off]
  ["case" ["boolean" ["feature-state" "hover"] false] on off])

(defn- incident-layer [layer-name source-name opacity]
  {:id     layer-name
   :type   "circle"
   :source source-name
   :layout {:visibility "visible"}
   :paint  {:circle-color        "#FF0000"
            :circle-opacity      opacity
            :circle-radius       (zoom-interp 8 14 5 20)
            :circle-stroke-color (on-hover "#FFFF00" "#000000")
            :circle-stroke-width (on-hover 4 2)}})

(defn- incident-labels-layer [layer-name source-name opacity]
  {:id     layer-name
   :type   "symbol"
   :source source-name
   :layout {:text-anchor        "top"
            :text-allow-overlap true
            :text-field         ["to-string" ["get" "prettyname"]]
            :text-font          ["Open Sans Semibold" "Arial Unicode MS Regular"]
            :text-offset        [0 0.6]
            :text-size          16
            :visibility         "visible"}
   :paint  {:text-color      "#000000"
            :text-halo-color ["case" ["boolean" ["feature-state" "hover"] false] "#FFFF00" "#FFFFFF"]
            :text-halo-width 1.5
            :text-opacity    ["step" ["zoom"] (on-hover opacity 0.0) 6 opacity 22 opacity]}})

(defn- build-wfs
  "Returns a new WFS source and layers in the form `[source layers]`.
   `source` must be a valid WFS layer in the geoserver
   `z-index` allows layers to be rendered on-top (positive z-index) or below
   (negative z-index) Mapbox base map layers."
  [id source opacity]
  (let [new-source {id (wfs-source source)}
        labels-id  (str id "-labels")
        new-layers [(incident-layer id id opacity)
                    (incident-labels-layer labels-id id opacity)]]
    [new-source new-layers]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Terrain and 3D Viewing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private terrain-source
  {mapbox-dem {:type     "raster-dem"
               :url      c/mapbox-dem-url
               :tileSize 512
               :maxzoom  14}})

(def ^:private terrain-layer
  {:source mapbox-dem :exaggeration 1.5})

(def ^:private sky-source
  {:id    "sky"
   :type  "sky"
   :paint {:sky-type                     "atmosphere"
           :sky-atmosphere-sun           [0.0, 0.0]
           :sky-atmosphere-sun-intensity 15}})

(defn- is-terrain? [s]
  (= s mapbox-dem))

(defn toggle-rotation!
  "Toggles whether the map can be rotated via right-click or touch."
  [enabled?]
  (let [toggle-drag-rotate-fn  (if enabled? #(.enable %) #(.disable %))
        toggle-touch-rotate-fn (if enabled? #(.enableRotation %) #(.disableRotation %))]
    (doto @the-map
      (-> .-dragRotate (toggle-drag-rotate-fn))
      (-> .-touchZoomRotate (toggle-touch-rotate-fn)))))

(defn toggle-pitch!
  "Toggles whether changing pitch via touch is enabled."
  [enabled?]
  (let [toggle-fn (if enabled? #(.enable %) #(.disable %))]
    (-> @the-map .-touchPitch (toggle-fn))))

(defn- toggle-terrain!
  "Toggles terrain DEM source, sky atmosphere layers."
  [enabled?]
  (update-style! (get-style) :new-sources terrain-source :new-layers [sky-source])
  (-> @the-map (.setTerrain (when enabled? (clj->js terrain-layer)))))

(defn toggle-dimensions!
  "Toggles whether the map is in 2D or 3D mode. When `three-dimensions?` is true,
   terrain is added to the base map and rotatation/pitch is enabled."
  [enabled?]
  (toggle-terrain! enabled?)
  (toggle-rotation! enabled?)
  (toggle-pitch! enabled?))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Manage Layers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn set-base-map-source!
  "Sets the base map source."
  [source]
  (go
    (let [style-chan (u/fetch-and-process source {} (fn [res] (.json res)))
          cur-style  (get-style)
          keep?      (fn [s] (or (is-selectable? s) (is-terrain? s)))
          sources    (->> (get cur-style "sources")
                          (u/filterm (fn [[k _]] (keep? (name k)))))
          layers     (->> (get cur-style "layers")
                          (filter (fn [l] (is-selectable? (get l "id")))))
          new-style  (-> (<! style-chan)
                         (js->clj)
                         (assoc "sprite" c/default-sprite)
                         (merge (select-keys cur-style ["terrain"]))
                         (update "sources" merge sources)
                         (update "layers" concat layers)
                         (clj->js))]
      (-> @the-map (.setStyle new-style)))))

(defn- hide-fire-layers [layers]
  (let [pred #(-> % (get "id") (is-selectable?))
        f    #(set-visible % false)]
    (map (u/only pred f) layers)))

(defn swap-active-layer!
  "Swaps the active layer. Used to scan through time-series WMS layers."
  [geo-layer opacity]
  {:pre [(string? geo-layer) (number? opacity) (<= 0.0 opacity 1.0)]}
  (let [style  (get-style)
        layers (hide-fire-layers (get style "layers"))
        [new-sources new-layers] (build-wms geo-layer geo-layer opacity)]
    (update-style! style
                   :layers      layers
                   :new-sources new-sources
                   :new-layers  new-layers)))

(defn reset-active-layer!
  "Resets the active layer source (e.g. from WMS to WFS). To reset to WFS layer,
   `style-fn` must not be nil."
  [geo-layer style-fn opacity]
  {:pre [(string? geo-layer) (number? opacity) (<= 0.0 opacity 1.0)]}
  (let [style  (get-style)
        layers (hide-fire-layers (get style "layers"))
        [new-sources new-layers] (if (some? style-fn)
                                   (build-wfs fire-active geo-layer opacity)
                                   (build-wms geo-layer geo-layer opacity))]
    (update-style! style
                   :layers      layers
                   :new-sources new-sources
                   :new-layers  new-layers)))

(defn create-wms-layer!
  "Adds WMS layer to the map."
  [id source z-index]
  (let [[new-source new-layers] (build-wms id source 1.0)]
    (update-style! (get-style)
                   :new-sources new-source
                   :new-layers  new-layers)))

(defn create-camera-layer!
  "Adds wildfire camera layer to the map."
  [id data]
  (add-icon! "video-icon" "./images/icons/video.png")
  (let [new-source {id {:type "geojson" :data data :generateId true}}
        new-layers [{:id     id
                     :source id
                     :type   "symbol"
                     :layout {:icon-image              "video-icon"
                              :icon-size               0.5
                              :icon-rotate             ["-" ["get" "pan"] 90]
                              :icon-rotation-alignment "map"}
                     :paint  {:icon-color      (on-hover "#e6550d" "#000000")
                              :icon-opacity    (on-hover 1.0 0.9)}}]]
    (update-style! (get-style) :new-sources new-source :new-layers new-layers)))

(defn create-red-flag-layer!
  "Adds red flag warning layer to the map."
  [id data]
  (let [new-source {id {:type "geojson" :data data :generateId true}}
        new-layers [{:id     id
                     :source id
                     :type   "fill"
                     :paint  {:fill-color   ["concat" "#" ["get" "color"]]
                              :fill-opacity 0.8}}]]
    (update-style! (get-style) :new-sources new-source :new-layers new-layers)))

(defn remove-layer!
  "Removes layer that matches `id`"
  [id]
  (let [curr-style      (get-style)
        layers          (get curr-style "layers")
        filtered-layers (remove #(= id (get % "id")) layers)]
    (swap! custom-layers disj id)
    (update-style! curr-style :layers filtered-layers)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Map Creation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init-map!
  "Initializes the Mapbox map inside of `container` (e.g. \"map\")."
  [container-id & [opts]]
  (set! (.-accessToken mapbox) c/mapbox-access-token)
  (when-not (.supported mapbox)
    (js/alert (str "Your browser does not support Pyregence Forecast.\n"
                   "Please use the latest version of Chrome, Safari, or Firefox.")))
  (reset! the-map
          (Map.
            (clj->js (merge {:container   container-id
                             :dragRotate  false
                             :maxZoom     20
                             :minZoom     3
                             :style       (-> c/base-map-options c/base-map-default :source)
                             :touchPitch  false
                             :trackResize true
                             :transition  {:duration 500 :delay 0}}
                            opts)))))
