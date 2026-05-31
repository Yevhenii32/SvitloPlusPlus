package com.noideasolutions.svitlo.service;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    void constructorShouldSetDefaultValues(){
        User user= new User("nikita","hash123","Guest");
        assertEquals("nikita",user.getUsername());
        assertEquals("hash123",user.getPasswordHash());
        assertEquals("Guest",user.getRole());
        assertEquals(5.00,user.getRating());
        assertEquals(0,user.getBonusPoints());
        assertEquals(0,user.getComplaintsCount());
        assertFalse(user.isBlocked());
    }
}
