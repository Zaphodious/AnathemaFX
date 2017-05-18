(ns anathemafx.image-collection
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [draconic.fx :as fx]
            [draconic.fx.assignment :as fxa]
            [draconic.fx.ml :as fxml]
            [draconic.fx.ml :as dfxml]

            [draconic.fx.cells :as fxc]
            [anathemafx.window-utils :as awu])
  (:import (javafx.stage FileChooser DirectoryChooser)
           (javafx.embed.swing SwingFXUtils)
           (javax.imageio ImageIO)
           (java.io File)
           (javafx.scene.image Image ImageView)
           (org.controlsfx.control GridView GridCell)
           (org.controlsfx.control.cell ImageGridCell)
           (javafx.util Callback)
           (javafx.application Platform)
           (javafx.scene.control ListView ListCell Button)
           (javafx.geometry Pos)))

(def ^:private image-collection-path "content/img/")
(def ^:private image-ext "png")
(defn- make-file-string-from [thingy]
  (if (string? thingy)
    (str "file:" image-collection-path thingy "." image-ext)
    (str "file:" (.getPath thingy))))
;(def ^:private collection-map-file-name (str image-collection-path "collection.edn"))
;(def ^:private collection-map-atom (atom
;                                     (if (.exists (io/file collection-map-file-name))
;                                       (read-string (slurp collection-map-file-name))
;                                       (do
;                                         (spit collection-map-file-name #{})
;                                         #{}))))
(defn get-collection-names []
  (let [things-in-dir (file-seq (io/file image-collection-path))
        just-png (filter #(str/ends-with? (.getName %) ".png") things-in-dir)
        just-names (map #(.getName %) just-png)
        names-without-png (map #(str/replace % ".png" "") just-names)]
    (into [] names-without-png)))
(defn get-file []
  (let [chooser (new FileChooser)]
    (fx/run-now (.showOpenDialog chooser nil))))
(defn get-directory []
  (let [chooser (new DirectoryChooser)]
    (fx/run-now (.showDialog chooser nil))))

(defn- save-image [the-name the-image]
  (let [image-ext "png"
        the-path (str image-collection-path the-name "." image-ext)]
    (io/make-parents the-path)
    (let [the-file-object (new File the-path)
          the-buffered-image (SwingFXUtils/fromFXImage the-image nil)]
      (ImageIO/write the-buffered-image image-ext the-file-object))))


(def image-load-default-settings
  {:w                  512
   :h                  512
   :preserve-ratio     true
   :smooth             true
   :background-loading false})
(def image-load-preview-settings
  (into image-load-default-settings {:h 200 :w 200 :background-loading true}))

(defn get-image-from-collection
  ([the-name] (get-image-from-collection the-name image-load-default-settings))
  ([the-name the-settings]
   (do (println "name: " the-name " settings: " the-settings)
       (new Image (make-file-string-from the-name)
            (:w the-settings)
            (:h the-settings)
            (:preserve-ratio the-settings)
            (:smooth the-settings)
            (:background-loading the-settings)))))

(defn add-to-collection
  ([] (add-to-collection (get-file)))
  ([the-file] (add-to-collection the-file (str (.hashCode the-file))))
  ([the-file the-name] (add-to-collection the-file the-name image-load-default-settings))
  ([the-file the-name the-settings]
   (let [the-image (get-image-from-collection the-file the-settings)]

     (save-image the-name the-image)
     ;(swap! collection-map-atom #(conj % the-name))
     ;(spit collection-map-file-name @collection-map-atom)
     ))
  )

(defn add-folder-to-collection
  ([] (add-folder-to-collection (get-directory)))
  ([the-directory]
   (let [filetree (file-seq the-directory)]
     (run! (fn [the-file]
             (try (add-to-collection the-file)
                  (catch Exception e
                    (println "didnt' work for " the-file))))
           filetree))))
;;Image(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth, boolean backgroundLoading)



(defn make-preview-map
  ([] (make-preview-map image-load-preview-settings))
  ([the-settings]
   (into {}
         (map (fn [the-name] {the-name (get-image-from-collection the-name the-settings)}) (get-collection-names)))))



(fxml/defcontroller-selector pic-selection-controller "Manages the images"
  [mappo imageChooserScrollpane displayImageView appButtonCancel stage delivery-atom]
  (do (println mappo)
      (let [the-view (new GridView)
            [preconfigFlowPane flow-pane-map] (dfxml/make-node-with-id-map "layouts/preconfigFlowPane.fxml")
            cell-height 100.0
            cell-width 400.0
            preview-map (make-preview-map (into image-load-preview-settings {:h cell-height :w cell-width}))]
        (.setOnAction appButtonCancel
                      (fx/event-handler [evento]
                        (do (swap! delivery-atom (fn [cs] nil))
                            (fx/run-later (.close stage)))))
        ;(.setCellHeight the-view cell-height)
        ;(.setCellWidth the-view cell-width)
        ;(.setHorizontalCellSpacing the-view 7.0)
        ;(.setVerticalCellSpacing the-view 7.0)
        (println "this is the result->" (fx/run-now (do (println "this runs on the fx thread") 42)))
        (println "gonna add the the-view to the parent")
        (fxa/set-content-for-content-havers imageChooserScrollpane preconfigFlowPane)
        (println "gonna set the cell factory")
        ;(fxc/set-cell-factory the-view
        ;                      (fxc/gen-cell-constructor
        ;                        GridCell
        ;                        :properties `{:setAlignment Pos/CENTER}
        ;                        :text-fn (fn [[a c] b] nil)
        ;                        :graphic-fn
        ;                        (fn [[a c] b]
        ;                          (let [imview (new ImageView)
        ;                                btn (new Button "" imview)]
        ;                            (println "did a thing for " a)
        ;                            (.setOnAction (fx/event-handler [evento]
        ;                                           (do
        ;                                             (.setImage displayImageView (get-image-from-collection a))
        ;                                             (swap! delivery-atom (fn [ex] a)))))
        ;                            (.setImage imview c)
        ;                            btn)))
        ;                      )
        (println "gonna add the images")
        (run! (fn [[a c]]
                (fxa/set-content-for-children-havers preconfigFlowPane
                                                     [(let [imview (new ImageView)
                                                            btn (new Button "" imview)]
                                                        (println "did a thing for " a)
                                                        (-> btn (.getStyleClass) (.add "img-selector-button"))
                                                        (.setOnAction btn (fx/event-handler [evento]
                                                                            (do
                                                                              (.setImage displayImageView (get-image-from-collection a))
                                                                              (swap! delivery-atom (fn [ex] a)))))
                                                        (.setImage imview c)
                                                        btn)]))
              preview-map)

        ;(run! (fn [img] (.add (.getItems the-view) img))
        ;      preview-map)
        (println "done!")
        ;(run! (fn [imagy]
        ;        (let [[imvw im-map] (dfxml/make-node-with-id-map "layouts/preconfigImageViewSelector.fxml")]
        ;          (println "setting image")
        ;          (.setImage imvw imagy)
        ;          (println "children-having!")
        ;          (fxa/set-content-for-children-havers preconfigFlowPane [imvw]))
        ;        (println "that's all done"))
        ;      (make-preview-map))
        )))

(defn show-selection-chooser []
  (do
    (println "this is the result first->" (fx/run-now (do (println "running on FX thread!") 777)))
    (dfxml/launch-fxml-window ["layouts/picChooser.fxml"]
                              ;#'awu/setup-custom-window-behavior
                              (awu/using-controllers
                                #'awu/setup-custom-window-behavior
                                #'pic-selection-controller
                                )
                              (awu/make-custom-dec-window 1200 500)
                              (promise)
                              ;["basisContentPane"            ;(make-preview-pane :large --sample-display-data)
                              ;
                              ; (make-preview-pane :large
                              ;                    --sample-display-data)]]
                              ))
  ;;to-do: make
  )

(defn whaa []
  (com.sun.javafx.application.PlatformImpl/tkExit)
  (Platform/exit))