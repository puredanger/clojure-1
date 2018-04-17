(ns simplefrontend.productgroups
  (:require
    [reagent.core :as r]
    [ajax.core :as a-core]
    [simplefrontend.components :as sf-components]))



;; ***** Namespace vars. *****
(def my-error-msg-atom (r/atom nil))
(def my-success-msg-atom (r/atom nil))
;; For debugging
(def my-response-atom (r/atom nil))
(def my-product-groups-atom (r/atom nil))
(def my-dev-product-groups-atom (r/atom {"1" "Books", "2" "Movies"}))


(defn reset-page
  "Reset page atoms when coming here from home page."
  []
  (reset! my-response-atom nil)
  (reset! my-error-msg-atom nil)
  (reset! my-success-msg-atom nil))



(defn -handler
  "The success (http status 200) handler."
  [response]
  (.log js/console (str "ENTER -handler, response: " response))
  (let []

    (do
      (reset! my-response-atom response)
      (reset! my-product-groups-atom (@my-response-atom "product-groups"))
      (reset! my-success-msg-atom "TODO")
      (reset! my-error-msg-atom nil))))


(defn -error-handler
  "The error (http status not 200) handler."
  [response]
  (.log js/console (str "ENTER -error-handler, response: " response))
  (let [error-msg ((:response response) "msg")]
    (do
      (reset! my-response-atom response)
      (reset! my-error-msg-atom error-msg)
      (reset! my-success-msg-atom nil))))




(defn -get-productgroups
  "Does the GET for productgroups"
  [token]
  (let [host (:host simplefrontend.core/backend-host-config)
        port (:port simplefrontend.core/backend-host-config)
        url (str "http://" host ":" port "/product-groups")
        ]

    (let [response (a-core/GET url
                               {:format          :json
                                :response-format :json
                                :headers         {"Accept"        "application/json"
                                                  "Content-Type"  "application/json"
                                                  "Authorization" token}

                                :handler         -handler
                                :error-handler   -error-handler})]


      (.log js/console (str "Response: " response))
      response)))


(defn -product-groups-table
  [data]
  [:table
   [:thead
    [:tr
     [:th "Id"]
     [:th "Name"]]]
   [:tbody
    (map (fn [item]
           (let [[my-key my-value] item]
             [:tr {:key my-key}
              [:td my-key]
              [:td [:a {:href (str "#/products/" my-key)} my-value]]]
             ))
         data)
    ]])

;; TODO: CONTINUE HERE: HOW TO SHOW THE PRODUCT GROUP LIST?
(defn productgroups-page
  "The actual page function called by simplefrontend.core."
  [token]
  (.log js/console (str "ENTER productgroups-page"))
  (let [response (-get-productgroups token)]

    (fn []
      [:div
       [:h1 "Product Groups"]
       (if (not (nil? @my-product-groups-atom))
         [:div (-product-groups-table @my-product-groups-atom)]
         )
       ;; During development:
       ;(if (not (nil? @my-dev-product-groups-atom))
       ;  [:div (-product-groups-table @my-dev-product-groups-atom)]
       ;  )
       [:div [:a {:href "#/"} "Back to Web Store Home Page"]]])))


