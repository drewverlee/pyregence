(ns pyregence.config)

;; WMS options

(def wms-url "https://californiafireforecast.com:8443/geoserver/wms?SERVICE=WMS&VERSION=1.3.0")

(def legend-url       (str wms-url
                           "&REQUEST=GetLegendGraphic"
                           "&FORMAT=application/json"
                           "&LAYER=%s"))
(def capabilities-url (str wms-url
                           "&REQUEST=GetCapabilities"
                           "&NAMESPACE=%s"))
(def point-info-url   (str wms-url
                           "&REQUEST=GetFeatureInfo"
                           "&INFO_FORMAT=application/json"
                           "&LAYERS=%s"
                           "&QUERY_LAYERS=%s"
                           "&FEATURE_COUNT=1000"
                           "&TILED=true"
                           "&I=0"
                           "&J=0"
                           "&WIDTH=1"
                           "&HEIGHT=1"
                           "&CRS=EPSG:3857"
                           "&STYLES="
                           "&BBOX=%s"))

;; Layer options

(def layer-types [{:opt-id    0
                   :opt-label "Fire Area"
                   :filter    "fire-area"
                   :units     "Acres"}
                  {:opt-id    1
                   :opt-label "Fire Volume"
                   :filter    "fire-volume"
                   :units     "Acre-ft"}
                  {:opt-id    2
                   :opt-label "Impacted Structures"
                   :filter    "impacted-structures"
                   :units     "Structures"}
                  {:opt-id    3
                   :opt-label "Times Burned"
                   :filter    "times-burned"
                   :units     "Times"}])

;; Scroll speeds for time slider

(def speeds [{:opt-id 0 :opt-label ".5x" :delay 2000}
             {:opt-id 1 :opt-label "1x"  :delay 1000}
             {:opt-id 2 :opt-label "2x"  :delay 500}
             {:opt-id 3 :opt-label "5x"  :delay 200}])

;; Basemap options

(def BingMaps js/ol.source.BingMaps)
(def OSM      js/ol.source.OSM)
(def TileJSON js/ol.source.TileJSON)
(def XYZ      js/ol.source.XYZ)

(defn get-map-box-url [map-id]
  (str "https://api.mapbox.com/styles/v1/mspencer-sig/"
       map-id
       "/tiles/256/{z}/{x}/{y}"
       "?access_token=pk.eyJ1IjoibXNwZW5jZXItc2lnIiwiYSI6ImNrYThoa3J6aDBnMzMyeGxqbml3OWtlaDEifQ.vHjKFFXZxqwTELyyOIJNQA"))

(def base-map-options [{:opt-id    0
                        :opt-label "MapBox Street Topo"
                        :source    (XYZ.
                                    #js {:url (get-map-box-url "cka8jaky90i9m1iphwh79wr04")})}
                       {:opt-id    1
                        :opt-label "MapBox Satellite"
                        :source    (XYZ.
                                    #js {:url (get-map-box-url "cka8jm5161vcd1jn2g47k5yuo")})}
                       {:opt-id    2
                        :opt-label "MapBox Satellite Street"
                        :source    (XYZ.
                                    #js {:url (get-map-box-url "cka8hoo5v0gpy1iphg08hz7oj")})}])
