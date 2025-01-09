package main

import (
	"database/sql"
	"encoding/json"
	"io"
	"net/http"
	"strings"

	_ "github.com/go-sql-driver/mysql"
)

type auth_payload struct {
	Username string `json:"username"`
	Password string `json:"passwordhash"`
	Token    []byte `json:"token"`
}

func (server *server) webhook_auth(w http.ResponseWriter, r *http.Request) {
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
		rows, err := server.db.Query("SELECT id, name, passwordhash FROM tc_system.users WHERE name=? and passwordhash=?;", payload.Username, payload.Password)
		if err != nil {
			println("login err")
			println(err.Error())
			w.WriteHeader(http.StatusBadRequest)
			return
		}
		defer func(rows *sql.Rows) {
			err := rows.Close()
			if err != nil {
				println(err.Error())
			}
		}(rows)

		if rows.Next() {
			var id int
			var name string
			var password string
			err := rows.Scan(&id, &name, &password)
			if err != nil {
				println(err.Error())
				return
			}
			//TODO: Generate JWT token to be returned
			w.WriteHeader(http.StatusOK)
			_, err = w.Write([]byte("You are logged in!"))
			if err != nil {
				println(err.Error())
				return
			}
			return
		} else {
			w.WriteHeader(http.StatusUnauthorized)
			_, err := w.Write([]byte("auth failed"))
			if err != nil {
				println(err.Error())
				return
			}
			return
		}
	case "register":
		//TODO: verify auth token
		_, err := server.db.Exec("INSERT INTO tc_system.users (name, passwordhash) VALUES(?, ?);", payload.Username, payload.Password)
		if err != nil {
			println(err.Error())
			_, err := w.Write([]byte("Unable to register user, probably same name/password as already existing user!"))
			if err != nil {
				println(err.Error())
				return
			}
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		w.WriteHeader(http.StatusOK)
		_, err = w.Write([]byte("ok"))
		if err != nil {
			println(err.Error())
			return
		}
		return
	default:
		w.WriteHeader(http.StatusBadRequest)
		_, err := w.Write([]byte("Put the fries in the bag lil bro"))
		if err != nil {
			println(err.Error())
			return
		}
		println("Bruh: ", event)
	}
}
