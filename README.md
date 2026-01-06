# SupplyChainX

![CI Pipeline](https://github.com/Meriem003/SupplyChainX/workflows/CI%20Pipeline%20-%20SupplyChainX/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-57.5%25-yellow)
![Quality Gate](https://img.shields.io/badge/quality%20gate-passed-brightgreen)
![Tests](https://img.shields.io/badge/tests-151%20passed-success)

> Système de gestion de chaîne d'approvisionnement avec Docker, Tests, et CI/CD

## 🚀 Stack Technique

- **Backend:** Java 17, Spring Boot 3.5.7
- **Sécurité:** Spring Security 6, JWT (Access Token + Refresh Token)
- **Base de données:** MySQL 8.0
- **Observabilité:** ELK Stack (Elasticsearch, Logstash, Kibana)
- **Conteneurisation:** Docker, Docker Compose
- **Tests:** JUnit 5, Mockito, Spring Boot Test (24 tests d'intégration sécurité)
- **Qualité:** JaCoCo, SonarQube
- **CI/CD:** GitHub Actions
- **Documentation:** Swagger/OpenAPI

## 📊 Métriques

- ✅ **Tests:** 151 (100% réussite)
- 📈 **Couverture:** 57.5%
- 🐛 **Bugs:** 0
- 🔒 **Vulnérabilités:** 3 (en cours de correction)
- 📋 **Duplication:** 0%

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

---

## 🔐 Sécurité JWT - Authentification Stateless

### Architecture de Sécurité

L'application utilise une **authentification stateless** basée sur JWT (JSON Web Token) avec un système de **Access Token** et **Refresh Token** pour garantir la sécurité des API REST.

```
┌──────────┐          ┌─────────────────┐          ┌──────────┐
│  Client  │          │  Spring Boot    │          │  MySQL   │
│          │          │                 │          │          │
│          │─Login──▶ │  AuthService    │          │          │
│          │          │       │         │          │          │
│          │◀────────│  JwtUtil        │          │          │
│          │ Tokens   │  (génération)   │          │          │
│          │          │       │         │          │          │
│          │          │       ▼         │          │          │
│          │          │ RefreshToken    │──save──▶ │ refresh_ │
│          │          │   Service       │          │ tokens   │
│          │          └─────────────────┘          └──────────┘
│          │                 │
│          │─API Request────▶│
│          │ + Access Token  │
│          │                 │
│          │                 ▼
│          │          JwtAuthenticationFilter
│          │                 │
│          │                 ▼
│          │          SecurityFilterChain
│          │                 │
│          │◀────────────────┘
│          │   Response
└──────────┘
```

### Composants Spring Security

#### 1. **SecurityConfig**
Configuration centrale de la sécurité :
- Définit les endpoints publics (`/auth/**`, `/health`)
- Configure les autorisations par rôle
- Définit la stratégie de session (STATELESS)
- Configure les gestionnaires d'erreurs (401, 403)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // Configuration stateless sans session
        http.sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
        // Protection des endpoints par rôle
        // Filtres JWT personnalisés
    }
}
```

#### 2. **JwtAuthenticationFilter**
Filtre personnalisé qui intercepte chaque requête HTTP :
- Extrait le JWT du header `Authorization: Bearer {token}`
- Valide le token (signature, expiration)
- Extrait les informations utilisateur (userId, email, role)
- Configure le contexte de sécurité Spring (`SecurityContextHolder`)
- Enrichit les logs avec les informations utilisateur (MDC)

**Flux** :
```
Request → JwtAuthenticationFilter → Validation JWT → SecurityContext → Controller
```

#### 3. **JwtUtil**
Utilitaire de génération et validation des tokens JWT :
- `generateAccessToken(userId, email, role)` → JWT 15 min
- `generateRefreshToken(userId)` → UUID stocké en base
- `isTokenValid(token)` → Vérifie signature + expiration
- `extractUserId(token)`, `extractRole(token)` → Extraction claims

**Structure Access Token** :
```json
{
  "userId": 123,
  "email": "user@example.com",
  "role": "CHEF_PRODUCTION",
  "tokenType": "ACCESS",
  "iat": 1704556800,
  "exp": 1704557700
}
```

#### 4. **RefreshTokenService**
Gestion du cycle de vie des Refresh Tokens :
- `createRefreshToken(user)` → Génère et stocke en base (7 jours)
- `verifyRefreshToken(token)` → Valide et retourne le token
- `revokeRefreshToken(token)` → Révoque le token (logout)
- **Rotation automatique** : ancien token révoqué lors du refresh

#### 5. **AuthService**
Service d'authentification principal :
- `login(email, password)` → Vérifie credentials + génère tokens
- `refreshAccessToken(refreshToken)` → Renouvelle avec rotation
- `logout(refreshToken)` → Révoque le refresh token

#### 6. **PasswordEncoder**
- Utilise **BCryptPasswordEncoder** pour hasher les mots de passe
- Force de hachage par défaut (10 rounds)
- Jamais de mot de passe en clair en base

### Types de Tokens

#### Access Token (15 minutes)
- **Type** : JWT signé avec HMAC-SHA256
- **Contenu** : userId, email, role, expiration
- **Usage** : Authentification sur chaque requête API
- **Stockage** : Côté client (localStorage/sessionStorage)
- **Révocation** : Impossible (attendre expiration)

#### Refresh Token (7 jours)
- **Type** : UUID unique
- **Contenu** : Stocké en base avec user_id, expiry_date, revoked
- **Usage** : Renouvellement de l'Access Token
- **Stockage** : Côté client + base de données
- **Révocation** : Possible (logout, rotation)

### Endpoints d'Authentification

#### POST /auth/login
Authentification avec email + mot de passe.

**Request** :
```json
{
  "email": "admin@supplychainx.com",
  "password": "Admin@2025"
}
```

**Response 200 OK** :
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "admin@supplychainx.com",
  "role": "ADMIN"
}
```

**Erreurs** :
- `401 Unauthorized` : Email ou mot de passe incorrect
- `400 Bad Request` : Données manquantes ou invalides

#### POST /auth/refresh
Renouvellement de l'Access Token (avec rotation du Refresh Token).

**Request** :
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response 200 OK** :
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (nouveau)",
  "refreshToken": "660f9511-f30c-52e5-b827-557766551111 (nouveau)",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "admin@supplychainx.com",
  "role": "ADMIN"
}
```

⚠️ **Important** : L'ancien Refresh Token est **automatiquement révoqué** (rotation).

**Erreurs** :
- `401 Unauthorized` : Token invalide, expiré ou révoqué

#### POST /auth/logout
Déconnexion et révocation du Refresh Token.

**Request** :
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response** : `204 No Content`

**Erreurs** :
- `401 Unauthorized` : Token inexistant

### Autorisation par Rôle

Chaque endpoint est protégé avec des rôles spécifiques :

| Module | Endpoint | Rôles autorisés |
|--------|----------|-----------------|
| **Approvisionnement** | `/api/suppliers/**` | GESTIONNAIRE_APPROVISIONNEMENT, RESPONSABLE_ACHATS, ADMIN |
| | `/api/raw-materials/**` | GESTIONNAIRE_APPROVISIONNEMENT, SUPERVISEUR_LOGISTIQUE, ADMIN |
| | `/api/supply-orders/**` | GESTIONNAIRE_APPROVISIONNEMENT, RESPONSABLE_ACHATS, ADMIN |
| **Production** | `/api/products/**` | CHEF_PRODUCTION, SUPERVISEUR_PRODUCTION, PLANIFICATEUR, ADMIN |
| | `/api/bill-of-materials/**` | CHEF_PRODUCTION, PLANIFICATEUR, ADMIN |
| | `/api/production-orders/**` | CHEF_PRODUCTION, SUPERVISEUR_PRODUCTION, PLANIFICATEUR, ADMIN |
| **Livraison** | `/api/customers/**` | GESTIONNAIRE_COMMERCIAL, ADMIN |
| | `/api/orders/**` | GESTIONNAIRE_COMMERCIAL, RESPONSABLE_LOGISTIQUE, ADMIN |
| | `/api/deliveries/**` | RESPONSABLE_LOGISTIQUE, SUPERVISEUR_LIVRAISONS, ADMIN |

**Exemple de refus d'accès** :
```bash
# Utilisateur avec rôle GESTIONNAIRE_APPROVISIONNEMENT
GET /api/products
→ 403 Forbidden
```

### Guide de Test avec Postman

#### 1. Importer l'environnement

Créer un environnement Postman avec :
- `base_url` = `http://localhost:8080`
- `access_token` = (vide, sera rempli automatiquement)
- `refresh_token` = (vide, sera rempli automatiquement)

#### 2. Tester le Login

**Request** :
```http
POST {{base_url}}/auth/login
Content-Type: application/json

{
  "email": "admin@supplychainx.com",
  "password": "Admin@2025"
}
```

**Script Postman** (onglet Tests) pour sauvegarder les tokens :
```javascript
const response = pm.response.json();
pm.environment.set("access_token", response.accessToken);
pm.environment.set("refresh_token", response.refreshToken);
```

#### 3. Accéder à un endpoint protégé

**Request** :
```http
GET {{base_url}}/api/suppliers
Authorization: Bearer {{access_token}}
```

**Résultat attendu** : `200 OK` avec la liste des fournisseurs.

#### 4. Tester le Refresh Token

**Request** :
```http
POST {{base_url}}/auth/refresh
Content-Type: application/json

{
  "refreshToken": "{{refresh_token}}"
}
```

**Script Postman** (onglet Tests) :
```javascript
const response = pm.response.json();
pm.environment.set("access_token", response.accessToken);
pm.environment.set("refresh_token", response.refreshToken);
```

#### 5. Tester la révocation (Logout)

**Request** :
```http
POST {{base_url}}/auth/logout
Content-Type: application/json

{
  "refreshToken": "{{refresh_token}}"
}
```

**Résultat attendu** : `204 No Content`

Après le logout, toute tentative de refresh avec l'ancien token retournera `401 Unauthorized`.

### Exemples de Requêtes cURL

#### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@supplychainx.com",
    "password": "Admin@2025"
  }'
```

#### Accès endpoint protégé
```bash
curl -X GET http://localhost:8080/api/suppliers \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Refresh Token
```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

#### Logout
```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

### Gestion des Erreurs

#### 401 Unauthorized
Retourné quand :
- Token manquant dans le header
- Token malformé ou invalide
- Token expiré
- Credentials incorrects (login)
- Refresh Token révoqué ou expiré

**Format de réponse** :
```json
{
  "timestamp": "2026-01-06T14:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token expiré",
  "path": "/api/suppliers"
}
```

#### 403 Forbidden
Retourné quand :
- Utilisateur authentifié mais rôle insuffisant
- Tentative d'accès à une ressource non autorisée

**Format de réponse** :
```json
{
  "timestamp": "2026-01-06T14:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/products"
}
```

### Sécurité et Bonnes Pratiques

✅ **Implémenté** :
- API stateless (pas de session serveur)
- Tokens JWT signés avec HMAC-SHA256
- Mots de passe hashés avec BCrypt
- Refresh Token stocké en base avec révocation
- Rotation automatique des Refresh Tokens
- `@JsonIgnore` sur le champ `password` de l'entité User
- Expiration courte des Access Tokens (15 min)
- Logs de sécurité (tentatives login, 401/403) vers ELK

⚠️ **Recommandations production** :
- Utiliser HTTPS en production
- Stocker le secret JWT dans une variable d'environnement
- Implémenter un rate limiting sur `/auth/login`
- Ajouter une blacklist de tokens compromis
- Activer CORS avec origines spécifiques

---

## 📊 Observabilité avec ELK

### Architecture ELK

```
Spring Boot (Logback) → Logstash (TCP:5000) → Elasticsearch → Kibana
```

### Services disponibles

- **Elasticsearch** : http://localhost:9200
- **Kibana** : http://localhost:5601
- **Logstash** : TCP port 5000

### Types de logs

#### 1. Logs applicatifs
Logs généraux de l'application (INFO, WARN, ERROR).

#### 2. Logs de sécurité
Événements d'authentification et autorisation :
- Tentatives de login (succès/échec)
- Tokens expirés ou invalides
- Accès refusés (403)
- Refresh token utilisé/révoqué
- Logout

**Champs** : `user_id`, `user_role`, `endpoint`, `http_status`, `log_type: SECURITY`

#### 3. Logs métier
Actions métier critiques :
- Création d'une commande
- Validation d'un ordre de production
- Expédition d'une livraison

**Champs** : `business_id`, `entity_type`, `action`, `log_type: BUSINESS`

### Recherche dans Kibana

**Logs de sécurité uniquement** :
```
log_type: "SECURITY"
```

**Erreurs 401** :
```
http_status: 401
```

**Actions d'un utilisateur spécifique** :
```
user_id: 123
```

**Traçabilité d'une commande** :
```
business_id: "Order_456"
```

### Configuration

Les logs sont automatiquement envoyés vers ELK via :
- **Logback** : `logback-spring.xml` avec encoder Logstash
- **MDC** : Contexte enrichi (user, endpoint, status)
- **Aspects AOP** : Interception automatique des méthodes de service

---

## 👥 Auteur

**Meriem003**
- GitHub: [@Meriem003](https://github.com/Meriem003)
