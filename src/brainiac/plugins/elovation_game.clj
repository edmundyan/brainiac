(ns brainiac.plugins.elovation-game
  (:use [clojure.contrib.json :only (read-json)]
        [clojure.java.io :only (reader)])
  (:require [brainiac.plugin :as brainiac]
            [brainiac.pages.templates :as templates]))

(defn format-rating [rating]
  (format "%s - %s" (:value rating) (:player rating)))

(defn format-result [result]
  (format "%s beat %s" (:winner result) (:loser result)))

(defn transform [stream]
  (let [json (read-json (reader stream))
        game-name (:name json)
        ratings (:ratings json)
        results (:results json)]
  (assoc {}
    :name "elovation-game"
    :type "elovation-game"
    :title game-name
    :rank-data (map format-rating (take 5 ratings))
    :result-data (map format-result (take 5 results)))))

(defn game-url [base-url game-id]
  (format "%s/games/%s.json" base-url game-id))

(defn html []
  [:script#elovation-game-template {:type "text/mustache"}
   "<h3>{{title}}</h3> <h5>Top-ranked Players</h5> <ul> {{#rank-data}}<li>{{.}}</li>{{/rank-data}} </ul> <h5>Recent Results</h5> <ul> {{#result-data}}<li>{{.}}</li>{{/result-data}} </ul>"])

(defn configure [{:keys [url game-id username password program-name]}]
  (brainiac/schedule
    15000
    (brainiac/simple-http-plugin
      {:url (game-url url game-id) :basic-auth [username password]}
      transform program-name)))
