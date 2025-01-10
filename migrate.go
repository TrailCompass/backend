package main

func migrate(server *server) {
	_, err := server.db.Exec("CREATE TABLE IF NOT EXISTS tc_system.users ( id INT UNSIGNED auto_increment NOT NULL, name varchar(100) NOT NULL, passwordhash CHAR(64) NOT NULL, CONSTRAINT PRIMARY KEY (id), CONSTRAINT users_username_unique UNIQUE KEY (name), CONSTRAINT users_password_unique UNIQUE KEY (passwordhash)\n)\nENGINE=InnoDB\nDEFAULT CHARSET=utf8mb4;")
	if err != nil {
		println(err.Error())
		return
	}
	_, err2 := server.db.Exec("CREATE TABLE tc_system.games (id INT UNSIGNED auto_increment NOT NULL,owner INT UNSIGNED NOT NULL,db_name varchar(100) NOT NULL COMMENT 'without tc_',CONSTRAINT games_pk PRIMARY KEY (id),CONSTRAINT games_unique UNIQUE KEY (db_name),CONSTRAINT games_users_FK FOREIGN KEY (owner) REFERENCES tc_system.users(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")
	if err2 != nil {
		println(err2.Error())
		return
	}
}
