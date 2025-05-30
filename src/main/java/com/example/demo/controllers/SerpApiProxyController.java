package com.example.demo.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/serpapi")
@Tag(name = "SerpAPI Proxy", description = "Proxies requests to SerpAPI for flights and hotels")
public class SerpApiProxyController {
    
    private static final Logger logger = LoggerFactory.getLogger(SerpApiProxyController.class);

    @Value("${serpapi.key}")
    private String serpApiKey;

    @Value("${serpapi.url}")
    private String serpapiUrl;

    private final RestTemplate restTemplate;

    @Autowired
    public SerpApiProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Operation(summary = "Search flights using SerpAPI")
    @GetMapping("/flights/search")
    public ResponseEntity<String> searchFlights(
            @Parameter(description = "Departure airport IATA code") @RequestParam(name = "departure_id") String departureId,
            @Parameter(description = "Arrival airport IATA code") @RequestParam(name = "arrival_id") String arrivalId,
            @Parameter(description = "Outbound date in format YYYY-MM-DD") @RequestParam(name = "outbound_date") String outboundDate,
            @Parameter(description = "Return date in format YYYY-MM-DD") @RequestParam(name = "return_date", required = false) String returnDate,
            @Parameter(description = "Number of adult passengers") @RequestParam(defaultValue = "1") int adults,
            @Parameter(description = "Trip type: 1 for round-trip, 2 for one-way") @RequestParam(name = "type", defaultValue = "1") int type,
            @Parameter(description = "Google location (gl) parameter") @RequestParam(name = "gl") String gl,
            @Parameter(description = "Google language (hl) parameter") @RequestParam(name = "hl") String hl,
            @Parameter(description = "Sorting method (e.g., 2 for best flights)") @RequestParam(name = "sort_by",defaultValue = "2") int sortBy,
            @Parameter(description = "Optional token for fetching return flights") @RequestParam(name= "departure_token", required = false) String departureToken) {

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serpapiUrl)
                    .queryParam("engine", "google_flights")
                    .queryParam("departure_id", departureId)
                    .queryParam("arrival_id", arrivalId)
                    .queryParam("outbound_date", outboundDate)
                    .queryParam("adults", adults)
                    .queryParam("type", type)
                    .queryParam("currency", "EUR")
                    .queryParam("hl", hl)
                    .queryParam("gl", gl)
                    .queryParam("sort_by", sortBy)
                    .queryParam("api_key", serpApiKey);

            if (type == 1 && (returnDate == null || returnDate.isEmpty())) {
                return ResponseEntity.badRequest().body("return_date is required");
            }

            if (returnDate != null && !returnDate.isEmpty()) {
                builder.queryParam("return_date", returnDate);
            }
            if (departureToken != null && !departureToken.isEmpty()) {
                builder.queryParam("departure_token", UriUtils.encode(departureToken, StandardCharsets.UTF_8));
            }

            URI uri = builder.build(true).toUri(); // preserve existing encoding (no double encoding)
            String result = restTemplate.getForObject(uri, String.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error calling SerpAPI: " + e.getMessage());
        }
    }

    @Operation(summary = "Search hotels using SerpAPI")
    @GetMapping("/hotels/search")
    public ResponseEntity<Object> searchHotels(
            @Parameter(description = "Search query (e.g., city or hotel name)") @RequestParam(name = "q") String query,
            @Parameter(description = "Check-in date (YYYY-MM-DD)") @RequestParam(name = "check_in_date") String checkInDate,
            @Parameter(description = "Check-out date (YYYY-MM-DD)") @RequestParam(name = "check_out_date") String checkOutDate,
            @Parameter(description = "Number of adults") @RequestParam(name = "adults", defaultValue = "1") int adults,
            @Parameter(description = "Optional hotel property token") @RequestParam(name = "property_token", required = false) String propertyToken,
            @Parameter(description = "Google location (gl) parameter") @RequestParam(name = "gl", defaultValue = "fr") String gl,
            @Parameter(description = "Google language (hl) parameter") @RequestParam(name = "hl", defaultValue = "fr") String hl,
            @Parameter(description = "Currency (e.g., EUR)") @RequestParam(name = "currency", defaultValue = "EUR") String currency
    ) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serpapiUrl)
                    .queryParam("engine", "google_hotels")
                    .queryParam("q", URLEncoder.encode(query, StandardCharsets.UTF_8))
                    .queryParam("check_in_date", checkInDate)
                    .queryParam("check_out_date", checkOutDate)
                    .queryParam("adults", adults)
                    .queryParam("currency", currency)
                    .queryParam("hl", hl)
                    .queryParam("gl", gl)
                    .queryParam("api_key", serpApiKey)
                    .queryParam("sort_by",3);

            if (propertyToken != null && !propertyToken.isEmpty()){
                builder.queryParam("property_token", propertyToken);
            }

            URI uri = builder.build(true).toUri();
            logger.debug("Hotels SerpAPI URL: {}", uri);

            String result = restTemplate.getForObject(uri, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(result);

            JsonNode properties = root.path("properties");

            if (properties.isMissingNode() || !properties.isArray()) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error calling SerpAPI (hotels): " + e.getMessage());
        }
    }
}