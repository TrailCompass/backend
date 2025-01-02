package main

import (
    "net/http"

    "crypto/hmac"
    "crypto/sha256"
    "encoding/hex"
)

func main() {
    http.HandleFunc("/trailcompass", webhook_handler)

    println("Server is starting up...")

    http.ListenAndServe(":8080", nil)
}

func validate_signature(payload []byte, signature string, secret string) bool {
    mac := hmac.New(sha256.New, []byte(secret))
    mac.Write(payload)
    expected_mac := hex.EncodeToString(mac.Sum(nil))
    return hmac.Equal([]byte(signature), []byte(expected_mac))
}
