(ns anathemafx.character
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
            [anathemafx.window-utils :as awu :refer [setup-custom-window-behavior make-custom-dec-window replace-tree-in-treeview]]
            [clojure.core.async :as ca :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
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

(defcontroller solar-controller
  ""
  [node-map basisContentPane solarCharSheetBase]
  ((ui/get-attribute basisContentPane :sub-app-registration-fn)
    [:character :solar] solarCharSheetBase))

(def solar-sheet-construction
  [["subAppIntroPoint" "layouts/charSheet.fxml"]
   ["charInfo" "layouts/character/baseinfo.fxml"]
   ["attributesSection" "layouts/character/attributes.fxml"]
   ["abilitySection" "layouts/character/abilities.fxml"]])

(defn launch-test-character-window
  ([] (launch-test-character-window nil))
  ([the-mode] (dfxml/launch-fxml-window ["layouts/basis.fxml"
                                         ["basisContentPane" "layouts/preconfigScrollPane.fxml"]
                                         ["preconfigScrollPane" "layouts/charSheet.fxml"]
                                         ["charInfo" "layouts/character/baseinfo.fxml"]
                                         ["attributesSection" "layouts/character/attributes.fxml"]
                                         ["abilitySection" "layouts/character/abilities.fxml"]
                                         ]
                                        ;["basisContentPane" ;(make-preview-pane :large --sample-display-data)
                                        ; (make-preview-pane :large
                                        ;                    --sample-display-data)]]
                                        #'setup-custom-window-behavior
                                        (make-custom-dec-window 1194 750)
                                        ;(if (= the-mode :prod)
                                        ;  (promise)
                                        ;  nil)
                                        )))