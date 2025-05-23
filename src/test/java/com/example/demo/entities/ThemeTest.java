package com.example.demo.entities;

// annotate the class

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ThemeTest {
    // Test class for Theme entity
    // This class can be used to write unit tests for the Theme entity
    // using a testing framework like JUnit or TestNG.

    /**
     * Tests the creation of a Theme object and verifies its name.
     *
     * This method creates a new Theme object with the name "Java Programming"
     * and asserts that the name of the created Theme object is correctly set.
     */
    @Test
    public void testThemeCreation() {
        Theme theme = new Theme("Java Programming");
        assert theme.getName().equals("Java Programming");
    }
}
