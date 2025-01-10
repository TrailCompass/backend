package main

import (
	"database/sql"
	"encoding/json"
	"io"
	"net/http"
	"strconv"
	"strings"

	_ "github.com/go-sql-driver/mysql"
)

type auth_payload struct {
	Username string `json:"username"`
	Password string `json:"passwordhash"`
}

type auth_login_response struct {
	Token string `json:"token"`
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
		server.logger.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	var payload auth_payload

	if err := json.Unmarshal(body, &payload); err != nil {
		server.logger.Error(err.Error())
		w.WriteHeader(http.StatusBadRequest)
		return
	}

	switch event {
	case "login":
		rows, err := server.db.Query("SELECT id, name, passwordhash FROM tc_system.users WHERE name=? and passwordhash=?;", payload.Username, payload.Password)
		if err != nil {
			server.logger.Error(err.Error())
			w.WriteHeader(http.StatusBadRequest)
			return
		}
		defer func(rows *sql.Rows) {
			err := rows.Close()
			if err != nil {
				server.logger.Error(err.Error())
			}
		}(rows)

		if rows.Next() {
			var id int
			var name string
			var password string
			err := rows.Scan(&id, &name, &password)
			if err != nil {
				server.logger.Error(err.Error())
				return
			}
			token, err := generate_jwt(id, server)
			//TODO: Generate JWT token to be returned

			w.WriteHeader(http.StatusOK)
			w.Header().Set("Content-Type", "application/json")
			err = json.NewEncoder(w).Encode(auth_login_response{token})
			if err != nil {
				server.logger.Error(err.Error())
				return
			}
			return
		} else {
			w.WriteHeader(http.StatusUnauthorized)
			_, err := w.Write([]byte("auth failed"))
			if err != nil {
				server.logger.Error(err.Error())
				return
			}
			return
		}
	case "register":
		//TODO: verify auth token
		_, err := server.db.Exec("INSERT INTO tc_system.users (name, passwordhash) VALUES(?, ?);", payload.Username, payload.Password)
		if err != nil {
			server.logger.Error(err.Error())
			_, err := w.Write([]byte("Unable to register user, probably same name/password as already existing user!"))
			if err != nil {
				server.logger.Error(err.Error())
				return
			}
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		w.WriteHeader(http.StatusOK)
		_, err = w.Write([]byte("ok"))
		if err != nil {
			server.logger.Error(err.Error())
			return
		}
		return
	case "isLoggedIn":
		id, err := validate_jwt(r, server)
		if err != nil {
			server.logger.Error("Attempted unauthorized request")
			server.logger.Error(err.Error())
			w.WriteHeader(http.StatusOK)
			_, err2 := w.Write([]byte("attempted unauthorized request, this incident has been reported!"))
			if err2 != nil {
				server.logger.Error(err.Error())
				return
			}
			return
		}
		w.WriteHeader(http.StatusOK)
		_, err = w.Write([]byte("Authorised as user #" + strconv.Itoa(id)))
		if err != nil {
			server.logger.Error(err.Error())
			return
		}
		return
	default:
		w.WriteHeader(http.StatusBadRequest)
		_, err := w.Write([]byte("Put the fries in the bag lil bro"))
		if err != nil {
			server.logger.Error(err.Error())
			return
		}
		server.logger.Info("Bruh: ", event)
	}
}
