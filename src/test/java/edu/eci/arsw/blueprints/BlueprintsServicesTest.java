package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BlueprintsServicesTest {

    @Autowired
    private BlueprintsServices services;

    @BeforeEach
    void setUp() throws BlueprintPersistenceException {
        try {
            services.getBlueprint("testauthor", "testblueprint");
        } catch (BlueprintNotFoundException e) {
            services.addNewBlueprint(new Blueprint("testauthor", "testblueprint",
                List.of(new Point(0, 0), new Point(10, 10))));
        }
    }

    @Test
    void testAddNewBlueprint() throws BlueprintPersistenceException, BlueprintNotFoundException {
        Blueprint bp = new Blueprint("alice", "house", 
            List.of(new Point(1, 1), new Point(2, 2)));
        services.addNewBlueprint(bp);
        
        assertNotNull(services.getBlueprint("alice", "house"));
    }

    @Test
    void testAddDuplicateBlueprint() {
        Blueprint bp = new Blueprint("testauthor", "testblueprint", List.of());
        
        assertThrows(BlueprintPersistenceException.class, () -> {
            services.addNewBlueprint(bp);
        });
    }

    @Test
    void testGetAllBlueprints() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        assertFalse(blueprints.isEmpty());
    }

    @Test
    void testGetBlueprintsByAuthor() throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = services.getBlueprintsByAuthor("testauthor");
        assertFalse(blueprints.isEmpty());
        blueprints.forEach(bp -> assertEquals("testauthor", bp.getAuthor()));
    }

    @Test
    void testGetBlueprintsByAuthorNotFound() {
        assertThrows(BlueprintNotFoundException.class, () -> {
            services.getBlueprintsByAuthor("nonexistent");
        });
    }

    @Test
    void testGetBlueprint() throws BlueprintNotFoundException {
        Blueprint bp = services.getBlueprint("testauthor", "testblueprint");
        assertNotNull(bp);
        assertEquals("testauthor", bp.getAuthor());
        assertEquals("testblueprint", bp.getName());
    }

    @Test
    void testGetBlueprintNotFound() {
        assertThrows(BlueprintNotFoundException.class, () -> {
            services.getBlueprint("nonexistent", "nonexistent");
        });
    }

    @Test
    void testAddPoint() throws BlueprintNotFoundException {
        services.addPoint("testauthor", "testblueprint", 20, 20);
        
        Blueprint bp = services.getBlueprint("testauthor", "testblueprint");
        assertTrue(bp.getPoints().stream()
            .anyMatch(p -> p.x() == 20 && p.y() == 20));
    }

    @Test
    void testAddPointToNonexistentBlueprint() {
        assertThrows(BlueprintNotFoundException.class, () -> {
            services.addPoint("nonexistent", "nonexistent", 1, 1);
        });
    }
}
