(ns pyregence.components.svg-icons)

(defn layers [& {:keys [height width]}]
  [:svg {:enable-background "new 0 0 96 96"
         :height            height
         :viewBox           "0 0 96 96"
         :width             width}
   [:polygon {:points "87,61.516 48,81.016 9,61.516 0,66.016 48,90.016 96,66.016 "}]
   [:polygon {:points "87,44.531 48,64.031 9,44.531 0,49.031 48,73.031 96,49.031 "}]
   [:path {:d "M48,16.943L78.111,32L48,47.057L17.889,32L48,16.943 M48,8L0,32l48,24l48-24L48,8L48,8z"}]])

(defn center-on-point [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d    "M0 0h48v48h-48z"
           :fill "none"}]
   [:path {:d "M24 16c-4.42 0-8 3.58-8 8s3.58 8 8 8 8-3.58 8-8-3.58-8-8-8zm17.88
               6c-.92-8.34-7.54-14.96-15.88-15.88v-4.12h-4v4.12c-8.34.92-14.96 7.54-15.88 15.88h-4.12v4h4.12c.92
               8.34 7.54 14.96 15.88 15.88v4.12h4v-4.12c8.34-.92 14.96-7.54 15.88-15.88h4.12v-4h-4.12zm-17.88
               16c-7.73 0-14-6.27-14-14s6.27-14 14-14 14 6.27 14 14-6.27 14-14 14z"}]])

(defn extent [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d    "M0 0h48v48h-48z"
           :fill "none"}]
   [:path {:d "M6 10v8h4v-8h8v-4h-8c-2.21 0-4 1.79-4 4zm4 20h-4v8c0 2.21 1.79 4 4 4h8v-4h-8v-8zm28 8h-8v4h8c2.21
               0 4-1.79 4-4v-8h-4v8zm0-32h-8v4h8v8h4v-8c0-2.21-1.79-4-4-4z"}]])

(defn info [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d    "M0 0h48v48h-48z"
           :fill "none"}]
   [:path {:d "M22 34h4v-12h-4v12zm2-30c-11.05 0-20 8.95-20 20s8.95 20 20 20 20-8.95 20-20-8.95-20-20-20zm0 36c-8.82
               0-16-7.18-16-16s7.18-16 16-16 16 7.18 16 16-7.18 16-16 16zm-2-22h4v-4h-4v4z"}]])

(defn legend [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d "M8 21c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3zm0-12c-1.66 0-3 1.34-3 3s1.34
               3 3 3 3-1.34 3-3-1.34-3-3-3zm0 24.33c-1.47 0-2.67 1.19-2.67 2.67s1.2 2.67 2.67 2.67 2.67-1.19
               2.67-2.67-1.2-2.67-2.67-2.67zm6 4.67h28v-4h-28v4zm0-12h28v-4h-28v4zm0-16v4h28v-4h-28z"}]
   [:path {:d    "M0 0h48v48h-48z"
           :fill "none"}]])

(defn my-location [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d    "M0-.17h48v48h-48z"
           :fill "none"}]
   [:path {:d "M41.88 22.17c-.92-8.34-7.54-14.96-15.88-15.88v-4.12h-4v4.12c-8.34.92-14.96 7.54-15.88
               15.88h-4.12v4h4.12c.92 8.34 7.54 14.96 15.88 15.88v4.12h4v-4.12c8.34-.92 14.96-7.54
               15.88-15.88h4.12v-4h-4.12zm-17.88 16c-7.73 0-14-6.27-14-14s6.27-14 14-14 14 6.27 14
               14-6.27 14-14 14z"}]])

(defn next-button [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d "M12 36l17-12-17-12v24zm20-24v24h4V12h-4z"}]
   [:path {:d    "M0 0h48v48H0z"
           :fill "none"}]])

(defn pause-button [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d "M12 38h8V10h-8v28zm16-28v28h8V10h-8z"}]
   [:path {:d    "M0 0h48v48H0z"
           :fill "none"}]])

(defn pin [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 32 32"
         :width   width}
   [:path {:d "M4 12 A12 12 0 0 1 28 12 C28 20, 16 32, 16 32 C16 32, 4 20 4 12 M11
               12 A5 5 0 0 0 21 12 A5 5 0 0 0 11 12 Z"}]])

(defn play-button [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d    "M-838-2232H562v3600H-838z"
           :fill "none"}]
   [:path {:d "M16 10v28l22-14z"}]
   [:path {:d    "M0 0h48v48H0z"
           :fill "none"}]])

(defn previous-button [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d "M12 12h4v24h-4zm7 12l17 12V12z"}]
   [:path {:d    "M0 0h48v48H0z"
           :fill "none"}]])

(defn zoom-in [& {:keys [height width]}]
  [:svg {:height    height
         :viewBox   "0 0 512 512"
         :width     width
         :xml-space "preserve"}
   [:polygon {:points "448,224 288,224 288,64 224,64 224,224 64,224 64,288 224,288
                       224,448 288,448 288,288 448,288 "}]])

(defn zoom-out [& {:keys [height width]}]
  [:svg {:height    height
         :viewBox   "0 0 512 512"
         :width     width
         :xml-space "preserve"}
   [:rect {:height "64" :width "384" :x "64" :y "224"}]])

(defn close [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d "M38 12.83l-2.83-2.83-11.17 11.17-11.17-11.17-2.83 2.83 11.17 11.17-11.17 11.17 2.83 2.83
               11.17-11.17 11.17 11.17 2.83-2.83-11.17-11.17z"}]
   [:path {:d    "M0 0h48v48h-48z"
           :fill "none"}]])

(defn help [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 16 16"
         :width   width}
   [:g {:id           "Icons with numbers"
        :fill         "none"
        :fill-rule    "evenodd"
        :stroke       "none"
        :stroke-width "1"}
    [:g {:id        "Group"
         :style     {:fill "currentColor"}
         :transform "translate(-48.000000, -432.000000)"}
     [:path {:d "M54.8796844,443.0591 L54.8796844,445 L57.2307692,445 L57.2307692,443.0591 Z M56,448
                 C51.5817218,448 48,444.418278 48,440 C48,435.581722 51.5817218,432 56,432 C60.4182782,432
                 64,435.581722 64,440 C64,444.418278 60.4182782,448 56,448 Z M53.5700197,435.51041
                 C52.5864514,436.043208 52.0631167,436.947609 52,438.22364 L54.2800789,438.22364 C54.2800789,437.852024
                 54.4076253,437.493845 54.6627219,437.149093 C54.9178185,436.804341 55.3504243,436.631968
                 55.9605523,436.631968 C56.5811997,436.631968 57.0085458,436.771881 57.2426036,437.051713
                 C57.4766613,437.331544 57.5936884,437.641592 57.5936884,437.981867 C57.5936884,438.277369
                 57.4884955,438.548241 57.2781065,438.794493 L56.8205128,439.190732 L56.2445759,439.573539
                 C55.6765258,439.949633 55.3241295,440.282067 55.1873767,440.570853 C55.0506239,440.859639
                 54.9664696,441.382356 54.9349112,442.139019 L57.0650888,442.139019 C57.0703485,441.780835
                 57.1045362,441.516679 57.1676529,441.346541 C57.2675876,441.077903 57.4700839,440.842849
                 57.7751479,440.64137 L58.3353057,440.271995 C58.9033559,439.895901 59.28731,439.586972
                 59.4871795,439.345198 C59.8290615,438.946718 60,438.456461 60,437.874412 C60,436.925225
                 59.6068415,436.208867 58.8205128,435.725319 C58.0341841,435.241771 57.0466858,435 55.8579882,435
                 C54.9533157,435 54.1906671,435.170135 53.5700197,435.51041 Z M53.5700197,435.51041"}]]]])

(defn flame [& {:keys [height width]}]
  [:svg {:enable-background "new 0 0 96 96"
         :height            height
         :viewBox           "0 0 855.492 855.492"
         :width             width}
   [:path {:d "M270.436,853.938c8.801,5.399,19.101-4.301,14-13.301c-13.399-23.8-21-51.199-21-80.5c0-62,
               34.301-111.6,84.801-144.6 c44.699-29.2,32.5-66.9,39.899-111.8c4.4-26.601,21-50.8,34.9-66.9c5.6-6.5,
               16.1-3.399,17.5,5c8.2,50.4,73.6,136.2,111.3,192.2 c43.4,64.6,40.6,121.5,40.3,125.8c0,0.2,0,0.4,0,
               0.601c-0.1,29.1-7.7,56.399-21,80c-5.1,9,5.2,18.8,14,13.3 c69.4-42.9,119.4-113.7,
               136.101-193.601c13.3-63.6,5.8-129.3-15.7-190.1c-12.7-35.9-30.2-70-51.5-101.6
               c-68.9-102.5-188.8-259.601-203.2-351.5c-2.7-16.9-24.1-22.9-35.2-9.9c-0.2,0.2-20.6,22.7-33.399,
               47.6 c-10.9,21.3-19.801,43.6-24.9,66.9c-9.6,43.9-7.9,90.9,3.1,134.4c3.601,14.2,8.2,28.1,13.801,
               41.6 c5.399,13,11.199,26.1,12.199,40.3c1.601,26.5-22.399,49.4-48.399,49.4c-23.3,
               0-42.601-14-47.601-40.4 c-1.3-6.8-8.899-10.399-14.899-6.899c-88.5,52.1-147.9,148.399-147.9,
               258.5C127.836,706.438,184.836,801.137,270.436,853.938z"}]])

(defn terrain [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 128 128"
         :width   width}
   [:path {:d      "M 5.5,99 L 43.4,26.2 56.2,51 77.8,14.9 127.8,98.7 Z"
           :fill   "currentColor"
           :stroke "none"}]])

(defn dropdown-arrow [stroke-color & {:keys [height width]}]
  [:svg {:eight   height
         :width   width
         :xmlns   "http://www.w3.org/2000/svg"
         :viewBox "0 0 16 16"}
   [:path {:d               "M2 5l6 6 6-6"
           :fill            "none"
           :stroke          stroke-color
           :stroke-linecap  "round"
           :stroke-linejoin "round"
           :stroke-width    "2"}]])

(defn camera [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 100 100"
         :width   width}
   [:g {:transform "matrix(1,0,0,1,-1.05324,1.00207)"}
    [:path {:d "M90.8,23.4C88.7,22.3 86.1,22.3 84.1,23.6L69.5,33C69,26.3 63.4,21.1 56.7,21.1L20.5,21.1
                C13.4,21.2 7.7,26.9 7.7,34L7.7,64C7.7,71.1 13.4,76.8 20.5,76.8L56.8,76.8C63.5,76.8 69.1,
                71.6 69.6,64.9L84.2,74.3C86.2,75.6 88.8,75.7 90.9,74.5C93.1,73.3 94.4,71 94.4,68.6
                L94.4,29.3C94.3,26.9 93,24.6 90.8,23.4Z"
            :stroke "none"
            :fill "currentColor"}]]])

(defn share [& {:keys [height width]}]
  [:svg {:viewBox "0 0 70 70"
         :style   {:clip-rule    "evenodd"
                   :fill-rule    "evenodd"
                   :stroke-line  "join:round"
                   :stroke-miter "limit:2"}
         :height  height
         :width   width}
   [:g {:transform "matrix(0.898341,0,0,0.874937,-9.91644,-8.74985)"}
    [:path {:d "M88.96,32.38C88.96,31.8 88.67,31.27 88.18,30.97L56.25,10.27C55.72,9.93 55.06,9.91
                54.51,10.22C53.97,10.51 53.62,11.09 53.62,11.72L53.65,18.7C53.55,18.73 53.46,18.75
                53.36,18.79C52.75,18.99 52.15,19.2 51.45,19.42C47.55,20.82 43.76,22.67 40.15,24.95C33.28,29.3
                27.11,35.31 22.38,42.3C17.78,49.06 14.41,56.87 12.64,64.85C10.9,72.56 10.58,80.75
                11.67,88.53C11.79,89.38 12.5,89.99 13.36,89.99C14.21,89.99 14.93,89.38 15.05,88.53C16.05,81.47
                18.32,74.66 21.6,68.84C24.72,63.31 28.87,58.46 33.63,54.83C38.12,51.41 43.23,49.04
                48.37,47.99C50.12,47.63 51.93,47.43 53.77,47.36L53.79,53.42C53.79,54.03 54.13,54.61
                54.69,54.9C55.23,55.21 55.9,55.17 56.43,54.83L88.19,33.81C88.67,33.49 88.96,32.96 88.96,32.38Z"}]]])

(defn flag [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 24 24"
         :width   width}
   [:path {:d "m19.25 8 2.55-3.4a1.0007 1.0007 0 0 0 -.8-1.6h-12a1 1 0 0 0 -2 0v17h-1a1 1 0 0 0 0 2h4a1 1
               0 0 0 0-2h-1v-7h12a1.0007 1.0007 0 0 0 .8-1.6z"}]])

(defn magnify-zoom-in [& {:keys [height width]}]
  [:svg {:enable-background "new 0 0 32 32"
         :height            height
         :viewBox           "0 0 32 32"
         :width             width}
   [:path {:d "m27.414 24.586-5.077-5.077c1.049-1.581 1.663-3.474 1.663-5.509 0-5.514-4.486-10-10-10s-10
               4.486-10 10 4.486 10 10 10c2.035 0 3.928-.614 5.509-1.663l5.077 5.077c.78.781 2.048.781
               2.828 0 .781-.781.781-2.047 0-2.828zm-20.414-10.586c0-3.86 3.14-7 7-7s7 3.14 7 7-3.14 7-7
               7-7-3.14-7-7z"}]
   [:path {:d "m19 14c0 .552-.448 1-1 1h-3v3c0 .552-.448 1-1 1s-1-.448-1-1v-3h-3c-.552 0-1-.448-1-1s.448-1
               1-1h3v-3c0-.552.448-1 1-1s1 .448 1 1v3h3c.552 0 1 .448 1 1z"}]])

(defn binoculars [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 48 48"
         :width   width}
   [:path {:d "m11.6 9.9c1-.3 2-.4 3-.4 1.6 0 3.2.4 4.6 1.1-1-2.2-3.5-3.2-5.7-2.3-.8.4-1.4.9-1.9 1.6z"}]
   [:path {:d "m28.8 10.6c1.4-.7 3-1.1 4.6-1.1 1 0 2 .2 3 .4-.9-1.2-2.2-1.9-3.7-1.9-1.7 0-3.3 1-3.9 2.6z"}]
   [:path {:d "m28.8 20c4-2.6 9.2-2.7 13.2-.2l-3.1-6.2c-.1-.2-.2-.3-.4-.4 0 0-1.6-1.6-5.2-1.6-2.3 0-3.7.6-4.9
               1.7-.2.1-.3.4-.4.6l-.5 3.1h-1.1c-.3-.2-.5-.4-.8-.5-1-.4-2.1-.4-3.1
               0-.3.1-.6.3-.8.5h-1.1l-.6-3.2c0-.1-.1-.3-.1-.3-1.9-1.6-3.7-1.9-5.2-1.9-1.7 0-2.6-.1-5.2
               1.6-.2.1-.3.2-.4.4l-3 6.2c4-2.4 9.1-2.4 13.1.2.7.5 4.8 5.5 4.8 5.5s4-5 4.8-5.5z"}]
   [:path {:d "m12.5 20c-5.8 0-10.5 4.7-10.5 10.5s4.7 10.5 10.5 10.5 10.5-4.7 10.5-10.5-4.7-10.5-10.5-10.5zm0
               17.4c-3.8 0-7-3.1-7-7 0-3.8 3.1-7 7-7s7 3.1 7 7-3.2 7-7 7z"}]
   [:path {:d "m35.5 20c-5.8 0-10.5 4.7-10.5 10.5s4.7 10.5 10.5 10.5 10.5-4.7 10.5-10.5-4.7-10.5-10.5-10.5zm0
               17.4c-3.8 0-7-3.1-7-7 0-3.8 3.1-7 7-7s7 3.1 7 7-3.2 7-7 7z"}]])

(defn clock [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 512 512"
         :width   width}
   [:path {:d "m256 0c-141.164062 0-256 114.835938-256 256s114.835938 256 256 256 256-114.835938 256-256-114.835938-256-256-256z
               m121.75 388.414062 c-4.160156 4.160157-9.621094 6.253907-15.082031 6.253907-5.460938 0-10.925781-2.09375-15.082031-6.253907
               l-106.667969-106.664062c-4.011719-3.988281-6.25-9.410156-6.25-15.082031v-138.667969c0-11.796875 9.554687-21.332031 21.332031-21.332031
               s21.332031 9.535156 21.332031 21.332031v129.835938l100.417969 100.414062c8.339844 8.34375 8.339844 21.824219 0 30.164062zm0 0"}]])

(defn measure-ruler [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 511.016 511.016"
         :width   width}
   [:g [:path {:d "m485.408 4.409-101.893 101.893 42.106 42.106c5.858 5.858 5.858 15.355 0 21.213-5.857 5.857-15.355
                   5.858-21.213 0l-42.107-42.107-42.787 42.787 42.106 42.106c5.858 5.858 5.858 15.355 0 21.213-5.857
                   5.857-15.355 5.858-21.213 0l-42.107-42.107-42.787 42.787 42.106 42.106c5.858 5.858 5.858 15.355 0
                   21.213-5.857 5.857-15.355 5.858-21.213 0l-42.107-42.107-42.787 42.787 42.106 42.106c5.858 5.858 5.858
                   15.355 0 21.213-5.857 5.857-15.355 5.858-21.213 0l-42.107-42.107-42.787 42.787 42.106 42.106c5.858
                   5.858 5.858 15.355 0 21.213-5.857 5.857-15.355 5.858-21.213 0l-42.107-42.107-101.889 101.899c-9.422 9.422-2.738
                   25.607 10.606 25.607h481c8.284 0 15-6.716 15-15v-481c.001-13.325-16.17-20.042-25.606-10.607zm-71.393
                   394.607c0 8.284-6.716 15-15 15h-136c-13.318 0-20.048-16.165-10.606-25.607l136-136c9.423-9.423 25.606-2.737
                   25.606 10.607z"}]]])

(defn right-arrow [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 451.846 451.847"
         :width   width}
   [:path {:d "M345.441,248.292L151.154,442.573c-12.359,12.365-32.397,12.365-44.75,0c-12.354-12.354-12.354-32.391,0-44.744\n\t\tL278.318,225.92L106.409,54.017c-12.354-12.359-12.354-32.394,0-44.748c12.354-12.359,32.391-12.359,44.75,0l194.287,194.284\n\t\tc6.177,6.18,9.262,14.271,9.262,22.366C354.708,234.018,351.617,242.115,345.441,248.292z"}]])

(defn return [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 32 32"
         :width   width}
    [:path {:d "m19.5078 8.3394h-11.5585l1.1722-1.7419a1.5326 1.5326 0 0 0 -1.2683-2.3931h-.0032a1.67 1.67 0 0 0 -1.3042.6268l-3.6065 4.5088a.8.8 0 0 0 0 .9995l3.6069 4.5085a1.67 1.67 0 0 0 1.3042.6269h.0028a1.5326 1.5326 0 0 0 1.2683-2.393l-1.1725-1.7425h11.5588a6.728 6.728 0 1 1 0 13.456h-3.2715a1.5 1.5 0 0 0 0 3h3.2715a9.728 9.728 0 1 0 0-19.456z"}]])

(defn admin-user [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "0 0 460.8 460.8"
         :width   width}
   [:path {:d "M230.432,239.282c65.829,0,119.641-53.812,119.641-119.641C350.073,53.812,296.261,0,230.432,0
               S110.792,53.812,110.792,119.641S164.604,239.282,230.432,239.282z"}]
   [:path {:d "M435.755,334.89c-3.135-7.837-7.314-15.151-12.016-21.943c-24.033-35.527-61.126-59.037-102.922-64.784
               c-5.224-0.522-10.971,0.522-15.151,3.657c-21.943,16.196-48.065,24.555-75.233,24.555s-53.29-8.359-75.233-24.555
               c-4.18-3.135-9.927-4.702-15.151-3.657c-41.796,5.747-79.412,29.257-102.922,64.784c-4.702,6.792-8.882,14.629-12.016,21.943
               c-1.567,3.135-1.045,6.792,0.522,9.927c4.18,7.314,9.404,14.629,14.106,20.898c7.314,9.927,15.151,18.808,24.033,27.167
               c7.314,7.314,15.673,14.106,24.033,20.898c41.273,30.825,90.906,47.02,142.106,47.02s100.833-16.196,142.106-47.02
               c8.359-6.269,16.718-13.584,24.033-20.898c8.359-8.359,16.718-17.241,24.033-27.167c5.224-6.792,9.927-13.584,14.106-20.898
               C436.8,341.682,437.322,338.024,435.755,334.89z"}]])

(defn refresh [& {:keys [height width]}]
  [:svg {:height            height
         :viewBox           "0 0 24 24"
         :width             width}
   [:path {:d "m19.6025 12.6348c-.5586-.085-1.0547.2979-1.1348.8438-.2012 1.3711-.834
               2.6221-1.8301 3.6182-2.5352 2.5352-6.6572 2.5332-9.1914 0-2.5337-2.5342-2.5337-6.6577
               0-9.1914.9531-.9526 2.1563-1.5737 3.5029-1.7998.5791-.1099 1.2017-.1289 1.8477-.0557.887.1021
               1.7126.3964 2.466.8285l-1.3019.2223c-.5439.0933-.9102.6099-.8164 1.1543.083.4873.5059.8315.9844.8315.0557
               0 .1123-.0044.1699-.0142l3.4902-.5967c.2607-.0449.4941-.1914.6475-.4082.1533-.2163.2139-.4849.1689-.7466l-.5977-3.4897c-.0918-.5439-.6016-.9082-1.1543-.8169-.5439.0933-.9102.6104-.8164
               1.1548l.1573.9185c-.9679-.543-2.0356-.8943-3.17-1.0249-.8496-.0967-1.6738-.0698-2.4282.0747-1.7368.291-3.3149
               1.105-4.564 2.354-3.3135 3.3135-3.3135 8.7051 0 12.0195 1.6567 1.6572 3.8335 2.4854 6.0098 2.4854 2.1768 0 4.3525-.8281
               6.0098-2.4854 1.3018-1.3018 2.1299-2.9414 2.3945-4.7412.0802-.5469-.2978-1.0548-.8437-1.1348z"}]])

(defn trash [& {:keys [height width]}]
  [:svg {:height  height
         :viewBox "-40 0 427 427.00131"
         :width   width}
   [:path {:d "m232.398438 154.703125c-5.523438 0-10 4.476563-10 10v189c0 5.519531 4.476562
               10 10 10 5.523437 0 10-4.480469 10-10v-189c0-5.523437-4.476563-10-10-10zm0 0"}]
   [:path {:d "m114.398438 154.703125c-5.523438 0-10 4.476563-10 10v189c0 5.519531 4.476562
               10 10 10 5.523437 0 10-4.480469 10-10v-189c0-5.523437-4.476563-10-10-10zm0 0"}]
   [:path {:d "m28.398438 127.121094v246.378906c0 14.5625 5.339843 28.238281 14.667968 38.050781
               9.285156 9.839844 22.207032 15.425781 35.730469 15.449219h189.203125c13.527344-.023438
               26.449219-5.609375 35.730469-15.449219 9.328125-9.8125 14.667969-23.488281
               14.667969-38.050781v-246.378906c18.542968-4.921875 30.558593-22.835938
               28.078124-41.863282-2.484374-19.023437-18.691406-33.253906-37.878906-33.257812h-51.199218v-12.5c.058593-10.511719-4.097657-20.605469-11.539063-28.03125-7.441406-7.421875-17.550781-11.5546875-28.0625-11.46875h-88.796875c-10.511719-.0859375-20.621094
               4.046875-28.0625 11.46875-7.441406 7.425781-11.597656 17.519531-11.539062 28.03125v12.5h-51.199219c-19.1875.003906-35.394531
               14.234375-37.878907 33.257812-2.480468 19.027344 9.535157 36.941407 28.078126 41.863282zm239.601562
               279.878906h-189.203125c-17.097656 0-30.398437-14.6875-30.398437-33.5v-245.5h250v245.5c0 18.8125-13.300782
               33.5-30.398438 33.5zm-158.601562-367.5c-.066407-5.207031 1.980468-10.21875 5.675781-13.894531 3.691406-3.675781
               8.714843-5.695313 13.925781-5.605469h88.796875c5.210937-.089844 10.234375 1.929688 13.925781 5.605469 3.695313
               3.671875 5.742188 8.6875 5.675782 13.894531v12.5h-128zm-71.199219 32.5h270.398437c9.941406 0 18 8.058594 18
               18s-8.058594 18-18 18h-270.398437c-9.941407 0-18-8.058594-18-18s8.058593-18 18-18zm0 0"}]
   [:path {:d "m173.398438 154.703125c-5.523438 0-10 4.476563-10 10v189c0 5.519531 4.476562 10 10 10
               5.523437 0 10-4.480469 10-10v-189c0-5.523437-4.476563-10-10-10zm0 0"}]])
