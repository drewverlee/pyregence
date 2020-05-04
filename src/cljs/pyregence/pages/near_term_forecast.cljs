(ns pyregence.pages.near-term-forecast
  (:require [cljsjs.vega-embed]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :as str]
            [clojure.core.async :refer [go <!]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [herb.core :refer [<class]]
            [pyregence.components.openlayers :as ol]
            [pyregence.styles :as $]
            [pyregence.utils  :as u]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce minZoom           (r/atom 0))
(defonce maxZoom           (r/atom 28))
(defonce *zoom             (r/atom 10))
(defonce legend-list       (r/atom []))
(defonce layer-list        (r/atom []))
(defonce *layer-idx        (r/atom 0))
(defonce last-clicked-info (r/atom nil))
(defonce animate?          (r/atom false))
(defonce *layer-type       (r/atom 0))
(defonce *speed            (r/atom 1))
(defonce *base-map         (r/atom 0))

;; Static Data

(defonce layer-types [{:opt-id 0
                       :opt-label "Fire Area"
                       :filter "fire-area"
                       :units "Acres"}
                      {:opt-id 1
                       :opt-label "Fire Volume"
                       :filter "fire-volume"
                       :units "Acre-ft"}
                      {:opt-id 2
                       :opt-label "Impacted Structures"
                       :filter "impacted-structures"
                       :units "Structures"}
                      {:opt-id 3
                       :opt-label "Times Burned"
                       :filter "times-burned"
                       :units "Times"}])
(defonce speeds      [{:opt-id 0 :opt-label ".5x" :delay 2000}
                      {:opt-id 1 :opt-label "1x"  :delay 1000}
                      {:opt-id 2 :opt-label "2x"  :delay 500}
                      {:opt-id 3 :opt-label "5x"  :delay 200}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; API Calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn filtered-layers []
  (let [filter-text (u/find-key-by-id layer-types @*layer-type :filter)]
    (filterv (fn [{:keys [type]}] (= type filter-text))
             @layer-list)))

(defn get-current-layer-name []
  (or (:layer (get (filtered-layers) @*layer-idx))
      ""))

(defn get-current-layer-hour []
  (or (:hour (get (filtered-layers) @*layer-idx))
      0))

(defn get-current-layer-extent []
  (or (:extent (get (filtered-layers) @*layer-idx))
      [-124.83131903974008 32.36304641169675 -113.24176261416054 42.24506977982483]))

(defn get-data
  "Asynchronously fetches the JSON or XML resource at url. Returns a
  channel containing the result of calling process-fn on the response
  or nil if an error occurred."
  [process-fn url]
  (u/fetch-and-process url
                       {:method "get"
                        :headers {"Accept" "application/json, text/xml"
                                  "Content-Type" "application/json"}}
                       process-fn))

(defn process-legend! [response]
  (go
    (reset! legend-list
            (as-> (<p! (.json response)) data
              (u/try-js-aget data "Legend" 0 "rules" 0 "symbolizers" 0 "Raster" "colormap" "entries")
              (js->clj data)
              (remove (fn [leg] (= "nodata" (get leg "label"))) data)
              (doall data)))))

;; Use <! for synchronous behavior or leave it off for asynchronous behavior.
(defn get-legend! [layer]
  (get-data process-legend!
            (str "https://californiafireforecast.com:8443/geoserver/demo/wms"
                 "?SERVICE=WMS"
                 "&VERSION=1.3.0"
                 "&REQUEST=GetLegendGraphic"
                 "&FORMAT=application/json"
                 "&LAYER=" layer)))

(defn date-from-string [date time]
  (js/Date. (str (subs date 0 4) "-"
                 (subs date 4 6) "-"
                 (subs date 6 8) "T"
                 (subs time 0 2) ":00:00.000z")))

(defn process-capabilities! [response]
  (go
    (reset! layer-list
            (->> (-> (<p! (.text response))
                     ol/wms-capabilities
                     (u/try-js-aget "Capability" "Layer" "Layer"))
                 (remove #(str/starts-with? (aget % "Name") "lg-"))
                 (mapv (fn [layer]
                         (let [full-name        (aget layer "Name")
                               [type date time] (str/split full-name "_") ; TODO this might break if we expand file names
                               cur-date         (date-from-string date time)
                               base-date        (date-from-string "20200424" "130000")] ; TODO find first date for each group. This may come with model information
                           {:layer  full-name
                            :type   type
                            :extent (aget layer "EX_GeographicBoundingBox")
                            :date   (subs (.toISOString cur-date) 0 10)
                            :time   (str (subs (.toISOString cur-date) 11 16) " UTC")
                            :hour   (/ (- cur-date base-date) 1000 60 60)})))))))

;; Use <! for synchronous behavior or leave it off for asynchronous behavior.
(defn get-layers! []
  (get-data process-capabilities!
            (str "https://californiafireforecast.com:8443/geoserver/demo/wms"
                 "?SERVICE=WMS"
                 "&VERSION=1.3.0"
                 "&REQUEST=GetCapabilities"
                 "&NAMESPACE=demo")))

(defn process-point-info! [response]
  (go
    (reset! last-clicked-info
            (mapv (fn [pi li]
                    (merge (select-keys li [:time :date :hour :type])
                           {:band (u/try-js-aget pi "properties" "GRAY_INDEX")}))
                  (-> (<p! (.json response))
                      (u/try-js-aget "features"))
                  (filtered-layers)))))

;; Use <! for synchronous behavior or leave it off for asynchronous behavior.
(defn get-point-info! [point-info]
  (reset! last-clicked-info nil)
  (when point-info
    (let [layer-str (u/find-key-by-id layer-types @*layer-type :filter)]
      (get-data process-point-info!
                (str "https://californiafireforecast.com:8443/geoserver/demo/wms"
                     "?SERVICE=WMS"
                     "&VERSION=1.3.0"
                     "&REQUEST=GetFeatureInfo"
                     "&INFO_FORMAT=application/json"
                     "&LAYERS=demo:lg-" layer-str
                     "&QUERY_LAYERS=demo:lg-" layer-str
                     "&FEATURE_COUNT=1000"
                     "&TILED=true"
                     "&I=0"
                     "&J=0"
                     "&WIDTH=1"
                     "&HEIGHT=1"
                     "&CRS=EPSG:3857"
                     "&STYLES="
                     "&BBOX=" (str/join "," point-info))))))

(defn cycle-layer! [change]
  (swap! *layer-idx #(mod (+ change %) (count (filtered-layers))))
  (ol/swap-active-layer! (get-current-layer-name)))

(defn loop-animation! []
  (when @animate?
    (cycle-layer! 1)
    (js/setTimeout loop-animation! (u/find-key-by-id speeds @*speed :delay))))

(defn select-zoom! [zoom]
  (when-not (= zoom @*zoom)
    (reset! *zoom (max @minZoom
                       (min @maxZoom
                            zoom)))
    (ol/set-zoom! @*zoom)))

(defn select-layer-type! [id]
  (reset! *layer-type id)
  (ol/swap-active-layer! (get-current-layer-name))
  (get-point-info!       (ol/get-selected-point))
  (get-legend!           (get-current-layer-name)))

(defn select-base-map! [id]
  (reset! *base-map id)
  (ol/set-base-map-source! (u/find-key-by-id ol/base-map-options @*base-map :source)))

(defn init-map! []
  (go
    (<! (get-layers!))
    (get-legend! (get-current-layer-name))
    (ol/init-map! (get-current-layer-name))
    (ol/add-map-single-click! get-point-info!)
    (let [[cur min max] (ol/get-zoom-info)]
      (reset! *zoom cur)
      (reset! minZoom min)
      (reset! maxZoom max))
    (ol/add-map-zoom-end! select-zoom!)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; UI Styles
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn $app-header []
  {:align-items     "center"
   :display         "flex"
   :height          "2.5rem"
   :justify-content "center"
   :position        "relative"
   :width           "100%"})

(defn $tool-label [selected?]
  (if selected?
    {:color "white"}
    {}))

(defn $legend-box []
  {:background-color "white"
   :bottom           "1rem"
   :border           "1px solid black"
   :border-radius    "5px"
   :right            ".5rem"
   :position         "absolute"
   :z-index          "100"})

(defn $legend-color [color]
  {:background-color color
   :height           "1rem"
   :margin-right     ".5rem"
   :width            "1rem"})

(defn $time-slider []
  {:background-color "white"
   :border           "1px solid black"
   :border-radius    "5px"
   :display          "flex"
   :margin-right     "auto"
   :margin-left      "auto"
   :left             "0"
   :right            "0"
   :padding          ".75rem"
   :position         "absolute"
   :bottom           "1rem"
   :width            "fit-content"
   :z-index          "100"})

(defn $collapsible-panel [show?]
  {:background-color "white"
   :border-right     "2px solid black"
   :height           "100%"
   :position         "absolute"
   :transition       "all 200ms ease-in"
   :width            "20rem"
   :z-index          "1000"
   :left             (if show? "0" "-20rem")})

(defn $collapse-button []
  {:background-color "white"
   :border-right     "2px solid black"
   :border-top       "2px solid black"
   :border-bottom    "2px solid black"
   :border-left      "4px solid white"
   :border-radius    "0 5px 5px 0"
   :height           "2rem"
   :position         "absolute"
   :right            "-2rem"
   :top              ".5rem"
   :width            "2rem"})

(defn $dropdown []
  {:background-color "white"
   :border           "1px solid"
   :border-color     ($/color-picker :sig-brown)
   :border-radius    "2px"
   :font-family      "inherit"
   :height           "2rem"
   :padding          ".25rem .5rem"})

(defn $zoom-slider []
  {:background-color "white"
   :border           "1px solid black"
   :border-radius    "5px"
   :right            "5rem"
   :position         "absolute"
   :bottom           "5rem"
   :display          "flex"
   :transform        "rotate(270deg)"
   :width            "10rem"
   :height           "2rem"
   :z-index          "100"})

(defn $p-zoom-button-common []
  (with-meta
    {:border-radius "4px"
     :cursor        "pointer"
     :font-weight   "bold"
     :transform     "rotate(90deg)"}
    {:pseudo {:hover {:background-color ($/color-picker :sig-brown 0.1)}}}))

(defn $pop-up-box []
  {:background-color "white"
   :border-radius    "5px"
   :box-shadow       "0 0 2px 1px rgba(0,0,0,0.1)"
   :margin-bottom    ".5rem"
   :padding          ".25rem .5rem"})

(defn $pop-up-arrow []
  {:background-color "white"
   :bottom           "0"
   :height           "1rem"
   :left             "0"
   :margin-left      "auto"
   :margin-right     "auto"
   :right            "0"
   :position         "absolute"
   :transform        "rotate(45deg)"
   :width            "1rem"
   :z-index          "-1"})

(defn $vega-box []
  {:background-color "white"
   :border           "1px solid black"
   :border-radius    "5px"
   :height           "15rem"
   :overflow         "hidden"
   :padding-top      "1rem"
   :position         "absolute"
   :right            ".5rem"
   :top              ".5rem"
   :width            "25rem"
   :z-index          "100"})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; UI Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn legend-box []
  [:div#legend-box {:style ($legend-box)}
   [:div {:style {:display "flex" :flex-direction "column"}}
    (doall (map-indexed (fn [i leg]
                          ^{:key i}
                          [:div {:style ($/combine $/flex-row {:justify-content "flex-start"})}
                           [:div {:style ($legend-color (get leg "color"))}]
                           [:label (get leg "label")]])
                        @legend-list))]])

(defn time-slider []
  [:div#time-slider {:style ($time-slider)}
   [:input {:style {:width "10rem"}
            :type "range" :min "0" :max (dec (count (filtered-layers))) :value @*layer-idx
            :on-change #(do (reset! *layer-idx (u/input-int-value %))
                            (ol/swap-active-layer! (get-current-layer-name)))}]
   [:button {:style {:padding "0 .25rem" :margin-left ".5rem"}
             :type "button"
             :on-click #(cycle-layer! -1)}
    "<<"]
   [:button {:style {:padding "0 .25rem"}
             :type "button"
             :on-click #(do (swap! animate? not)
                            (loop-animation!))}
    (if @animate? "Stop" "Play")]
   [:button {:style {:padding "0 .25rem"}
             :type "button"
             :on-click #(cycle-layer! 1)}
    ">>"]
   [:select {:style ($/combine $dropdown)
             :value (or @*speed 1)
             :on-change #(reset! *speed (u/input-int-value %))}
    (doall (map (fn [{:keys [opt-id opt-label]}]
                  [:option {:key opt-id :value opt-id} opt-label])
                speeds))]])

(defn zoom-slider []
  [:div#zoom-slider {:style ($zoom-slider)}
   [:span {:class (<class $p-zoom-button-common)
           :style ($/combine ($/fixed-size "1.75rem") {:margin "1px" :padding ".15rem 0 0 .75rem"})
           :on-click #(select-zoom! (dec @*zoom))}
    "-"]
   [:input {:style {:min-width "0"}
            :type "range" :min @minZoom :max @maxZoom :value @*zoom
            :on-change #(select-zoom! (u/input-int-value %))}]
   [:span {:class (<class $p-zoom-button-common)
           :style ($/combine ($/fixed-size "1.75rem") {:margin "1px" :padding ".15rem 0 0 .5rem"})
           :on-click #(select-zoom! (inc @*zoom))}
    "+"]
   [:span {:class (<class $p-zoom-button-common)
           :style ($/combine ($/fixed-size "1.75rem") {:margin "1px" :padding ".25rem 0 0 .5rem" :font-size ".9rem"})
           :title "Zoom to Extent"
           :on-click #(ol/zoom-to-extent! (get-current-layer-extent))}
    "E"]])

(defn layer-dropdown []
  [:div {:style {:display "flex" :flex-direction "column" :padding "0 3rem"}}
   [:label "Select Layer"]
   [:select {:style ($dropdown)
             :value (or @*layer-type -1)
             :on-change #(select-layer-type! (u/input-int-value %))}
    (doall (map (fn [{:keys [opt_id opt_label]}]
                  [:option {:key opt_id :value opt_id} opt_label])
                layer-types))]])

(defn to-hex-str [num]
  (let [hex-num (.toString (.round js/Math num) 16)]
    (if (= 2 (count hex-num))
      hex-num
      (str "0" hex-num))))

(defn interp-color [from to ratio]
  (when (and from to)
    (let [fr (js/parseInt (subs from 1 3) 16)
          fg (js/parseInt (subs from 3 5) 16)
          fb (js/parseInt (subs from 5 7) 16)
          tr (js/parseInt (subs to   1 3) 16)
          tg (js/parseInt (subs to   3 5) 16)
          tb (js/parseInt (subs to   5 7) 16)]
      (str "#"
           (to-hex-str (+ fr (* ratio (- tr fr))))
           (to-hex-str (+ fg (* ratio (- tg fg))))
           (to-hex-str (+ fb (* ratio (- tb fb))))))))

(defn create-stops []
  (let [max-band (reduce (fn [acc cur] (max acc (:band cur))) 1.0 @last-clicked-info)]
    (reductions
     (fn [last cur] (let [last-q (get last :quantity  0.0)
                          cur-q  (get cur  "quantity" 0.0)]
                      {:quantity cur-q
                       :offset   (min (/ cur-q max-band) 1.0)
                       :color    (if (< last-q max-band cur-q)
                                   (interp-color (get last :color)
                                                 (get cur  "color")
                                                 (/ (- max-band last-q)
                                                    (- cur-q last-q)))
                                   (get cur "color"))}))
     {:offset 0.0
      :color  (get (first @legend-list) "color")}
     (rest @legend-list))))

(defn create-scale []
  {:type   "linear"
   :domain (mapv #(get % "quantity") @legend-list)
   :range  (mapv #(get % "color")    @legend-list)})

(defn layer-line-plot []
  (let [units (u/find-key-by-id layer-types @*layer-type :units)]
    {:width    "container"
     :height   "container"
     :padding  {:left "16" :top "0" :right "16" :bottom "32"}
     :data     {:values (or @last-clicked-info [])}
     :layer    [{:encoding {:x {:field "hour" :type "quantitative" :title "Hour"}
                            :y {:field "band" :type "quantitative" :title units}
                            :tooltip [{:field "band" :title units  :type "nominal"}
                                      {:field "date" :title "Date" :type "nominal"}
                                      {:field "time" :title "Time" :type "nominal"}]}
                 :layer [{:mark {:type        "line"
                                 :interpolate "monotone"
                                 :stroke      {:x2 0
                                               :y1 1
                                               :gradient "linear"
                                               :stops    (create-stops)}}}
                         ;; Layer with all points for selection
                         {:mark      {:type   "point"
                                      :opacity 0}
                          :selection {:point-hover {:type  "single"
                                                    :on    "mouseover"
                                                    :empty "none"}}}
                         {:transform [{:filter {:or [{:field "hour" :lt (get-current-layer-hour)}
                                                     {:field "hour" :gt (get-current-layer-hour)}]}}]
                          :mark     {:type   "point"
                                     :filled true}
                          :encoding {:size {:condition {:selection :point-hover :value 150}
                                            :value 75}
                                     :color {:field  "band"
                                             :type   "quantitative"
                                             :scale  (create-scale)
                                             :legend false}}}
                         {:transform [{:filter {:field "hour" :equal (get-current-layer-hour)}}]
                          :mark {:type   "point"
                                 :filled false
                                 :fill   "black"
                                 :stroke "black"}
                          :encoding {:size {:condition {:selection :point-hover :value 150}
                                            :value 75}}}]}]}))

(defn render-vega [spec elem]
  (when (and spec (seq (get-in spec [:data :values])))
    (go
      (try
        (let [result (<p! (js/vegaEmbed elem
                                        (clj->js spec)
                                        (clj->js {:renderer "canvas"
                                                  :mode     "vega-lite"})))]
          (-> result .-view (.addEventListener
                             "click"
                             (fn [_ data]
                               (when-let [index (or (u/try-js-aget data "datum" "datum" "_vgsid_")
                                                    (u/try-js-aget data "datum" "_vgsid_"))]
                                 (reset! *layer-idx (dec ^js/integer index))
                                 (ol/swap-active-layer! (get-current-layer-name)))))))
        (catch ExceptionInfo e (js/console.log (ex-cause e)))))))

(defn vega-box [props]
  (r/create-class
   {:component-did-mount
    (fn [this] (render-vega (:spec props) (rd/dom-node this)))

    :component-did-update
    (fn [this _] (render-vega (:spec (r/props this)) (rd/dom-node this)))

    :render
    (fn [] [:div#vega-box {:style ($vega-box)}])})) ; TODO render no data / loading message

(defn panel-dropdown [title state options call-back]
  [:div {:style {:display "flex" :flex-direction "column"}}
   [:label title]
   [:select {:style ($dropdown)
             :value (or @state -1)
             :on-change #(call-back (u/input-int-value %))}
    (doall (map (fn [{:keys [opt-id opt-label]}]
                  [:option {:key opt-id :value opt-id} opt-label])
                options))]])

(defn collapsible-panel []
  (r/with-let [show-panel?       (r/atom true)
               active-opacity    (r/atom 70.0)
               hillshade-opacity (r/atom 50.0)
               show-hillshade?   (r/atom false)]
    [:div#collapsible-panel {:style ($collapsible-panel @show-panel?)}
     [:div {:style ($collapse-button)
            :on-click #(swap! show-panel? not)}
      [:label {:style {:padding-top "2px"}} (if @show-panel? "<<" ">>")]]
     [:div {:style {:display "flex" :flex-direction "column" :padding "3rem"}}
      [:div#baselayer
       [panel-dropdown "Base Layer" *base-map ol/base-map-options select-base-map!]
       [:div {:style {:margin-top ".5rem"}}
        [:div {:style {:display "flex"}}
         [:input {:style {:margin ".25rem .5rem 0 0"}
                  :type "checkbox"
                  :on-click #(do (swap! show-hillshade? not)
                                 (ol/set-visible-by-title! "hillshade" @show-hillshade?))}]
         [:label "Show hill shade"]]
        (when @show-hillshade?
          [:<> [:label (str "Opacity: " @hillshade-opacity)]
           [:input {:style {:width "100%"}
                    :type "range" :min "0" :max "100" :value @hillshade-opacity
                    :on-change #(do (reset! hillshade-opacity (u/input-int-value %))
                                    (ol/set-opacity-by-title! "hillshade" (/ @hillshade-opacity 100.0)))}]])]]
      [:div#activelayer {:style {:margin-top "2rem"}}
       [panel-dropdown "Active Layer" *layer-type layer-types select-layer-type!]
       [:div {:style {:margin-top ".5rem"}}
        [:label (str "Opacity: " @active-opacity)]
        [:input {:style {:width "100%"}
                 :type "range" :min "0" :max "100" :value @active-opacity
                 :on-change #(do (reset! active-opacity (u/input-int-value %))
                                 (ol/set-opacity-by-title! "active" (/ @active-opacity 100.0)))}]]]]]))

(defn control-layer []
  [:div {:style {:height "100%" :position "absolute" :width "100%"}}
   [collapsible-panel]
   [legend-box]
   [vega-box {:spec (layer-line-plot)}]
   [time-slider]
   [zoom-slider]])

(defn pop-up []
  [:div#popup
   [:div {:style ($pop-up-box)}
    [:label (if @last-clicked-info
              (str (:band (get @last-clicked-info @*layer-idx))
                   " "
                   (u/find-key-by-id layer-types @*layer-type :units))
              "...")]]
   [:div {:style ($pop-up-arrow)}]])

(defn root-component [_]
  (r/create-class
   {:component-did-mount
    (fn [_] (init-map!))

    :reagent-render
    (fn [_]
      [:div {:style ($/combine $/root {:height "100%" :padding 0})}
       [:div {:class "bg-yellow"
              :style ($app-header)}
        [:span
         [:label {:style ($tool-label false)} "Fire Weather"]
         [:label {:style ($/combine [$tool-label false] [$/margin "2rem" :h])} "Active Fire Forecast"]
         [:label {:style ($tool-label true)} "Risk Forecast"]]
        [:label {:style {:position "absolute" :right "3rem"}} "Login"]]
       [:div {:style {:height "100%" :position "relative" :width "100%"}}
        [control-layer]
        [:div#map {:style {:height "100%" :position "absolute" :width "100%"}}
         [pop-up]]]])}))
