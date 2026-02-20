package edu.eci.arsw.blueprints.model;

import jakarta.persistence.*;

@Entity
@Table(name = "point")
public class PointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blueprint_id", nullable = false)
    private BlueprintEntity blueprint;

    @Column(nullable = false)
    private int x;
    @Column(nullable = false)
    private int y;

    public PointEntity() {}
    public PointEntity(BlueprintEntity blueprint, int x, int y) {
        this.blueprint = blueprint;
        this.x = x;
        this.y = y;
    }
    public Integer getId() { return id; }
    public BlueprintEntity getBlueprint() { return blueprint; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setBlueprint(BlueprintEntity blueprint) { this.blueprint = blueprint; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}