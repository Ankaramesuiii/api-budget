package com.example.demo.entities;

import com.example.demo.enums.Post;
import com.example.demo.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TrainingTest {

    private TeamMember teamMember;
    private Theme theme;

    @BeforeEach
    void setUp() {
        // Create a manager for the team
        Manager manager = new Manager();
        manager.setId(1);
        manager.setName("John Doe");
        manager.setEmail("john.doe@example.com");
        manager.setRole(Role.MANAGER);

        // Create a team
        Team team = new Team("Development Team", manager);
        team.setId(1);

        // Create a team member
        teamMember = new TeamMember();
        teamMember.setId(1);
        teamMember.setName("Jane Smith");
        teamMember.setEmail("jane.smith@example.com");
        teamMember.setRole(Role.TEAM_MEMBER);
        teamMember.setTeam(team);
        teamMember.setPost(Post.DEVELOPER);

        // Create a theme
        theme = new Theme("Java Programming");
        theme.setId(1);
    }

    /**
     * Test creating a Training with valid data
     */
    @Test
    void testCreateTrainingWithValidData() {
        // Create a training with valid data
        Training training = new Training(
                1,
                LocalDate.of(2023, 5, 1),
                LocalDate.of(2023, 5, 5),
                "JAVA-001",
                40,
                "Online",
                "Completed",
                "Present",
                LocalDate.now(),
                "DA-001",
                true,
                1000.0,
                "USD",
                3.5,
                3500.0,
                teamMember,
                theme
        );

        // Verify that the training was created correctly
        assertEquals(1, training.getId());
        assertEquals(LocalDate.of(2023, 5, 1), training.getStartDate());
        assertEquals(LocalDate.of(2023, 5, 5), training.getEndDate());
        assertEquals("JAVA-001", training.getCodeSession());
        assertEquals(40, training.getDuration());
        assertEquals("Online", training.getMode());
        assertEquals("Completed", training.getStatus());
        assertEquals("Present", training.getPresence());
        assertEquals("DA-001", training.getCodeDA());
        assertTrue(training.isInternalTrainer());
        assertEquals(1000.0, training.getPrice());
        assertEquals("USD", training.getCurrency());
        assertEquals(3.5, training.getExchangeRate());
        assertEquals(3500.0, training.getPriceTND());
        assertEquals(teamMember, training.getTeamMember());
        assertEquals(theme, training.getTheme());
    }

    /**
     * Test getters and setters of Training class
     */
    @Test
    void testGettersAndSetters() {
        // Create a training with default constructor
        Training training = new Training();
        
        // Set values using setters
        training.setId(2);
        training.setStartDate(LocalDate.of(2023, 6, 1));
        training.setEndDate(LocalDate.of(2023, 6, 10));
        training.setCodeSession("SPRING-001");
        training.setDuration(80);
        training.setMode("In-person");
        training.setStatus("Scheduled");
        training.setPresence("Required");
        training.setCreationDate(LocalDate.of(2023, 5, 15));
        training.setCodeDA("DA-002");
        training.setInternalTrainer(false);
        training.setPrice(2000.0);
        training.setCurrency("EUR");
        training.setExchangeRate(3.2);
        training.setPriceTND(6400.0);
        training.setTeamMember(teamMember);
        training.setTheme(theme);
        
        // Verify values using getters
        assertEquals(2, training.getId());
        assertEquals(LocalDate.of(2023, 6, 1), training.getStartDate());
        assertEquals(LocalDate.of(2023, 6, 10), training.getEndDate());
        assertEquals("SPRING-001", training.getCodeSession());
        assertEquals(80, training.getDuration());
        assertEquals("In-person", training.getMode());
        assertEquals("Scheduled", training.getStatus());
        assertEquals("Required", training.getPresence());
        assertEquals(LocalDate.of(2023, 5, 15), training.getCreationDate());
        assertEquals("DA-002", training.getCodeDA());
        assertFalse(training.isInternalTrainer());
        assertEquals(2000.0, training.getPrice());
        assertEquals("EUR", training.getCurrency());
        assertEquals(3.2, training.getExchangeRate());
        assertEquals(6400.0, training.getPriceTND());
        assertEquals(teamMember, training.getTeamMember());
        assertEquals(theme, training.getTheme());
    }

    /**
     * Test with null required fields
     */
    @Test
    void testWithNullRequiredFields() {
        // Create a training with valid data
        Training training = new Training();
        training.setId(3);
        training.setPrice(1500.0);
        training.setCurrency("USD");
        training.setExchangeRate(3.5);
        training.setPriceTND(5250.0);
        
        // Verify that null fields are null
        assertNull(training.getStartDate());
        assertNull(training.getEndDate());
        assertNull(training.getCodeSession());
        assertNull(training.getMode());
        assertNull(training.getStatus());
        assertNull(training.getPresence());
        assertNull(training.getCreationDate());
        assertNull(training.getCodeDA());
        assertNull(training.getTeamMember());
        assertNull(training.getTheme());
    }

    /**
     * Test with invalid dates (end date before start date)
     */
    @Test
    void testWithInvalidDates() {
        // Create a training with invalid dates
        Training training = new Training();
        LocalDate startDate = LocalDate.of(2023, 5, 10);
        LocalDate endDate = LocalDate.of(2023, 5, 5); // End date before start date
        
        training.setStartDate(startDate);
        training.setEndDate(endDate);
        
        // Verify that the dates are set correctly
        assertEquals(startDate, training.getStartDate());
        assertEquals(endDate, training.getEndDate());
        
        // Verify that end date is before start date
        assertTrue(training.getEndDate().isBefore(training.getStartDate()));
    }

    /**
     * Test price calculation in TND
     */
    @Test
    void testPriceCalculationInTND() {
        // Create a training with price information
        Training training = new Training();
        training.setPrice(1000.0);
        training.setExchangeRate(3.5);
        training.setPriceTND(3500.0);
        
        // Verify that the price in TND is calculated correctly
        assertEquals(3500.0, training.getPriceTND());
        
        // Verify that changing the price or exchange rate doesn't automatically update priceTND
        training.setPrice(2000.0);
        training.setExchangeRate(3.2);
        
        // The priceTND should still be the original value
        assertEquals(3500.0, training.getPriceTND());
        
        // Manually update priceTND
        training.setPriceTND(training.getPrice() * training.getExchangeRate());
        assertEquals(6400.0, training.getPriceTND());
    }

    /**
     * Test with negative price values
     */
    @Test
    void testWithNegativePriceValues() {
        // Create a training with negative price
        Training training = new Training();
        training.setPrice(-1000.0);
        training.setExchangeRate(3.5);
        training.setPriceTND(-3500.0);
        
        // Verify that negative values are accepted
        assertEquals(-1000.0, training.getPrice());
        assertEquals(3.5, training.getExchangeRate());
        assertEquals(-3500.0, training.getPriceTND());
    }

    /**
     * Test with invalid exchange rate (zero or negative)
     */
    @Test
    void testWithInvalidExchangeRate() {
        // Create a training with zero exchange rate
        Training training = new Training();
        training.setPrice(1000.0);
        training.setExchangeRate(0.0);
        training.setPriceTND(0.0);
        
        // Verify that zero exchange rate is accepted
        assertEquals(0.0, training.getExchangeRate());
        assertEquals(0.0, training.getPriceTND());
        
        // Set negative exchange rate
        training.setExchangeRate(-3.5);
        training.setPriceTND(-3500.0);
        
        // Verify that negative exchange rate is accepted
        assertEquals(-3.5, training.getExchangeRate());
        assertEquals(-3500.0, training.getPriceTND());
    }

    /**
     * Test with different currencies
     */
    @Test
    void testWithDifferentCurrencies() {
        // Create a training with USD currency
        Training trainingUSD = new Training();
        trainingUSD.setPrice(1000.0);
        trainingUSD.setCurrency("USD");
        trainingUSD.setExchangeRate(3.5);
        trainingUSD.setPriceTND(3500.0);
        
        // Verify USD currency
        assertEquals("USD", trainingUSD.getCurrency());
        assertEquals(3500.0, trainingUSD.getPriceTND());
        
        // Create a training with EUR currency
        Training trainingEUR = new Training();
        trainingEUR.setPrice(1000.0);
        trainingEUR.setCurrency("EUR");
        trainingEUR.setExchangeRate(3.8);
        trainingEUR.setPriceTND(3800.0);
        
        // Verify EUR currency
        assertEquals("EUR", trainingEUR.getCurrency());
        assertEquals(3800.0, trainingEUR.getPriceTND());
        
        // Create a training with TND currency
        Training trainingTND = new Training();
        trainingTND.setPrice(1000.0);
        trainingTND.setCurrency("TND");
        trainingTND.setExchangeRate(1.0);
        trainingTND.setPriceTND(1000.0);
        
        // Verify TND currency
        assertEquals("TND", trainingTND.getCurrency());
        assertEquals(1000.0, trainingTND.getPriceTND());
    }
}