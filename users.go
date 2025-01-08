package main

import (
    _ "github.com/go-sql-driver/mysql"
)

func (server *server) add_user(name string) {
    // INSERT INTO players (id, name) VALUES (1, 'John Doe');

    _, err := server.db.Exec("INSERT INTO players (name) VALUES ('$1');", name)

    if err != nil {}

}   // TODO

func (server *server) remove_user(id int) {
} // TODO
