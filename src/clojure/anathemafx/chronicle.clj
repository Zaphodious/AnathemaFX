(ns anathemafx.chronicle
  (:require [draconic.fx :as dfx]
            [draconic.ui.fx :as uifx]
            [lytek.draconic.chron :as ldc]
            [lytek.col :as lycol]
            [draconic.fx.ml :as dfxml :refer [launch-fxml-window defcontroller]]
            [draconic.fx.tree :refer :all]
            [draconic.fx :as fx]
            [draconic.macros :as dramac]
            [draconic.fx.assignment :as fxa]
            [draconic.fx.cells :as fxc]
            [com.rpl.specter :as sp :refer [walker transform select keypath ALL FIRST LAST]]
            [clojure.string :as str]
            [anathemafx.image-collection :as img]
            [anathemafx.launcher :as anl]
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
           (javafx.scene.control Label TabPane Tab)
           (javafx.scene.image ImageView)))

(defcontroller chron-editor-controller
  ""
  [nodemap elementList ^TabPane editorTabs chronEditor basisContentPane]
  ((ui/get-attribute basisContentPane :sub-app-registration-fn)
    [:chron :current] chronEditor)
  (let [{:strs [infoTab meritTab charmTab] :as tabbies}
        (into {} (map (fn [^Tab a] [(.getId a) a]) (.getTabs editorTabs)))]

    )
  )
(def chron-editor-setup
  [["subAppIntroPoint" "layouts/chron/chronEditor.fxml"]
   ["meritArea" "layouts/chron/meritEditor.fxml"]])

(defn launch-standalone-chron-window
  ([] (launch-standalone-chron-window nil))
  ([the-mode]

   (comment (dfxml/launch-fxml-window
      ["layouts/basis.fxml"
       ["basisContentPane" "layouts/chron/chronEditor.fxml"]
       ["meritArea" "layouts/chron/meritEditor.fxml"]
       ]
      ;["basisContentPane" ;(make-preview-pane :large --sample-display-data)
      ; (make-preview-pane :large
      ;                    --sample-display-data)]]
      #'setup-custom-window-behavior
      (make-custom-dec-window 1194 750)
      ;(if (= the-mode :prod)
      ;  (promise)
      ;  nil)
      ))
    (anl/launch-anathema [["basisContentPane" "layouts/chron/chronEditor.fxml"]
                          ["meritArea" "layouts/chron/meritEditor.fxml"]]
                         [#'chron-editor-controller]
                         [1194 750]
                         false)))

(comment

  (do
    (def testmerit
      {:name           "The Testing Merit"
       :description    "This merit is intended only for testing purposes."
       :possible-ranks #{3 5}
       :prereq         [:or [:strength 3] [:performance 4]
                        ]
       :static-tags    [[] [] [:sorcerer] []
                        [:martial-artist]]})
    (def testmeritchron
      {:merits
       (lycol/namemap
         [{:name           "single rank"
           :possible-ranks #{2}}
          {:name           "multiple ranks"
           :possible-ranks #{1 2 4}}
          {:name           "prereq athletics 3"
           :possible-ranks #{1 2 3 4 5}
           :prereq         [:or ["Athletics" 3]]}
          {:name           "multiple or prereqs"
           :possible-ranks #{1 2 3 4 5}
           :prereq         [:or ["Melee" 3] ["brawl" 3]]}
          {:name           "multiple and prereqs"
           :possible-ranks #{1 2 3 4 5}
           :prereq         [:and ["Melee" 3] [:brawl 3]]}
          {:name           "repurchasable"
           :possible-ranks #{1 2 3 4 5}
           :repurchasable  true}
          {:name           "adds sample tag"
           :possible-ranks #{1 2 3 4 5}
           :repurchasable  true
           :static-tags    [[:blorp]                            ;; For a given rank, adds the tags at index rank-1
                            [:blorp]
                            [:blorp]
                            [:blorp]
                            [:blorp]]}
          {:name           "adds tag from rank"
           :possible-ranks #{1 2 3 4 5}
           :repurchasable  true
           :static-tags    [[:primero]                          ;; For a given rank, adds the tags at index rank-1
                            [:segundo]
                            [:terciro]
                            [:fourth-y]
                            [:full-circle]]}
          {:name           "adds martial arts tag"
           :possible-ranks #{5}
           :static-tags    [[] [] [] []
                            [:martial-artist]]}
          {:name           "artifact tag giver"
           :possible-ranks #{2 3 4 5}
           :static-tags    [[]
                            [:artifact-2]
                            [:artifact-3]
                            [:artifact-4]
                            [:artifact-5]]}
          {:name           "hearthstone tag giver"
           :possible-ranks #{2 4}
           :static-tags    [[] [:hearthstone-2] [] [:hearthstone-4] []]}
          ])})
    (def chron-editor-window (launch-standalone-chron-window))
    (str "elementList")
    (ldc/put-merit-into-editor! testmerit chron-editor-window)
    (ui/apply-render-fn! (get chron-editor-window "elementList")
                         (fn [[blip bloop]]
                           (str/capitalize blip)))
    (ui/set-options-list! (get chron-editor-window "elementList")
                          (:merits testmeritchron))


    )

  )