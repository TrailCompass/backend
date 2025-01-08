package main

import (
    "net/http"

//    "crypto/hmac"
//    "crypto/sha256"
//    "encoding/hex"
)

type server struct {
    db_string string
}

func main() {
    //rows, err := db.Query("SELECT name FROM users WHERE age = $1", 21)

    //println(rows)

    http.HandleFunc("/users", webhook_users)

    println("Server is starting up...")

    http.ListenAndServe(":8080", nil)
}

func validate_signature(payload []byte, signature string, secret string) bool {
    return false
} // TODO
