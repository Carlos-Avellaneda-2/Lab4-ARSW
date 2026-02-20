# Lab4-ARSW - REST API Blueprints

**Carlos Andres Avellaneda Franco**


---

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 16 (o Docker)
- Postman (para pruebas de endpoints)

---

## Instalación de PostgreSQL

### Opción 1: Usando Docker 

Ejecuta este comando para levantar una instancia de PostgreSQL:

```bash
docker run --name blueprints-db -e POSTGRES_DB=blueprints -e POSTGRES_USER=blueuser -e POSTGRES_PASSWORD=bluepass -p 5432:5432 -d postgres:16
```

**Credenciales:**
- Base de datos: `blueprints`
- Usuario: `blueuser`
- Contraseña: `bluepass`
- Puerto: `5432`

### Opción 2: PostgreSQL Local

Instala PostgreSQL desde [postgresql.org](https://www.postgresql.org/download/) y crea manualmente:

```sql
CREATE DATABASE blueprints;
CREATE USER blueuser WITH PASSWORD 'bluepass';
GRANT ALL PRIVILEGES ON DATABASE blueprints TO blueuser;
```

### Creación de Tablas

Ejecuta este script SQL en tu base de datos (con DBeaver, pgAdmin o psql):

```sql
CREATE TABLE blueprint (
    id SERIAL PRIMARY KEY,
    author VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT unique_author_name UNIQUE (author, name)
);

CREATE TABLE point (
    id SERIAL PRIMARY KEY,
    blueprint_id INTEGER REFERENCES blueprint(id) ON DELETE CASCADE,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL
);
```

---

## Configuración de Spring Boot

### 1. Dependencias (pom.xml)

Se agregaron las siguientes dependencias:

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.0</version>
    <scope>runtime</scope>
</dependency>

<!-- H2 para pruebas -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. Configuración de Conexión (application.properties)

```properties
spring.mvc.pathmatch.matching-strategy=ant_path_matcher

# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/blueprints
spring.datasource.username=blueuser
spring.datasource.password=bluepass
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

### 3. Habilitación de Repositorios JPA (BlueprintsApplication.java)

```java
@SpringBootApplication
@EnableJpaRepositories(basePackages = "edu.eci.arsw.blueprints.persistence")
public class BlueprintsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlueprintsApplication.class, args);
    }
}
```

---


---

## Clases Implementadas

### 1. Entidades JPA

#### BlueprintEntity.java
Mapeo JPA de Blueprint a la tabla `blueprint` en PostgreSQL:

```java
@Entity
@Table(name = "blueprint", uniqueConstraints = @UniqueConstraint(columnNames = {"author", "name"}))
public class BlueprintEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "blueprint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PointEntity> points = new ArrayList<>();
    // ... getters y setters
}
```

#### PointEntity.java
Mapeo JPA de Point a la tabla `point`:

```java
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
    // ... getters y setters
}
```

### 2. Repositorios Spring Data JPA

#### BlueprintEntityRepository.java
```java
@Repository
public interface BlueprintEntityRepository extends JpaRepository<BlueprintEntity, Integer> {
    Optional<BlueprintEntity> findByAuthorAndName(String author, String name);
    List<BlueprintEntity> findByAuthor(String author);
}
```

#### PointEntityRepository.java
```java
@Repository
public interface PointEntityRepository extends JpaRepository<PointEntity, Integer> {
}
```

### 3. Persistencia PostgreSQL

#### PostgresBlueprintPersistence.java
Implementación que conecta la lógica de negocio con PostgreSQL, usando los repositorios JPA:

```java
@Repository
@Primary
public class PostgresBlueprintPersistence implements BlueprintPersistence {
    private final BlueprintEntityRepository blueprintRepo;
    private final PointEntityRepository pointRepo;

    // Implementa todos los métodos de BlueprintPersistence usando JPA
    // - saveBlueprint()
    // - getBlueprint()
    // - getBlueprintsByAuthor()
    // - getAllBlueprints()
    // - addPoint()
}
```

### 4. Respuesta Uniforme de API

#### ApiResponse.java
```java
public record ApiResponse<T>(int code, String message, T data) {}
```

Esta clase estandariza todas las respuestas de la API en el siguiente formato:

```json
{
  "code": 200,
  "message": "execute ok",
  "data": { ... }
}
```

---

## Endpoints de la API

### 1. **GET /api/v1/blueprints** - Obtener todos los blueprints

```
GET http://localhost:8080/api/v1/blueprints
```

**Respuesta esperada (200 OK):**
```json
{
  "code": 200,
  "message": "execute ok",
  "data": [
    {
      "author": "john",
      "name": "house",
      "points": [...]
    }
  ]
}
```

---

### 2. **GET /api/v1/blueprints/{author}** - Obtener blueprints por autor

```
GET http://localhost:8080/api/v1/blueprints/john
```

**Respuesta esperada (200 OK):**
```json
{
  "code": 200,
  "message": "execute ok",
  "data": [...]
}
```

**Respuesta si no existe (404 Not Found):**
```json
{
  "code": 404,
  "message": "No blueprints for author: unknown",
  "data": null
}
```

---

### 3. **GET /api/v1/blueprints/{author}/{bpname}** - Obtener un blueprint específico

```
GET http://localhost:8080/api/v1/blueprints/john/house
```

**Respuesta esperada (200 OK):**
```json
{
  "code": 200,
  "message": "execute ok",
  "data": {
    "author": "john",
    "name": "house",
    "points": [
      { "x": 0, "y": 0 },
      { "x": 10, "y": 0 }
    ]
  }
}
```

---

### 4. **POST /api/v1/blueprints** - Crear un nuevo blueprint

```
POST http://localhost:8080/api/v1/blueprints
Content-Type: application/json
```

**Body:**
```json
{
  "author": "alice",
  "name": "livingroom",
  "points": [
    { "x": 10, "y": 5 },
    { "x": 15, "y": 10 },
    { "x": 20, "y": 15 }
  ]
}
```

**Respuesta esperada (201 Created):**
```json
{
  "code": 201,
  "message": "created",
  "data": null
}
```

**Si el blueprint ya existe (403 Forbidden):**
```json
{
  "code": 403,
  "message": "Blueprint already exists: alice:livingroom",
  "data": null
}
```

---

### 5. **PUT /api/v1/blueprints/{author}/{bpname}/points** - Agregar un punto

```
PUT http://localhost:8080/api/v1/blueprints/alice/livingroom/points
Content-Type: application/json
```

**Body:**
```json
{
  "x": 25,
  "y": 20
}
```

**Respuesta esperada (202 Accepted):**
```json
{
  "code": 202,
  "message": "point added",
  "data": null
}
```

---

## Pruebas de los Endpoints

Las siguientes capturas muestran ejemplos reales del funcionamiento de los endpoints principales usando la API REST:

### 1. GET /api/v1/blueprints - Obtener todos los blueprints

Esta petición retorna la lista completa de blueprints almacenados en la base de datos. En la imagen se observa una respuesta exitosa con el formato uniforme `ApiResponse`, donde el campo `data` contiene el arreglo de blueprints.

![Get Blueprints](img/Get%20blueprints.png)

---

### 2. GET /api/v1/blueprints/{author}/{bpname} - Obtener blueprint específico

Permite consultar un blueprint concreto indicando el autor y el nombre. La imagen muestra la respuesta con los datos del blueprint solicitado, incluyendo el listado de puntos.

![Get blueprints {author} {name}](img/Get%20blueprints%20%7Bauthor%7D%20%7Bname%7D.png)

---

### 3. POST /api/v1/blueprints - Crear un nuevo blueprint

Permite registrar un nuevo blueprint enviando el autor, nombre y puntos. En la imagen se observa la petición exitosa y la respuesta con código 201 y mensaje de creación.

![Post blueprints](img/Post%20blueprints.png)

---

### 4. PUT /api/v1/blueprints/{author}/{bpname}/points - Agregar un punto a un blueprint

Permite agregar un nuevo punto a un blueprint existente. La imagen muestra la petición y la respuesta indicando que el punto fue agregado correctamente.

![Put Blueprints](img/Put%20Blueprints.png)

---

## Formato de Respuesta (ApiResponse)

Todas las respuestas de la API siguen un formato uniforme implementado mediante la clase genérica `ApiResponse<T>`:

```java
public record ApiResponse<T>(int code, String message, T data) {}
```

### Ventajas:

- **Consistencia:** Todas las respuestas tienen la misma estructura (`code`, `message`, `data`).
- **Mantenibilidad:** Cambios en el formato afectan un solo lugar.
- **Claridad:** El cliente siempre sabe qué esperar.
- **Escalabilidad:** Fácil de extender con información adicional (timestamps, request ID, etc.).

### Códigos HTTP Utilizados:

| Código | Significado | Uso |
|--------|-------------|-----|
| 200 | OK | Consultas exitosas (GET) |
| 201 | Created | Creación exitosa (POST) |
| 202 | Accepted | Actualización exitosa (PUT) |
| 403 | Forbidden | Blueprint duplicado |
| 404 | Not Found | Recurso no encontrado |

---

## Filtros de Blueprints

La aplicación incluye tres filtros que se pueden activar mediante perfiles de Spring:

### 1. IdentityFilter (Por defecto)

No modifica los blueprints. Se activa automáticamente por defecto.

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=default"
```

### 2. RedundancyFilter

Elimina puntos consecutivos duplicados para reducir redundancia.

**Activación:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=redundancy"
```

**Ejemplo:**  
- Entrada: `[(0,0), (0,0), (1,1), (1,1)]`  
- Salida: `[(0,0), (1,1)]`

### 3. UndersamplingFilter

Conserva 1 de cada 2 puntos (índices pares) para reducir la densidad.

**Activación:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=undersampling"
```

**Ejemplo:**  
- Entrada: `[(0,0), (1,1), (2,2), (3,3)]`  
- Salida: `[(0,0), (2,2)]`

**Nota:** Los filtros se aplican automáticamente cuando consultas un blueprint específico mediante el endpoint GET.

---

## Pruebas Unitarias

Se crearon pruebas completas para validar la funcionalidad de la aplicación:

### 1. BlueprintsServicesTest

Pruebas para la capa de servicios:

```bash
mvn test -Dtest=BlueprintsServicesTest
```

**Casos de prueba:**
- `testAddNewBlueprint()` - Crear un blueprint correctamente
- `testAddDuplicateBlueprint()` - Validar error al crear duplicado
- `testGetAllBlueprints()` - Obtener todos los blueprints
- `testGetBlueprintsByAuthor()` - Filtrar por autor
- `testGetBlueprintsByAuthorNotFound()` - Error cuando no existen blueprints del autor
- `testGetBlueprint()` - Obtener un blueprint específico
- `testGetBlueprintNotFound()` - Error cuando no existe el blueprint
- `testAddPoint()` - Agregar un punto a un blueprint
- `testAddPointToNonexistentBlueprint()` - Error al agregar punto a blueprint inexistente

### 2. BlueprintsAPIControllerTest

Pruebas para los endpoints HTTP:

```bash
mvn test -Dtest=BlueprintsAPIControllerTest
```

**Casos de prueba:**
- `testGetAllBlueprints()` - GET /api/v1/blueprints con respuesta ApiResponse
- `testCreateBlueprint()` - POST /api/v1/blueprints con código 201
- `testCreateDuplicateBlueprint()` - POST duplicado con código 403
- `testGetBlueprintsByAuthor()` - GET /api/v1/blueprints/{author}
- `testGetBlueprintsByAuthorNotFound()` - GET con autor inexistente (404)
- `testGetSpecificBlueprint()` - GET /api/v1/blueprints/{author}/{bpname}
- `testGetSpecificBlueprintNotFound()` - GET blueprint inexistente (404)
- `testAddPoint()` - PUT /api/v1/blueprints/{author}/{bpname}/points con código 202
- `testAddPointToNonexistentBlueprint()` - PUT a blueprint inexistente (404)

### 3. Ejecutar todas las pruebas

```bash
mvn test
```

**Resultado esperado:**
```
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
```

---

## Cambios Realizados

### Cambio 1: Versionamiento de API

Se cambió el path base del controlador de `/blueprints` a `/api/v1/blueprints` para aplicar versionamiento de APIs REST.

**Archivo modificado:**
- `BlueprintsAPIController.java` - Cambio de `@RequestMapping("/blueprints")` a `@RequestMapping("/api/v1/blueprints")`

**Impacto:** Todos los endpoints ahora están bajo `/api/v1/blueprints`

### Cambio 2: Persistencia en PostgreSQL

Se implementó una capa completa de persistencia usando Spring Data JPA y PostgreSQL:

**Archivos creados:**
- `BlueprintEntity.java` - Entidad JPA para Blueprint
- `PointEntity.java` - Entidad JPA para Point
- `BlueprintEntityRepository.java` - Repositorio JPA
- `PointEntityRepository.java` - Repositorio JPA
- `PostgresBlueprintPersistence.java` - Implementación con JPA

### Cambio 3: Respuesta Uniforme

Se implementó la clase `ApiResponse<T>` para estandarizar todas las respuestas:

**Archivo creado:**
- `ApiResponse.java` - Record genérico para respuestas uniformes

**Beneficios:**
- Consistencia en formato de respuestas
- Mantenimiento simplificado
- Mejor experiencia del cliente

### Cambio 4: Pruebas Unitarias

Se agregaron pruebas completas para servicios y controladores:

**Archivos creados:**
- `BlueprintsServicesTest.java` - 9 pruebas para servicios
- `BlueprintsAPIControllerTest.java` - 9 pruebas para endpoints

---

## Ejecución del Proyecto

### 1. Limpiar y compilar

```bash
cd c:\Users\CAndr\Downloads\Lab4-ARSW
mvn clean install
```

### 2. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

### 3. Acceder a la documentación

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 4. Probar los endpoints

Usa Postman, curl o cualquier cliente HTTP con los bodies y URLs proporcionados anteriormente.

---

## Arquitectura y Patrones Aplicados

### Separación de Capas

- **Model (Dominio):** `Blueprint`, `Point` - Independientes de la persistencia.
- **Entities (Persistencia):** `BlueprintEntity`, `PointEntity` - Mapeo JPA a base de datos.
- **Repositories:** `BlueprintEntityRepository`, `PointEntityRepository` - Acceso a datos.
- **Persistence:** `PostgresBlueprintPersistence` - Implementación de negocio.
- **Services:** `BlueprintsServices` - Orquestación de lógica.
- **Controllers:** `BlueprintsAPIController` - Endpoints REST con respuestas uniformes.

### Buenas Prácticas Aplicadas

- **Inyección de Dependencias** - Spring maneja automáticamente los beans.
- **Separación de Responsabilidades** - Cada clase tiene un propósito específico.
- **Códigos HTTP Correctos** - 200, 201, 202, 403, 404.
- **Respuestas Uniformes** - ApiResponse<T> para toda la API.
- **Persistencia con JPA** - Uso de Hibernate y Spring Data.
- **Configuración Externalizada** - application.properties para bases de datos.
- **Documentación OpenAPI** - Swagger UI habilitado.


---

