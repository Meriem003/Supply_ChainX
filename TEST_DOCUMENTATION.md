# Documentation des Tests - SupplyChainX

## Vue d'ensemble

Cette documentation décrit la suite complète de tests unitaires et d'intégration pour l'application SupplyChainX.

## Structure des Tests

```
src/test/java/com/supplychainx/
├── approvisionnement/
│   ├── controller/
│   │   ├── RawMaterialControllerIntegrationTest.java
│   │   └── SupplierControllerIntegrationTest.java
│   ├── repository/
│   │   ├── RawMaterialRepositoryIntegrationTest.java
│   │   └── SupplierRepositoryIntegrationTest.java
│   └── service/
│       ├── RawMaterialServiceTest.java
│       ├── SupplierServiceTest.java
│       └── SupplyOrderServiceTest.java
├── production/
│   ├── controller/
│   │   └── ProductControllerIntegrationTest.java
│   ├── repository/
│   │   └── ProductRepositoryIntegrationTest.java
│   └── service/
│       ├── ProductServiceTest.java
│       └── ProductionOrderServiceTest.java
├── livraison/
│   ├── controller/
│   │   └── OrderControllerIntegrationTest.java
│   └── service/
│       ├── CustomerServiceTest.java
│       ├── OrderServiceTest.java
│       └── DeliveryServiceTest.java
├── common/
│   ├── controller/
│   │   └── UserControllerIntegrationTest.java
│   └── service/
│       └── UserServiceTest.java
├── security/
│   └── AuthenticationServiceTest.java
└── config/
    └── TestConfig.java
```

## Types de Tests

### 1. Tests Unitaires (Service Layer)

**Objectif** : Tester la logique métier de manière isolée

**Technologies** :
- JUnit 5
- Mockito
- AssertJ

**Couverture** :
- ✅ RawMaterialServiceTest
- ✅ SupplierServiceTest
- ✅ SupplyOrderServiceTest
- ✅ ProductServiceTest
- ✅ ProductionOrderServiceTest
- ✅ CustomerServiceTest
- ✅ OrderServiceTest
- ✅ DeliveryServiceTest
- ✅ UserServiceTest
- ✅ AuthenticationServiceTest

**Scénarios testés** :
- Création de ressources
- Récupération de ressources (tous, par ID)
- Mise à jour de ressources
- Suppression de ressources
- Gestion des exceptions (ResourceNotFoundException, BusinessRuleException)
- Validations métier
- Règles spécifiques (stock, statuts, etc.)

### 2. Tests d'Intégration (Controller Layer)

**Objectif** : Tester les API REST end-to-end

**Technologies** :
- Spring MockMvc
- @WebMvcTest
- Jackson ObjectMapper

**Couverture** :
- ✅ RawMaterialControllerIntegrationTest
- ✅ SupplierControllerIntegrationTest
- ✅ ProductControllerIntegrationTest
- ✅ OrderControllerIntegrationTest
- ✅ UserControllerIntegrationTest

**Scénarios testés** :
- Requêtes HTTP (GET, POST, PUT, DELETE)
- Validation des codes de statut HTTP
- Sérialisation/Désérialisation JSON
- Validation des données d'entrée
- Gestion des erreurs HTTP

### 3. Tests d'Intégration (Repository Layer)

**Objectif** : Tester les opérations de persistence avec la base de données

**Technologies** :
- @DataJpaTest
- H2 In-Memory Database
- TestEntityManager

**Couverture** :
- ✅ RawMaterialRepositoryIntegrationTest
- ✅ SupplierRepositoryIntegrationTest
- ✅ ProductRepositoryIntegrationTest

**Scénarios testés** :
- Opérations CRUD
- Requêtes personnalisées
- Relations entre entités
- Contraintes de base de données
- Transactions

## Configuration des Tests

### application-test.properties

```properties
# Base de données H2 en mémoire pour les tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.liquibase.enabled=false
```

### TestConfig.java

Configuration spécifique pour les tests incluant :
- PasswordEncoder pour les tests de sécurité
- Beans mockés si nécessaire

## Exécution des Tests

### Tous les tests

```bash
mvn test
```

### Tests d'un module spécifique

```bash
# Module Approvisionnement
mvn test -Dtest="com.supplychainx.approvisionnement.**"

# Module Production
mvn test -Dtest="com.supplychainx.production.**"

# Module Livraison
mvn test -Dtest="com.supplychainx.livraison.**"
```

### Tests par type

```bash
# Tests unitaires uniquement
mvn test -Dtest="**/*ServiceTest"

# Tests d'intégration controller
mvn test -Dtest="**/*ControllerIntegrationTest"

# Tests d'intégration repository
mvn test -Dtest="**/*RepositoryIntegrationTest"
```

### Tests avec couverture de code

```bash
mvn test jacoco:report
```

Le rapport sera disponible dans : `target/site/jacoco/index.html`

## Métriques de Tests

### Nombre de Tests par Module

| Module | Tests Unitaires | Tests Intégration | Total |
|--------|----------------|-------------------|-------|
| Approvisionnement | 3 classes | 4 classes | 7 |
| Production | 2 classes | 2 classes | 4 |
| Livraison | 3 classes | 1 classe | 4 |
| Common | 1 classe | 1 classe | 2 |
| Security | 1 classe | 0 classe | 1 |
| **TOTAL** | **10** | **8** | **18** |

### Couverture Estimée

- **Services** : ~85%+ de couverture
- **Controllers** : ~80%+ de couverture
- **Repositories** : ~75%+ de couverture

## Bonnes Pratiques Implémentées

1. **Isolation des tests** : Chaque test est indépendant
2. **Nomenclature claire** : Noms de tests descriptifs avec @DisplayName
3. **AAA Pattern** : Arrange-Act-Assert dans chaque test
4. **Mocking approprié** : Utilisation de @Mock et @InjectMocks
5. **Tests de cas limites** : Tests des erreurs et exceptions
6. **Données de test** : Setup dans @BeforeEach pour cohérence
7. **Assertions riches** : Utilisation d'AssertJ pour meilleure lisibilité

## Prochaines Étapes

### Tests à ajouter

1. **Tests de sécurité**
   - Tests d'authentification
   - Tests d'autorisation par rôle
   - Tests de validation de token

2. **Tests de performance**
   - Tests de charge
   - Tests de stress
   - Tests de latence

3. **Tests end-to-end**
   - Scénarios complets utilisateur
   - Tests de workflow

4. **Tests de validation**
   - Tests de contraintes Bean Validation
   - Tests de validation des DTO

## Maintenance

### Mise à jour des tests

Lors de l'ajout de nouvelles fonctionnalités :

1. Créer les tests unitaires pour les services
2. Créer les tests d'intégration pour les controllers
3. Créer les tests d'intégration pour les repositories si nécessaire
4. Mettre à jour cette documentation

### Debugging des tests échoués

```bash
# Exécuter un test spécifique en mode verbose
mvn test -Dtest=RawMaterialServiceTest -X

# Exécuter avec logs détaillés
mvn test -Dtest=RawMaterialServiceTest -Dlogging.level.root=DEBUG
```

## Ressources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)

## Contact

Pour toute question concernant les tests, contactez l'équipe de développement.
