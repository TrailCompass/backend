package main

import (
    "net/http"
    "encoding/json"
    "io"
)

type webhook_payload struct {
    Event   string `json:"Event"`
    Cmd     string `json:"Cmd"`
    Sig     string `json:"Sig"`

}

func webhook_handler(w http.ResponseWriter, r *http.Request) {
    if r.Method != http.MethodPost {
        w.WriteHeader(http.StatusMethodNotAllowed)
        return
    }

    body, err := io.ReadAll(r.Body)

    if err != nil {
        println("Failed to read request body:", err)
        w.WriteHeader(http.StatusInternalServerError)
        return
    }

    var payload webhook_payload

    if err := json.Unmarshal(body, &payload); err != nil {
        println("Failed to parse payload JSON:", err)
         w.WriteHeader(http.StatusBadRequest)
        return
    }


    switch payload.Event {
    case "create":
        // TODO
    default:
        w.WriteHeader(http.StatusBadRequest)
        w.Write([]byte("Put the fries in the bag lil bro"))
        println("Bruh: ", payload.Event)
    }
}
