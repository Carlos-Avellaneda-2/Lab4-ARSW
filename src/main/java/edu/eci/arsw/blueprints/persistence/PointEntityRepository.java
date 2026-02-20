package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointEntityRepository extends JpaRepository<PointEntity, Integer> {
}