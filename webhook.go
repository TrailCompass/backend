package main

import (
	"database/sql"
	"encoding/json"
	"io"
	"net/http"
	"strconv"

    "github.com/labstack/echo/v4"
	_ "github.com/go-sql-driver/mysql"
)

type auth_payload struct {
	Username string `json:"username"`
	Password string `json:"passwordhash"`
}

type auth_login_response struct {
	Token string `json:"token"`
}

func (server *server) webhook_auth(c echo.Context) error {
	// event := strings.TrimPrefix(r.URL.Path, "/uac/")
    event := c.Param("event")
    r := c.Response()
    
    body, err := io.ReadAll(c.Request().Body)
    if err != nil {
        server.logger.Error(err.Error())
        return c.String(http.StatusInternalServerError, "Failed to read body")
    }

	var payload auth_payload

	if err := json.Unmarshal(body, &payload); err != nil {
		server.logger.Error(err.Error())
		return c.String(http.StatusBadRequest, "Failed to Unmarshal the json request")
	}

	switch event {
	case "login":
		rows, err := server.db.Query("SELECT id, name, passwordhash FROM tc_system.users WHERE name=? and passwordhash=?;", payload.Username, payload.Password)
		if err != nil {
			server.logger.Error(err.Error())
			return c.String(http.StatusBadRequest, "Failed to execute the SQL Query")
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
				return c.String(http.StatusInternalServerError, "No idea what happened")
			}
			token, err := generate_jwt(id, server)
			//TODO: Generate JWT token to be returned

			r.WriteHeader(http.StatusOK)
			r.Header().Set("Content-Type", "application/json")
			// err = json.NewEncoder(w).Encode(auth_login_response{token})
			err = c.JSON(http.StatusOK, auth_login_response{token})
            if err != nil {
				server.logger.Error(err.Error())
				return nil
			}
            r.Flush()
			return nil
		} else {
			// w.WriteHeader(http.StatusUnauthorized)
			// _, err := w.Write([]byte("auth failed"))
            r.WriteHeader(http.StatusUnauthorized)
            r.Write([]byte("auth failed"))
			if err != nil {
				server.logger.Error(err.Error())
			}
			r.Flush()
            return nil
		}
	case "register":
		//TODO: verify auth token
		_, err := server.db.Exec("INSERT INTO tc_system.users (name, passwordhash) VALUES(?, ?);", payload.Username, payload.Password)
		if err != nil {
			server.logger.Error(err.Error())
            _, err := r.Write([]byte("Unable to register user, probably same name/password as already existing user!"))
			if err != nil {
				server.logger.Error(err.Error())
                r.WriteHeader(http.StatusInternalServerError)
                r.Flush()
				return nil
			}
            r.WriteHeader(http.StatusUnauthorized)
            r.Flush()
			return nil
		}
		r.WriteHeader(http.StatusOK)
		_, err = r.Write([]byte("ok"))
		if err != nil {
			server.logger.Error(err.Error())
		}
        r.Flush()
		return nil
	case "isLoggedIn":
		id, err := validate_jwt(r, server)
		if err != nil {
			server.logger.Error("Attempted unauthorized request")
			server.logger.Error(err.Error())
			r.WriteHeader(http.StatusOK)
			_, err2 := r.Write([]byte("attempted unauthorized request, this incident has been reported!"))
			if err2 != nil {
				server.logger.Error(err.Error())
			}
            r.Flush()
			return nil
		}
		r.WriteHeader(http.StatusOK)
		_, err = r.Write([]byte("Authorised as user #" + strconv.Itoa(id)))
		if err != nil {
			server.logger.Error(err.Error())
		}
        r.Flush()
		return nil
	default:
		r.WriteHeader(http.StatusBadRequest)
		_, err := r.Write([]byte("Put the fries in the bag lil bro"))
		if err != nil {
			server.logger.Error(err.Error())
            r.Flush()
			return nil
		}
        r.Flush()
		server.logger.Info("Bruh: ")
		server.logger.Info(event)
	}
    return nil
}
