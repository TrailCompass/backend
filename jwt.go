package main

import (
	"errors"
	"math/rand"
	"net/http"
	"time"

	"github.com/golang-jwt/jwt"
)

func getHMACToken(server *server) []byte {
	query, err := server.db.Query("SELECT * FROM tc_system.keystore WHERE `key`='jwt_key' LIMIT 1;")
	if err != nil {
		server.logger.Error(err.Error())
		return []byte(nil)
	}
	if !query.Next() {
		server.logger.Warn("no jwt_key found, generating")
		generate_HMAC_token(server)
		return getHMACToken(server)
	}
	return []byte("")
}

func generate_HMAC_token(server *server) {
	_, err := server.db.Exec("INSERT INTO tc_system.keystore (`key`, `value`) VALUES ('jwt_key', ?);", RandomString(64))
	if err != nil {
		server.logger.Error(err.Error())
		return
	}
}

func generate_jwt(id int, server *server) (string, error) {
	token := jwt.New(jwt.SigningMethodHS512)
	claims := token.Claims.(jwt.MapClaims)

	claims["id"] = id
	claims["exp"] = time.Now().Add(time.Hour).Unix()

	token_string, err := token.SignedString(getHMACToken(server))

	if err != nil {
		server.logger.Error("JWT Generation failed:")
		server.logger.Error(err.Error())
		return "", err
	}
	return token_string, nil
}

func validate_jwt(r *http.Request, server *server) (id int, err error) {
	if r.Header["Token"] == nil {
		server.logger.Error("Token is invalid")
		return -1, errors.New("invalid JWT")
	}

	token, err := jwt.Parse(r.Header["Token"][0], func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			server.logger.Debug("Unexpected signing method: %v", token.Header["alg"])
			return nil, errors.New("there was an error in parsing")
		}
		return getHMACToken(server), nil
	})

	if token == nil {
		server.logger.Debug("Token is invalid")
		return -1, errors.New("invalid JWT")
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		server.logger.Debug("Couldn't parse claims")
		return -1, errors.New("token error")
	}

	exp := claims["exp"].(float64)
	if int64(exp) < time.Now().Local().Unix() {
		server.logger.Debug("Token is expired")
		return -1, errors.New("token error")
	}
	i := int(claims["id"].(float64))
	return i, nil
}

func RandomString(n int) string {
	var letters = []rune("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")

	s := make([]rune, n)
	for i := range s {
		s[i] = letters[rand.Intn(len(letters))]
	}
	return string(s)
}
