(ns anathemafx.manager
  (:require [draconic.fx :as dfx]
            [draconic.fx.ml :as dfxml :refer [launch-fxml-window defcontroller]]
            [draconic.fx.tree :refer :all]
            [draconic.fx :as fx]
            [draconic.macros :as dramac]
            [draconic.fx.assignment :as fxa]
            [draconic.fx.cells :as fxc]
            [com.rpl.specter :as sp :refer [walker transform select keypath ALL FIRST LAST]]
            [clojure.string :as str]
            [anathemafx.image-collection :as img]
            [anathemafx.chronicle :as achron]
            [anathemafx.window-utils :as awu :refer [setup-custom-window-behavior make-custom-dec-window replace-tree-in-treeview]]
            [clojure.core.async :as ca :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [anathemafx.launcher :as anl]
            [draconic.ui :as ui])
  (:import (javafx.stage Stage StageStyle)
           (javafx.scene Scene)
           (javafx.scene Node)
           (javafx.scene.image Image)
           (javafx.application Platform)
           (com.blakwurm.anathemafx ResizeHelper)
           (javafx.scene.layout FlowPane)
           (javafx.scene.control Label)
           (javafx.scene.image ImageView)))

(def --sample-display-data
  [{:pic "1053759595",
    :type :character,
    :subtype :solar,
    :name "Magnus the Magnificent",
    :subtitle "Solar Twilight",
    :short-description "Lore Supernal",
    :id 4200}
   {:pic "1077315991",
    :type :character,
    :subtype :solar,
    :name "The Lady Sapphira",
    :subtitle "Solar Eclipse",
    :short-description "Performance Supernal",
    :id 4201}
   {:pic "-818670546",
    :type :character,
    :subtype :mortal,
    :name "Captain Nij-fe",
    :subtitle "Mortal Warrior",
    :short-description "Combat Focus",
    :id 4202}
   {:pic "-603529578",
    :type :chron,
    :subtype :current,
    :name "A Knight's Problem",
    :subtitle "A Chron by A Mad Duck",
    :short-description "When a death knight is addicted to gambling, the world will shake with the weight of his debt.",
    :id 4203}
   {:pic "-508618295",
    :type :character,
    :subtype :solar,
    :name "Jade Dagger",
    :subtitle "Solar Night",
    :short-description "Stealth Supernal",
    :id 4204}
   {:pic "1438562483",
    :type :character,
    :subtype :solar,
    :name "Prince Alibaina",
    :subtitle "Solar Zenith",
    :short-description "Presence Supernal",
    :id 4205}
   {:pic "1390621955",
    :type :character,
    :subtype :solar,
    :name "Alex Rose",
    :subtitle "Solar Dawn",
    :short-description "Melee Supernal",
    :id 4206}]
  )

(def ^:private preview-data-change-chan (ca/chan))
(def ^:private current-preview-data (atom --sample-display-data))
(defn get-preview-nodes [] @current-preview-data)
(defn add-preview-node [{:keys [pic type subtype name subtitle short-description] :as new-thing}]
  (do
    (>!! preview-data-change-chan (into new-thing {:change :add}))
    (swap! current-preview-data (fn [oldvec] (conj oldvec new-thing)))))
(defn remove-preview-node
  "Clears the preview data of all nodes with the same name as the argument"
  [{:keys [pic type subtype name subtitle short-description] :as new-thing}]
  (do
    (>!! preview-data-change-chan (into new-thing {:change :remove}))
    (swap! current-preview-data (fn [oldvec] (filter #(not (= (:name %) name)) oldvec)))))
(def preview-data-change-pub (ca/pub preview-data-change-chan :change))

;(def preview-data-change-pubs {:type (ca/pub preview-data-change-chan :type)
;                               :change (ca/pub preview-data-change-chan :add)})
;(defn test-sub [] (ca/sub (:type preview-data-change-pubs) :solar (let [ch (chan)]
;                                                                    (go (println "thingy is " (<! ch)))
;                                                   ch)))


(defn make-preview [size {:keys [pic type subtype name subtitle short-description] :as to-display-map}]
  (let [[thing-preview-container
         {:strs [previewImage previewTitle previewSubTitle previewDescription]
          :as   preview-map}] (dfxml/make-node-with-id-map "layouts/thingPreviewLarge.fxml")
        the-widgets [previewImage previewTitle previewSubTitle previewDescription]
        id-prefix (str (str/replace name " " "") "-")
        imago (img/get-image-from-collection pic (into img/image-load-preview-settings {:w 500 :background-loading false}))]
    (run! (fn [thingy] (try
                         (fxa/set-id! thingy (str id-prefix (.getId thingy)))
                         (catch Exception e
                           (print "an exception! " e))))
          the-widgets)
    ;(img/block-until-image-is-loaded imago)
    (let [maxo-width (.getWidth imago)]
      (println "maxi width is " maxo-width)
      (run! (fn [thing] (try
                          (.setPrefWidth thing maxo-width)
                          (catch Exception e
                            (println "another exception! " e))))
            [previewTitle previewSubTitle previewDescription]))
    (.setImage previewImage imago)
    (.setText previewTitle name)
    (.setText previewSubTitle subtitle)
    (.setText previewDescription short-description)
    [thing-preview-container {}]))                          ;empty map so that it can be passed into a launch function for debugging.



(defn make-preview-pane [size preview-data-to-display]
  (println "preview data is " preview-data-to-display)
  (fn []
    (let [[preconfigFlowPane flow-pane-map] (dfxml/make-node-with-id-map "layouts/preconfigFlowPane.fxml")]
      (run! (fn [preview-thingy]
              (let [[preview-container _] (make-preview size preview-thingy)]
                (fxa/set-content-for-children-havers preconfigFlowPane [preview-container])))
            preview-data-to-display)
      [preconfigFlowPane flow-pane-map])))

(def navtree-model-base {:display     "Everything"
                         :select-type [:all :all]
                         :children    [{:display     "Characters"
                                        :select-type [:character :all]
                                        :children    [{:display     "Solars"
                                                       :select-type [:character :solar]
                                                       :children    []}
                                                      {:display     "Mortals"
                                                       :select-type [:character :mortal]
                                                       :children    []}]}
                                       {:display     "Chronicles"
                                        :select-type [:chron :all]
                                        :children    [{:display     "Templates"
                                                       :select-type [:chron :template]
                                                       :children    []}
                                                      {:display     "Now Playing"
                                                       :select-type [:chron :current]
                                                       :children    []}]}]})

(defn merge-display-data-with-nav-tree [navtree-model display-data]
  (loop [display-node (first display-data)
         other-display-nodes (rest display-data)
         current-navtree navtree-model]
    (if display-node
      (recur (first other-display-nodes)
             (rest other-display-nodes)
             (sp/transform [(sp/walker (fn [thingy]
                                         (= (:select-type thingy)
                                            [(:type display-node) (:subtype display-node)])))
                            (sp/keypath :children)]
                           (fn [thingy]
                             (println thingy)
                             (into thingy [{:display     (:name display-node)
                                            :select-type display-node}]))
                           current-navtree))
      current-navtree)))

(defn set-display-section! [scroller display-data]
  (fxa/try-set! scroller :children
                ((make-preview-pane :large
                                    display-data))))

(defcontroller manager-controller
  "Responsible for "
  [nodemap windowBasis appButtonExit basisContentPane appToolBar preconfigFlowPane basisnavtree chronEditor]
  (let [add-chan (ca/sub preview-data-change-pub :add (chan))
        remove-chan (ca/sub preview-data-change-pub :remove (chan))
        nav-tree-select-chan (chan)]

    (println "passed chan is " nav-tree-select-chan)
    (set-display-section! basisContentPane (filter (fn [thingy] (= :character (:type thingy))) (get-preview-nodes)))
    (ca/go-loop []
      (let [selection (<! nav-tree-select-chan)]
        (println "\nselection is " selection "\n")
        (cond (vector? (:select-type (:new selection)))
              (let [[first-selector second-selector] (:select-type (:new selection))]
                (set-display-section! basisContentPane
                                      (filter (fn [thingy]
                                                (if (= second-selector :all)
                                                  (= first-selector (:type thingy))
                                                  (and (= first-selector (:type thingy)) (= second-selector (:subtype thingy))))) --sample-display-data)
                                      ))
              (map? (:select-type (:new selection)))
              (let [the-thing (-> selection :new :select-type)
                    typevec [(:type the-thing) (:subtype the-thing)]]
                (println "##--##>> " the-thing " selected, should be putting in " (get (ui/get-attribute basisContentPane :sub-app-register) typevec))
                (ui/set-state! basisContentPane (get (ui/get-attribute basisContentPane :sub-app-register) typevec)))
              ))
      (recur))

    (replace-tree-in-treeview basisnavtree (merge-display-data-with-nav-tree navtree-model-base --sample-display-data) nav-tree-select-chan)
    (setup-custom-window-behavior nodemap)
    (println "manager-controller works!")))


(dramac/defmethod-using-map
  draconic.fx.assignment/set!-typed [the-node op-id new-state]
  {[:children "javafx.scene.layout.Pane"] (do (println "it does the thing what it do") (fxa/set-content-for-children-havers the-node new-state))}
  )





(defn launch-manager-window
  ([] (launch-manager-window nil))
  ([the-mode] (anl/launch-anathema [] [#'manager-controller] [1194 461] the-mode)
    )
  )
;; Image(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth, boolean backgroundLoading)