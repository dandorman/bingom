(ns bingom.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def free ["FREE", true])
(def card (atom [[[ 8] [19] [39] [55] [73]]
                 [[ 2] [17] [31] [51] [62]]
                 [[15] [23] free [47] [64]]
                 [[ 9] [18] [44] [60] [70]]
                 [[13] [21] [37] [53] [72]]]))

(defn bingo? [numbers]
  (every? second numbers))

(defn some-bingo? [rows]
  (let [columns (apply mapv vector rows)
        tl->br (for [x (range (count (first rows)))]
                 (nth (nth rows x) x))
        rt->bl (for [x (range (count (first rows)))]
                 (nth (reverse (nth rows x)) x))
        chances (concat rows columns [tl->br rt->bl])]
    (some #(bingo? %) chances)))

(add-watch card :check-bingo
           (fn [_ _ _ new-card]
             (when (some-bingo? new-card)
               (.setTimeout js/window #(js/alert "BINGO!") 100))))

(defmulti bingo-cell (fn [[content _] _] (= "FREE" content)))

(defmethod bingo-cell true
  [& _]
  (reify
    om/IRender
    (render [_]
      (dom/td #js {:className "checked"} "FREE"))))

(defmethod bingo-cell false
  [[number checked :as cell] owner]
  (reify
    om/IRender
    (render [_]
      (dom/td #js {:className (when checked "checked")
                   :onClick (fn [_] (om/transact! cell 1 #(not checked)))}
              number))))

(defn bingo-card [card owner]
  (reify
    om/IRender
    (render [_]
      (dom/table nil
                 (dom/thead nil
                            (apply dom/tr nil
                                   (map #(dom/th nil %) "BINGO")))
                 (apply dom/tbody nil
                        (map (fn [numbers]
                               (apply dom/tr nil
                                      (om/build-all bingo-cell numbers)))
                             card))))))

(om/root bingo-card card
         {:target (.getElementById js/document "bingo")})
