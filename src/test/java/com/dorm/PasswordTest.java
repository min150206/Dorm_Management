package com.dorm;

import com.dorm.logic.code.PasswordUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {
    @Test void passwordsUseSaltAndRejectWrongInput() {
        String one = PasswordUtil.hash("student123");
        assertNotEquals(one, PasswordUtil.hash("student123"));
        assertTrue(PasswordUtil.matches("student123", one));
        assertFalse(PasswordUtil.matches("incorrect", one));
        assertFalse(PasswordUtil.matches(null, one));
        assertFalse(PasswordUtil.matches("student123", "pbkdf2$broken$hash"));
        assertFalse(PasswordUtil.matches("student123", "student123"));
    }
    @Test void passwordLengthIsChecked() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash("abc"));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(null));
    }
}
