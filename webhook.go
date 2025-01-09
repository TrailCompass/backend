package main

import (
    "net/http"
    "encoding/json"
    "io"
    "strings"
)

type auth_payload struct {
    Username    string `json:"username"`
    Password    string `json:"password"`
    Token       []byte `json:"token"`
}

func webhook_auth(w http.ResponseWriter, r *http.Request) {
    if r.Method != http.MethodPost {
        w.WriteHeader(http.StatusMethodNotAllowed)
        return
    }

    event := strings.TrimPrefix(r.URL.Path, "/uac/")

    if event == "" {
        w.WriteHeader(http.StatusBadRequest)
    }

    body, err := io.ReadAll(r.Body)

    if err != nil {
        println("Failed to read request body:", err)
        w.WriteHeader(http.StatusInternalServerError)
        return
    }

    var payload auth_payload

    if err := json.Unmarshal(body, &payload); err != nil {
        println("Failed to parse payload JSON:", err)
         w.WriteHeader(http.StatusBadRequest)
        return
    }


    switch event {
    case "login":
        println("Hello World Motherf-")
    default:
        w.WriteHeader(http.StatusBadRequest)
        w.Write([]byte("Put the fries in the bag lil bro"))
        println("Bruh: ", event)
    }
}
