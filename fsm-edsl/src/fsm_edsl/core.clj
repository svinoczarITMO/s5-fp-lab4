(ns fsm-edsl.core
  (:require [clojure.string :as str]))

(defrecord State [name])
(defrecord Transition [from label to])
(defrecord Ignore [from to])

(def states (atom []))
(def transitions (atom []))
(def ignores (atom []))

(defmacro state [name]
  `(do
     (swap! states conj (->State ~name))))

(defmacro transition [from label to]
  `(do
     (swap! transitions conj (->Transition ~from ~label ~to))))

(defmacro ignore [from to]
  `(do
     (swap! ignores conj (->Ignore ~from ~to))))

(defmacro fsm-model [& forms]
  `(do ~@forms))

(defn generate-dot []
  (let [state-nodes (map #(if (= (:name %) "Idle")
                            (str "  " (:name %) " [shape=doublecircle];") 
                            (str "  " (:name %) " [shape=circle];")) @states)
        transition-edges (map #(str "  " (:from %) " -> " (:to %) " [label=\"" (:label %) "\"];") @transitions)
        ignore-edges (map #(str "  " (:from %) " -> " (:to %) " [style=dotted];") @ignores)]
    (str "digraph FSM {\n"
         (str/join "\n" state-nodes) "\n"
         (str/join "\n" transition-edges) "\n"
         (str/join "\n" ignore-edges) "\n"
         "}")))



(defn lift-model []
  (fsm-model
   (state "Idle")
   (state "MovingUp")
   (state "MovingDown")
   (state "PickingUp")
   (transition "Idle" "GoUp" "MovingUp")
   (transition "Idle" "GoDown" "MovingDown")
   (transition "MovingUp" "Stop" "Idle")
   (transition "MovingDown" "Stop" "Idle")
   (transition "MovingDown" "PickUp" "PickingUp")
   (transition "PickingUp" "Done" "MovingDown")
   (ignore "Idle" "PickUp")))

(defn -main [] 
  (lift-model) 
  (let [dot-graph (generate-dot)]
    (spit "result.dot" dot-graph)
    (println dot-graph)))
