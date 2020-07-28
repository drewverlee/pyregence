(ns pyregence.pages.login
  (:require [reagent.core :as r]
            [clojure.core.async :refer [go <! timeout]]
            [pyregence.utils  :as u]
            [pyregence.styles :as $]
            [pyregence.components.common    :refer [simple-form]]
            [pyregence.components.messaging :refer [toast-message!
                                                    toast-message
                                                    process-toast-messages!]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def pending? (r/atom false))

(defonce forgot?  (r/atom false))
(defonce email    (r/atom ""))
(defonce password (r/atom ""))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; API Calls
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn log-in! []
  (go
    (if (:success (<! (u/call-clj-async! "log-in" @email @password)))
      (let [url (:redirect-from (u/get-session-storage) "/near-term-forecast")]
        (u/clear-session-storage!)
        (u/jump-to-url! url))
      ;; TODO, it would be helpful to show the user which of the two errors it actually is.
      (toast-message! ["Invalid login credentials. Please try again."
                       "If you feel this is an error, check your email for the verification email."]))))

(defn request-password! []
  (go
    (reset! pending? true)
    (toast-message! "Submitting request. This may take a moment...")
    (if (:success (<! (u/call-clj-async! "send-email" @email :reset)))
      (do (toast-message! "Please check your email for a password reset link.")
          (<! (timeout 4000))
          (u/jump-to-url! "/near-term-forecast"))
      (do (toast-message! ["An error occurred."
                           "Please try again shortly or contact support@pyregence.org for help."])
          (reset! pending? false)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; UI Components
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn reset-link []
  [:a {:style ($/align :block :left)
       :href "#"
       :on-click #(reset! forgot? true)} "Forgot Password?"])

(defn root-component [_]
  (process-toast-messages!)
  (fn [_]
    [:<>
     [toast-message]
     [:div {:style ($/combine ($/disabled-group @pending?)
                              {:display "flex" :justify-content "center" :margin "5rem"})}
      (if @forgot?
        [simple-form
         "Request New Password"
         "Submit"
         [["Email" email "text"]]
         request-password!]
        [simple-form
         "Log in"
         "Log in"
         [["Email"    email    "text"]
          ["Password" password "password"]]
         log-in!
         reset-link])]]))