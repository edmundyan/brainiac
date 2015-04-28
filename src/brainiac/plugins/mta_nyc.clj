(ns brainiac.plugins.mta-nyc
  (:require [brainiac.plugin :as brainiac]
            [brainiac.xml-utils :as xml]
            [clojure.contrib.zip-filter.xml :as zf]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as enlive])
  (:import [java.io StringReader]
           [org.jsoup Jsoup])
  (:use [clojure.pprint]
        [clojure.string :only (join split)]))

(defn advisory-url [api-key]
  (format "http://api.bart.gov/api/bsa.aspx?cmd=bsa&key=%s&date=today" api-key))

(defn schedule-url [station-id api-key]
  (format "http://api.bart.gov/api/etd.aspx?cmd=etd&orig=%s&key=%s" station-id api-key))

(defn minutes-display [minutes]
  (if (= minutes "Leaving")
    "now"
    (str minutes "m")))

(defn parse-text [text]
  ; Only show the first line
  ; (.text (Jsoup/parse text)))
  (pprint (str "parsing a line of text?"))
  (pprint text)
  (pprint (str "~~~~~~~DONE!~~~~~~~~~"))
  (flush)


  (join "* "
    (enlive/texts
      (enlive/select
        (enlive/html-snippet text)
        [:a.plannedWorkDetailLink])
      )
    )
  )

(defn parse-status [status]
  (case status
    "GOOD SERVICE" "green"
    "PLANNED WORK" "yellow"
    "DELAYS" "red"
    status))

(defn parse-line [node]
  (let [line-name (zf/xml1-> node :name zf/text)
        status (zf/xml1-> node :status zf/text)
        text (zf/xml1-> node :text zf/text) ]
    (assoc {}
           :line-name line-name
           :status (parse-status status)
           :text (parse-text text))))

(defn transform [stream]
  (let [xml-zipper (xml/parse-xml stream)]
    (assoc {}
           :name "mta-nyc"
           :data (take 9 (zf/xml-> xml-zipper :subway :line parse-line)))))

(defn configure [{:keys [program-name]}]
  (brainiac/simple-http-plugin
    {:method "GET"
     :url "http://web.mta.info/status/serviceStatus.txt"}
    transform program-name))
