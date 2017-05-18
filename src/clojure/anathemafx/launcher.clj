(ns anathemafx.launcher
  (:require [anathemafx.window-utils :as awu]
            [draconic.fx.ml :as dfxml]
            [draconic.ui :as ui]))

(defn setup-screen-register
  "Sets up the sub-app type registry."
  [{:strs [basisContentPane] :as mappo}]
  (ui/set-attribute! basisContentPane :sub-app-register {})
  (ui/set-attribute! basisContentPane :sub-app-registration-fn
                     (fn [[type subtype :as typevec] recieving-node]
                       (let [current-sub-apps (ui/get-attribute basisContentPane :sub-app-register)]
                         (println "----> " type " with " subtype " registered with " recieving-node)
                         (ui/set-attribute! basisContentPane :sub-app-register
                                            (into current-sub-apps {[type subtype] recieving-node})))))
  mappo)

(defn launch-anathema
  [fxml-addresses controllers [width height :as dims] prod?]
  (let [mega-controller (-> (into [#'setup-screen-register] controllers)
                            (conj #'awu/setup-custom-window-behavior)
                            awu/chain-fn)
        address-block (-> ["layouts/basis.fxml"]
                          (into fxml-addresses))]
    (dfxml/launch-fxml-window
      address-block
      mega-controller
      (awu/make-custom-dec-window width height)
      (if prod?                                             ;If this is intended to be launched from a standalone jar (and not from a repl), the promise will prevent the window from closing.
        (promise)
        nil)
      )))
