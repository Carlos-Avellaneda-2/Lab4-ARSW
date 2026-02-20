package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BlueprintsAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlueprintsServices services;

    @Test
    void testGetAllBlueprints() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("execute ok"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testCreateBlueprint() throws Exception {
        String json = """
                {
                    "author": "testauthor",
                    "name": "testblueprint",
                    "points": [
                        { "x": 1, "y": 1 },
                        { "x": 2, "y": 2 }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/blueprints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("created"));
    }

    @Test
    void testCreateDuplicateBlueprint() throws Exception {
        services.addNewBlueprint(new Blueprint("author1", "bp1", 
            List.of(new Point(0, 0))));

        String json = """
                {
                    "author": "author1",
                    "name": "bp1",
                    "points": []
                }
                """;

        mockMvc.perform(post("/api/v1/blueprints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void testGetBlueprintsByAuthor() throws Exception {
        services.addNewBlueprint(new Blueprint("author2", "bp2", 
            List.of(new Point(5, 5))));

        mockMvc.perform(get("/api/v1/blueprints/author2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetBlueprintsByAuthorNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message", containsString("No blueprints")));
    }

    @Test
    void testGetSpecificBlueprint() throws Exception {
        services.addNewBlueprint(new Blueprint("author3", "bp3", 
            List.of(new Point(10, 10), new Point(20, 20))));

        mockMvc.perform(get("/api/v1/blueprints/author3/bp3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.author").value("author3"))
                .andExpect(jsonPath("$.data.name").value("bp3"))
                .andExpect(jsonPath("$.data.points").isArray());
    }

    @Test
    void testGetSpecificBlueprintNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints/nonexistent/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void testAddPoint() throws Exception {
        services.addNewBlueprint(new Blueprint("author4", "bp4", 
            List.of(new Point(0, 0))));

        String json = """
                {
                    "x": 50,
                    "y": 50
                }
                """;

        mockMvc.perform(put("/api/v1/blueprints/author4/bp4/points")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(202))
                .andExpect(jsonPath("$.message").value("point added"));
    }

    @Test
    void testAddPointToNonexistentBlueprint() throws Exception {
        String json = """
                {
                    "x": 1,
                    "y": 1
                }
                """;

        mockMvc.perform(put("/api/v1/blueprints/nonexistent/nonexistent/points")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
