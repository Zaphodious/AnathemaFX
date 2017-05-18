(ns anathemafx.window-utils
  (:require [draconic.fx :as fx]
            [draconic.fx.ml :as dfxml :refer [launch-fxml-window defcontroller]]
            [draconic.fx.tree :refer :all]
            [draconic.fx :as dfx]
            [draconic.fx.assignment :as fxa]
            [clojure.core.async :as ca :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]])
  (:import (com.blakwurm.anathemafx ResizeHelper)
           (javafx.scene Scene)
           (javafx.stage Stage StageStyle)
           (javafx.scene.layout AnchorPane Pane HBox)))

(defn make-nav-tree-from-model [tree-model]
  (make-simple-tree #(contains? % :children)
                    :children
                    tree-model))
;
;(def navtree (let [tree-thing (make-simple-tree
;                                #(contains? % :children)
;                                :children
;                                '{:display     "Everything"
;                                  :select-type [:all :all]
;                                  :children    [{:display     "Characters"
;                                                 :select-type [:character :all]
;                                                 :children    [{:display     "Solars"
;                                                                :select-type [:character :solar]}
;                                                               {:display     "Mortals"
;                                                                :select-type [:character :mortal]}]}
;                                                {:display     "Chrons"
;                                                 :select-type [:chron :all]
;                                                 :children    [{:display     "Templates"
;                                                                :select-type [:chron :template]}
;                                                               {:display     "Now Playing"
;                                                                :select-type [:chron :current]}]}]})]
;               (println tree-thing)
;               tree-thing))

(defn replace-tree-in-treeview [navtreenode treemodel onclick-chan]
  (let [real-tree-model (if (map? treemodel)
                          (make-nav-tree-from-model treemodel)
                          treemodel)]
    (println "recieved chan is " onclick-chan)
    (set-cell-factory navtreenode (fn [thingy] (:display thingy)))
    (set-selection-listener navtreenode
                            (fn [oldval newval]
                              (go (>! onclick-chan {:old oldval :new newval}))))
    (fx/run-later (.setRoot navtreenode real-tree-model))
    (fx/run-later (.setShowRoot navtreenode false)))
  )
;
;(defcontroller nav-tree-controller
;  "Sets up the tree view"
;  [mappa basisnavtree]
;  (println basisnavtree)
;  (println "Hello")
;  (fx/run-later (.setRoot basisnavtree navtree))
;  (fx/run-later (.setShowRoot basisnavtree false))

  ;(set-cell-factory basisnavtree (fn [thingy] (:display thingy)))
  ;(set-selection-listener basisnavtree
  ;                        (fn [oldval newval]
  ;                          (println "things are " oldval " and then " newval)))
  ;)
;primaryStage.setX(event.getScreenX() - xOffset);
;primaryStage.setY(event.getScreenY() - yOffset);
(defn ^:dynamic *close-this*
  [the-stage]
  (fx/event-handler [evento] (fx/run-later (.close the-stage))))

(defcontroller setup-custom-window-behavior
  ""
  [nodemap stage appToolBar windowBasis appButtonExit]
  (ResizeHelper/addResizeListener stage)

  (.setOnAction appButtonExit (*close-this* stage))

  (let [offsets (atom {:x 0 :y 0})]
    (.setOnMousePressed appToolBar
                        (fx/event-handler [mouseEvent]
                          (do
                            (swap! offsets assoc :y (.getSceneY mouseEvent))
                            (swap! offsets assoc :x (.getSceneX mouseEvent))
                            (println @offsets))))
    (.setOnMouseDragged appToolBar
                        (fx/event-handler [mouseEvent]
                          (do
                            (.setX stage (- (.getScreenX mouseEvent) (:x @offsets)))
                            (.setY stage (- (.getScreenY mouseEvent) (:y @offsets))))))))

(defn make-custom-dec-window [width height] (fn [the-node]
                                              (dfx/run-now
                                                (let [stage (new Stage)
                                                      scene (new Scene the-node)]
                                                  (-> the-node
                                                      (.getStylesheets)
                                                      (.add "styles/basis.css"))
                                                  (.setWidth stage width)
                                                  (.setHeight stage height)
                                                  (.initStyle stage StageStyle/TRANSPARENT)
                                                  (.setFill scene nil)
                                                  (.setScene stage scene)
                                                  stage))))

(defn chain-fn [vec-of-functions]
  (let [seq-of-functions (seq vec-of-functions)
        the-arg (gensym "arg")
        threaded-form (-> seq-of-functions
                          (conj the-arg)
                          (conj '->))
        basis-fn `(fn [~the-arg] ~threaded-form)]
    (eval basis-fn)
    ))
(defn using-controllers [& controllers]
  (chain-fn controllers)
  )