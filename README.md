# SupplyChainX

![CI Pipeline](https://github.com/Meriem003/SupplyChainX/workflows/CI%20Pipeline%20-%20SupplyChainX/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-57.5%25-yellow)
![Quality Gate](https://img.shields.io/badge/quality%20gate-passed-brightgreen)
![Tests](https://img.shields.io/badge/tests-151%20passed-success)

> Système de gestion de chaîne d'approvisionnement avec Docker, Tests, et CI/CD

## 🚀 Stack Technique

- **Backend:** Java 17, Spring Boot 3.5.7
- **Base de données:** MySQL 8.0
- **Conteneurisation:** Docker, Docker Compose
- **Tests:** JUnit 5, Mockito, Spring Boot Test
- **Qualité:** JaCoCo, SonarQube
- **CI/CD:** GitHub Actions
- **Documentation:** Swagger/OpenAPI

## 📊 Métriques

- ✅ **Tests:** 151 (100% réussite)
- 📈 **Couverture:** 57.5%
- 🐛 **Bugs:** 0
- 🔒 **Vulnérabilités:** 3 (en cours de correction)
- 📋 **Duplication:** 0%
- ⚡ **Build Time:** ~2min 20s

## 🏗️ Architecture
```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Client    │────▶│  Spring Boot │────▶│    MySQL    │
└─────────────┘     └──────────────┘      └─────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  SonarQube   │
                    └──────────────┘
```

## 🚀 Démarrage Rapide
```bash
# Cloner le projet
git clone https://github.com/Meriem003/SupplyChainX.git
cd SupplyChainX

# Lancer avec Docker Compose
docker-compose up -d

# Accéder à l'application
http://localhost:8080

# Accéder à phpMyAdmin
http://localhost:8081

# Accéder à SonarQube
http://localhost:9000
```

## 🧪 Exécuter les Tests
```bash
# Tous les tests
mvn test

# Avec rapport de couverture
mvn clean verify jacoco:report

# Analyse SonarQube
mvn sonar:sonar
```

## 📦 Modules

### Approvisionnement
- Gestion des fournisseurs
- Gestion des matières premières
- Commandes d'approvisionnement

### Production
- Gestion des produits finis
- Ordres de production
- Bill of Materials (BOM)

### Livraison & Distribution
- Gestion des clients
- Commandes clients
- Suivi des livraisons

## 👥 Auteur

**Meriem003**
- GitHub: [@Meriem003](https://github.com/Meriem003)