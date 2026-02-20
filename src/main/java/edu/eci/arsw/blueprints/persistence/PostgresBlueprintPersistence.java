package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Primary
public class PostgresBlueprintPersistence implements BlueprintPersistence {
    private final BlueprintEntityRepository blueprintRepo;
    private final PointEntityRepository pointRepo;

    @Autowired
    public PostgresBlueprintPersistence(BlueprintEntityRepository blueprintRepo, PointEntityRepository pointRepo) {
        this.blueprintRepo = blueprintRepo;
        this.pointRepo = pointRepo;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (blueprintRepo.findByAuthorAndName(bp.getAuthor(), bp.getName()).isPresent()) {
            throw new BlueprintPersistenceException("Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }
        BlueprintEntity entity = new BlueprintEntity(bp.getAuthor(), bp.getName());
        List<PointEntity> points = bp.getPoints().stream()
                .map(p -> new PointEntity(entity, p.x(), p.y()))
                .collect(Collectors.toList());
        entity.setPoints(points);
        blueprintRepo.save(entity);
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        BlueprintEntity entity = blueprintRepo.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException("Blueprint not found: " + author + "/" + name));
        return toBlueprint(entity);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<BlueprintEntity> entities = blueprintRepo.findByAuthor(author);
        if (entities.isEmpty()) throw new BlueprintNotFoundException("No blueprints for author: " + author);
        return entities.stream().map(this::toBlueprint).collect(Collectors.toSet());
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return blueprintRepo.findAll().stream().map(this::toBlueprint).collect(Collectors.toSet());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BlueprintEntity entity = blueprintRepo.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException("Blueprint not found: " + author + "/" + name));
        PointEntity point = new PointEntity(entity, x, y);
        entity.getPoints().add(point);
        blueprintRepo.save(entity);
    }

    private Blueprint toBlueprint(BlueprintEntity entity) {
        List<Point> points = entity.getPoints().stream()
                .map(p -> new Point(p.getX(), p.getY()))
                .collect(Collectors.toList());
        return new Blueprint(entity.getAuthor(), entity.getName(), points);
    }
}
