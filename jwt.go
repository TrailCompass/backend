package main

import (
    "time"

    "github.com/golang-jwt/jwt"
)

func generate_jwt(username string) (string, error) {
	token := jwt.New(jwt.SigningMethodHS256)
	claims := token.Claims.(jwt.MapClaims)

	claims["authorized"] = true
	claims["username"] = username
	claims["exp"] = time.Now().Add(time.Hour * 24).Unix()

	tokenString, err := token.SignedString("Hello World")

	if err != nil {
        println("JWT Generation failed:", err.Error())
		return "", err
	}
	return tokenString, nil
}
