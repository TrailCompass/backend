package main

import (
	"database/sql"
	"github.com/go-sql-driver/mysql"
	"net/http"

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
		User:                 os.Getenv("MARIA_USER"),
		Passwd:               os.Getenv("MARIA_PASSWORD"),
		Net:                  "tcp",
		Addr:                 os.Getenv("MARIA"),
		DBName:               os.Getenv("MARIA_DB"),
		AllowNativePasswords: true,
	}

	db, err := sql.Open("mysql", cfg.FormatDSN())

	if err != nil {
		println(err.Error())
		return
	}
	server.db = db

	http.HandleFunc("/uac/", server.webhook_auth)

	println("Server is starting up...")

	err = http.ListenAndServe(":8080", nil)
	if err != nil {
		println(err.Error())
		return
	}

	err = server.db.Close()
	if err != nil {
		println(err.Error())
		return
	}
}

func validate_signature(payload []byte, signature string, secret string) bool {
	return false
} // TODO
