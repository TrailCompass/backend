package main

import (
    "net/http"
    "database/sql"
     "github.com/go-sql-driver/mysql"

    "os"

//    "crypto/hmac"
//    "crypto/sha256"
//    "encoding/hex"
)

type server struct {
    db *sql.DB
}

func main() {
    var server server

    cfg := mysql.Config{
        User:   os.Getenv("DBUSER"),
        Passwd: os.Getenv("DBPASS"),
        Net:    "tcp",
        Addr:   "127.0.0.1:3306",
        DBName: "tc_system",
        AllowNativePasswords: true,
    }

    db, err := sql.Open("mysql", cfg.FormatDSN())

    if err != nil {}
    server.db = db

    http.HandleFunc("/uac/", server.webhook_auth)

    println("Server is starting up...")

    http.ListenAndServe(":8080", nil)

    server.db.Close()
}

func validate_signature(payload []byte, signature string, secret string) bool {
    return false
} // TODO
