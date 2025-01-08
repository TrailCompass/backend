package main

import (
    "database/sql"

    _ "github.com/go-sql-driver/mysql"
)

func (server *server) add_user(id int, name string) {
    db, err := sql.Open("postgres", server.db_string)

    if err != nil {}
    
    // INSERT INTO players (id, name) VALUES (1, 'John Doe');

    _, err = db.Exec("INSERT INTO players (id, name) VALUES ($1, '$2');", id, name)

    if err != nil {}

    db.Close()

}   // TODO

func (server *server) remove_user(id int) {
    db, err := sql.Open("postgres", server.db_string)

    if err != nil {}

    db.Close()

} // TODO
