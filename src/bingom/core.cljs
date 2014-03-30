(ns bingom.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(enable-console-print!)

(def app-state (atom {:rows [[[8] [19] [39] [55] [73]]
                             [[2] [17] [31] [51] [62]]
                             [[15] [23] ["FREE", true] [47] [64]]
                             [[9] [18] [44] [60] [70]]
                             [[13] [21] [37] [53] [72]]]}))

(defn bingo-cell [[number checked] owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [toggle]}]
      (dom/td #js {:className (when checked "checked")
                   :onClick (fn [e] (put! toggle number))}
              number))))

(defn bingo-card [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:toggle (chan)
       :bingo? (chan)})
    om/IWillMount
    (will-mount [_]
      (let [toggle (om/get-state owner :toggle)
            bingo? (om/get-state owner :bingo?)]
        (go (loop []
              (let [number (<! toggle)]
                (om/transact! app :rows
                              (fn [rows]
                                (map (fn [row]
                                       (map (fn [[n checked]]
                                              (cond
                                                (= n "FREE") (do (put! bingo? true) [n true])
                                                (= n number) [n (not checked)]
                                                :else        [n checked]))
                                            row))
                                     rows)))
                (recur))))
        (go (loop []
              (let [_ (<! bingo?)]
                (om/transact! app :rows
                              (fn [rows]
                                (if (some (fn [row]
                                            (every? (fn [[_ checked]] checked)
                                                    row))
                                          rows)
                                  (js/alert "Bingo!"))
                                (let [columns (apply mapv vector rows)]
                                  (if (some (fn [column]
                                              (every? (fn [[_ checked]] checked)
                                                      column))
                                            columns)
                                    (js/alert "Bingo!")))
                                (let [diagonal (for [x (range (count (first rows)))]
                                                 (nth (nth rows x) x))]
                                  (if (every? (fn [[_ checked]] checked) diagonal)
                                    (js/alert "Bingo!")))
                                (let [diagonal (for [x (range (count (first rows)))]
                                                 (nth (reverse (nth rows x)) x))]
                                  (if (every? (fn [[_ checked]] checked) diagonal)
                                    (js/alert "Bingo!")))
                                rows)))
              (recur)))))
    om/IRenderState
    (render-state [this {:keys [toggle]}]
      (dom/table nil
                 (dom/thead nil
                            (apply dom/tr nil
                                   (map #(dom/th nil %) "BINGO")))
                 (apply dom/tbody nil
                        (map (fn [numbers]
                               (apply dom/tr nil
                                      (om/build-all bingo-cell numbers
                                                    {:init-state {:toggle toggle}})))
                             (:rows app)))))))

(om/root bingo-card app-state
         {:target (. js/document (getElementById "bingo"))})
