package main

import (
    "time"
    "errors"
    "net/http"

    "github.com/golang-jwt/jwt"
)

func generate_jwt(username string, password string) (string, error) {
	token := jwt.New(jwt.SigningMethodHS256)
	claims := token.Claims.(jwt.MapClaims)

	claims["username"] = username
	claims["exp"] = time.Now().Add(time.Hour * 24).Unix()
    claims["password"] = password

	token_string, err := token.SignedString("Hello World")

	if err != nil {
        println("JWT Generation failed:", err.Error())
		return "", err
	}
	return token_string, nil
}

func validate_jwt(w http.ResponseWriter, r *http.Request) (err error) {
    if r.Header["Token"] == nil {
        println("Token is invalid")
        return errors.New("Invalid JWT")
    }

    token, err := jwt.Parse(r.Header["Token"][0], func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, println("There was an error in parsing")
		}
		return "Hello World", nil
	})

    claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		println("Couldn't parse claims")
		return errors.New("Token error")
	}

    exp := claims["exp"].(float64)
	if int64(exp) < time.Now().Local().Unix() {
		println("Token is expired")
		return errors.New("Token error")
	}

	return nil
}
