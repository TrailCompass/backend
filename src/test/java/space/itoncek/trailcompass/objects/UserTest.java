package space.itoncek.trailcompass.objects;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class UserTest {
	@Test
	void hasPermission() {
		User fullperms = new User(1,"itoncek", 0b111111);
		User noperms = new User(2,"dan", 0b000000);
		User someperms1 = new User(2,"dan", 0b010101);
		User someperms2 = new User(2,"dan", 0b011010);
		User someperms3 = new User(2,"dan", 0b100011);
		User someperms4 = new User(2,"dan", 0b101100);
		for (Permission value : Permission.values()) {
			assertTrue(fullperms.hasPermission(value));
			assertFalse(noperms.hasPermission(value));
			switch (value) {
				case ADMIN -> {
					//true
					assertTrue(someperms1.hasPermission(value));
					assertTrue(someperms3.hasPermission(value));
					//false
					assertFalse(someperms2.hasPermission(value));
					assertFalse(someperms4.hasPermission(value));
				}
				case ADD_USERS -> {
					//true
					assertTrue(someperms2.hasPermission(value));
					assertTrue(someperms3.hasPermission(value));
					//false
					assertFalse(someperms1.hasPermission(value));
					assertFalse(someperms4.hasPermission(value));
				}
				case MANAGE_GAMES -> {
					//true
					assertTrue(someperms1.hasPermission(value));
					assertTrue(someperms4.hasPermission(value));
					//false
					assertFalse(someperms2.hasPermission(value));
					assertFalse(someperms3.hasPermission(value));
				}
				case CREATE_GAMES -> {
					//true
					assertTrue(someperms2.hasPermission(value));
					assertTrue(someperms4.hasPermission(value));
					//false
					assertFalse(someperms1.hasPermission(value));
					assertFalse(someperms3.hasPermission(value));
				}
				case VIEW_ALL_GAMES -> {
					//true
					assertTrue(someperms1.hasPermission(value));
					assertTrue(someperms2.hasPermission(value));
					//false
					assertFalse(someperms3.hasPermission(value));
					assertFalse(someperms4.hasPermission(value));
				}
				case PLAY -> {
					//true
					assertTrue(someperms3.hasPermission(value));
					assertTrue(someperms4.hasPermission(value));
					//false
					assertFalse(someperms1.hasPermission(value));
					assertFalse(someperms2.hasPermission(value));
				}
			}
		}
	}
}