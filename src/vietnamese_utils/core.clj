(ns vietnamese-utils.core
  (:require [clojure.string :as str])
  (:import [java.text Normalizer]))

(def ^:private NFC  java.text.Normalizer$Form/NFC)
(def ^:private NFD  java.text.Normalizer$Form/NFD)
(def ^:private NFKC java.text.Normalizer$Form/NFKC)
(def ^:private NFKD java.text.Normalizer$Form/NFKD)

(def ^:private tones "([\u0300\u0309\u0303\u0301\u0323])")

(def ^:private pattern_classic
  (re-pattern (str "(?i)(?<!q)([ou])([aeoy])" tones "(?!\\w)")))

(def ^:private pattern_1+3+4
  (re-pattern (str "(?i)" tones "([aeiouy\u0306\u0302\u031B]+)")))
(def ^:private pattern_2 ;; or "\\B" if Java <= 1.5
  (re-pattern (str "(?i)(?<=[\u0306\u0302\u031B])(.)" tones "\\b")))
(def ^:private pattern_5
  (re-pattern (str "(?i)(?<=[ae])([iouy])" tones)))
(def ^:private pattern_other-1
  (re-pattern (str "(?i)(?<=[oy])([iuy])" tones)))
(def ^:private pattern_other-2
  (re-pattern (str "(?i)(?<!q)(u)([aeiou])" tones)))
(def ^:private pattern_other-3
  (re-pattern (str "(?i)(?<!g)(i)([aeiouy])" tones)))

(defn normalize-diacritics
  ([s] (normalize-diacritics s true))
  ([s classic?]
     (let [convert-to-classic-on-demand
           (if classic?
             #(str/replace % pattern_classic "$1$3$2")
             identity)]
       (-> s
           (Normalizer/normalize NFD)
           (str/replace pattern_1+3+4   "$2$1")
           (str/replace pattern_2       "$2$1")
           (str/replace pattern_5       "$2$1")
           (str/replace pattern_other-1 "$2$1")
           (str/replace pattern_other-2 "$1$3$2")
           (str/replace pattern_other-3 "$1$3$2")
           convert-to-classic-on-demand
           (Normalizer/normalize NFC)))))

;; Works around an obscure Normalization bug which
;; erroneously converts D with stroke and d with stroke to D and d,
;; respectively, on certain Windows systems,
;; by substituting them with \00DO and \00F0, respectively,
;; prior to normalization and then reverting them in post-processing.

(defn normalize-diacritics*
  ([s] (normalize-diacritics* s true))
  ([s classic?]
     (-> s
         (str/replace \u0110 \u00D0)
         (str/replace \u0111 \u00F0)
         (normalize-diacritics classic?)
         (str/replace \u00D0 \u0110)
         (str/replace \u00F0 \u0111))))
