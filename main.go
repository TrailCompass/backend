package main

import (
    "net/http"
    "database/sql"
     _ "github.com/go-sql-driver/mysql"

    "os"

//    "crypto/hmac"
//    "crypto/sha256"
//    "encoding/hex"
)

type server struct {
    db *sql.DB
    db_string string
}

func main() {
    var server server

    db, err := sql.Open("mysql", server.db_string)

    if err != nil {}
    server.db = db

    server.db_string = os.Getenv("POSTGRES_DB")

    http.HandleFunc("/users", webhook_users)

    println("Server is starting up...")

    http.ListenAndServe(":8080", nil)

    server.db.Close()
}

func validate_signature(payload []byte, signature string, secret string) bool {
    return false
} // TODO
