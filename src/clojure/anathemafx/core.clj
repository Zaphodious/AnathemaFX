(ns anathemafx.core
  (:gen-class)
  (:require [anathemafx.launcher :as anl]
            [anathemafx.manager :as anm :refer [manager-controller]]
            [anathemafx.character :as anchar :refer [solar-controller]]
            [anathemafx.chronicle :as anc :refer [chron-editor-controller]]))

(defn full-anathema-run [[height width :as dims] mode]
  (anl/launch-anathema (-> []
                           (into anc/chron-editor-setup)
                           (into anchar/solar-sheet-construction))
                       [#'chron-editor-controller
                        #'solar-controller
                        #'manager-controller]
                       dims
                       mode))

(defn -main [& args]
  (anl/launch-anathema [["basisContentPane" "layouts/chron/chronEditor.fxml"]
                        ["meritArea" "layouts/chron/meritEditor.fxml"]]
                       [#'chron-editor-controller
                        #'manager-controller]
                       [1194 750]
                       :this-is-production-mode!))

