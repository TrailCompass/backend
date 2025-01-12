package main

import (
	"database/sql"
	"github.com/go-sql-driver/mysql"
	"github.com/lmittmann/tint"
	"github.com/mattn/go-isatty"
	"log/slog"
	"net/http"
	"strconv"

    "github.com/labstack/echo/v4"

	"os"
	//    "crypto/hmac"
	//    "crypto/sha256"
	//    "encoding/hex"
)

type server struct {
	db     *sql.DB
	logger *slog.Logger
}

func main() {
	w := os.Stderr
	logger := slog.New(
		tint.NewHandler(w, &tint.Options{
			NoColor: !isatty.IsTerminal(w.Fd()),
		}),
	)

	logger.Info("Server is starting up...")
	var server server
	server.logger = logger
	var port, interr = strconv.Atoi(os.Getenv("PORT"))
	if interr != nil {
		logger.Warn("Port is invalid, using 8080")
		port = 8080
	}

	logger.Info("Connecting to database...")
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
		logger.Error(err.Error())
		return
	}
	server.db = db
	logger.Info("Database connected!...")
	logger.Info("Migrating...")

	migrate(&server)

	logger.Info("Migrated!")
	logger.Info("Finalising...")
    
    e := echo.New()
    e.POST("/uac/:event", server.webhook_auth)
	e.Logger.Fatal(e.Start(":8080"))

	logger.Info("Ready!")

	print("\033[H\033[2J")
	println(generateIntroMural())
	logger.Info("Listening on port " + strconv.Itoa(port) + "...")

	err = http.ListenAndServe(":"+strconv.Itoa(port), nil)
	if err != nil {
		logger.Error(err.Error())
		return
	}

	err = server.db.Close()
	if err != nil {
		logger.Error(err.Error())
		return
	}
}
