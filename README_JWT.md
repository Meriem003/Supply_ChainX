# 🔐 Authentification JWT - SupplyChainX

## 📋 Vue d'ensemble

SupplyChainX utilise une authentification **JWT (JSON Web Token)** stateless pour sécuriser son API REST. L'implémentation est simple, claire et réutilise les entités existantes du projet.

## 🏗️ Architecture

### Structure des packages

```
com.supplychainx
├── common/                           # Entités partagées
│   ├── entity/User.java             # Entité User principale
│   ├── enums/UserRole.java          # Rôles utilisateur
│   └── repository/UserRepository.java
├── exception/                        # Gestion globale des erreurs
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
└── security/                         # Système JWT
    ├── config/
    │   └── SecurityConfig.java      # Configuration Spring Security
    ├── controller/
    │   └── AuthController.java      # Endpoint /auth/login
    ├── dto/
    │   ├── LoginRequest.java        # Requête de connexion
    │   └── AuthResponse.java        # Réponse avec token
    ├── filter/
    │   └── JwtAuthenticationFilter.java  # Interception des requêtes
    ├── jwt/
    │   └── JwtUtil.java             # Génération/validation des tokens
    └── service/
        └── AuthService.java         # Logique d'authentification
```

### 🎯 Points clés

- ✅ **Pas de duplication** : Utilise `common.entity.User` existant
- ✅ **Stateless** : Aucune session HTTP, JWT uniquement
- ✅ **Simple** : 7 classes au total
- ✅ **BCrypt** : Passwords hashés avec BCryptPasswordEncoder
- ✅ **RESTful** : API REST pure
- ✅ **Spring Security 6** : Dernière version

## 🔄 Flux d'authentification

### 1️⃣ Connexion (Login)

```
Client                    AuthController              AuthService                JwtUtil
  |                             |                          |                        |
  |--POST /auth/login---------->|                          |                        |
  |  {email, password}          |                          |                        |
  |                             |--login()---------------->|                        |
  |                             |                          |--findByEmail()-------->DB
  |                             |                          |<-----------------------|
  |                             |                          |--validatePassword()----|
  |                             |                          |--generateAccessToken()->|
  |                             |                          |                        |--create JWT
  |                             |                          |<-----------------------|
  |                             |<--AuthResponse-----------|                        |
  |<--{accessToken, userId}-----|                          |                        |
```

**Étapes détaillées :**
1. Client envoie `email` + `password` à `POST /auth/login`
2. `AuthService` vérifie les credentials dans la base de données
3. Si valide, `JwtUtil` génère un token JWT contenant :
   - `userId` (idUser)
   - `email`
   - `role` (ADMIN, GESTIONNAIRE_APPROVISIONNEMENT, etc.)
   - Expiration : 1 heure (3600000 ms)
4. Le token est retourné au client dans `AuthResponse`

### 2️⃣ Requêtes authentifiées

```
Client                JwtAuthenticationFilter      JwtUtil      Spring Security
  |                            |                       |               |
  |--GET /api/xxx------------->|                       |               |
  |  Header: Bearer {token}    |                       |               |
  |                            |--extractToken()-------|               |
  |                            |--isTokenValid()------>|               |
  |                            |<----------------------|               |
  |                            |--extractUserInfo()---->|               |
  |                            |<----------------------|               |
  |                            |--setAuthentication()------------------>|
  |                            |                       |               |--authorize
  |<--Response-----------------|                       |               |
```

**Étapes détaillées :**
1. Client envoie le token dans le header : `Authorization: Bearer {token}`
2. `JwtAuthenticationFilter` intercepte la requête
3. Extraction et validation du token JWT
4. Extraction des informations : email, userId, role
5. Création d'un objet `Authentication` dans `SecurityContext`
6. Spring Security autorise ou refuse l'accès selon le rôle

## 🛠️ Configuration

### application.properties

```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-key-here-make-it-very-long-and-secure-for-production
jwt.access-token-expiration=3600000

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/supplychainx
spring.datasource.username=root
spring.datasource.password=your_password
```

### Roles disponibles

| Role | Description |
|------|-------------|
| `ADMIN` | Accès complet à toutes les ressources |
| `GESTIONNAIRE_APPROVISIONNEMENT` | Gestion des approvisionnements |
| `RESPONSABLE_ACHATS` | Gestion des achats |
| `SUPERVISEUR_LOGISTIQUE` | Supervision logistique |
| `CHEF_PRODUCTION` | Chef de production |
| `PLANIFICATEUR` | Planification |
| `SUPERVISEUR_PRODUCTION` | Supervision production |
| `GESTIONNAIRE_COMMERCIAL` | Gestion commerciale |
| `RESPONSABLE_LOGISTIQUE` | Responsable logistique |
| `SUPERVISEUR_LIVRAISONS` | Supervision des livraisons |

## 📡 API Endpoints

### 🔓 Endpoints publics (sans authentification)

#### POST /auth/login
Connexion utilisateur et génération du token JWT.

**Request:**
```json
{
  "email": "admin@supplychainx.com",
  "password": "password123"
}
```

**Response 200 OK:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "admin@supplychainx.com",
  "role": "ADMIN"
}
```

**Errors:**
- **401 Unauthorized** : Email ou mot de passe incorrect
- **400 Bad Request** : Champs manquants ou format invalide

### 🔒 Endpoints protégés

Tous les endpoints `/api/**` nécessitent un token JWT valide dans le header :
```
Authorization: Bearer {votre_token_jwt}
```

#### Contrôle d'accès par rôle

```java
// Dans SecurityConfig.java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/approvisionnement/**")
    .hasAnyRole("GESTIONNAIRE_APPROVISIONNEMENT", "ADMIN")
.anyRequest().authenticated()
```

## 🧪 Tests avec Postman / cURL

### 1. Connexion

**cURL:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@supplychainx.com",
    "password": "password123"
  }'
```

**Postman:**
1. Method: `POST`
2. URL: `http://localhost:8080/auth/login`
3. Body → raw → JSON:
```json
{
  "email": "admin@supplychainx.com",
  "password": "password123"
}
```

### 2. Utiliser le token

**cURL:**
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Postman:**
1. Method: `GET`
2. URL: `http://localhost:8080/api/users`
3. Headers → Add:
   - Key: `Authorization`
   - Value: `Bearer {votre_token}`

## 🔧 Composants détaillés

### SecurityConfig.java
Configure Spring Security avec :
- **CSRF désactivé** (API REST stateless)
- **Session stateless** (pas de session HTTP)
- **Endpoints publics** : `/auth/**`, `/swagger-ui/**`
- **Endpoints protégés** : `/api/**`
- **Filtre JWT** ajouté avant `UsernamePasswordAuthenticationFilter`

### JwtUtil.java
Responsable de :
- ✅ **Génération** de tokens JWT avec claims (userId, email, role)
- ✅ **Validation** de tokens (expiration, signature)
- ✅ **Extraction** des informations (userId, email, role)
- ✅ **Signature HMAC-SHA256** avec clé secrète

### JwtAuthenticationFilter.java
Filtre Spring qui :
1. Intercepte chaque requête HTTP
2. Extrait le token du header `Authorization`
3. Valide le token avec `JwtUtil`
4. Définit l'authentification dans `SecurityContext`
5. Laisse passer la requête ou retourne 401

### AuthService.java
Service métier qui :
- ✅ Valide les credentials (email + password)
- ✅ Vérifie le mot de passe avec `BCryptPasswordEncoder`
- ✅ Génère le token JWT
- ✅ Retourne `AuthResponse` avec toutes les informations

## 🗃️ Structure de la base de données

### Table `users`

| Colonne | Type | Description |
|---------|------|-------------|
| id_user | BIGINT | Primary key (auto-increment) |
| first_name | VARCHAR(255) | Prénom |
| last_name | VARCHAR(255) | Nom |
| email | VARCHAR(255) | Email (unique) |
| password | VARCHAR(255) | Mot de passe BCrypt |
| role | VARCHAR(50) | Role (enum UserRole) |

### Utilisateurs de test

```sql
-- Admin (password: password123)
email: admin@supplychainx.com
role: ADMIN

-- Gestionnaire (password: password123)
email: gestionnaire@supplychainx.com
role: GESTIONNAIRE_APPROVISIONNEMENT

-- Responsable (password: password123)
email: responsable@supplychainx.com
role: RESPONSABLE_ACHATS
```

## 🔐 Sécurité

### ✅ Bonnes pratiques implémentées

1. **BCrypt Password Hashing** : Force 10 par défaut
2. **Tokens signés** : HMAC-SHA256 avec clé secrète
3. **Expiration des tokens** : 1 heure
4. **Stateless** : Pas de session côté serveur
5. **HTTPS recommandé** : En production
6. **Validation des inputs** : `@Valid` sur LoginRequest

### ⚠️ À faire en production

- [ ] Utiliser une clé secrète de 256+ bits (variable d'environnement)
- [ ] Activer HTTPS
- [ ] Implémenter rate limiting sur `/auth/login`
- [ ] Logger les tentatives de connexion échouées
- [ ] Ajouter refresh tokens (optionnel)
- [ ] Blacklist pour tokens révoqués (optionnel)

## 🚀 Démarrage rapide

### 1. Vérifier la configuration

```properties
# application.properties
jwt.secret=CHANGE_THIS_SECRET_KEY_IN_PRODUCTION
jwt.access-token-expiration=3600000
```

### 2. Démarrer l'application

```bash
mvn spring-boot:run
```

### 3. Tester la connexion

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@supplychainx.com","password":"password123"}'
```

### 4. Copier le token et tester un endpoint protégé

```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI"
```

## 📚 Documentation Swagger

Accédez à la documentation interactive :
```
http://localhost:8080/swagger-ui/index.html
```

Pour utiliser JWT dans Swagger :
1. Cliquez sur **Authorize** 🔒
2. Entrez : `Bearer {votre_token}`
3. Cliquez sur **Authorize**

Tous vos appels API seront maintenant authentifiés.

## 🐛 Dépannage

### Erreur 401 Unauthorized

**Causes possibles :**
- Token expiré (> 1 heure)
- Token invalide ou mal formé
- Header `Authorization` manquant
- Format incorrect : doit être `Bearer {token}`

**Solution :**
```bash
# Reconnectez-vous pour obtenir un nouveau token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@supplychainx.com","password":"password123"}'
```

### Erreur 403 Forbidden

**Cause :** Rôle insuffisant pour accéder à la ressource

**Solution :** Utilisez un compte avec les permissions nécessaires (ex: ADMIN)

### Erreur 400 Bad Request

**Cause :** Email ou password manquant/invalide

**Solution :** Vérifiez le format JSON et les champs requis

## 🔄 Différences avec l'implémentation précédente

### ✅ Améliorations

| Avant | Après |
|-------|-------|
| 22 classes | 7 classes (-68%) |
| Entités dupliquées | Réutilisation de `common.entity.User` |
| Refresh tokens | Simplifié : Access tokens uniquement |
| Token 15 min | Token 1 heure |
| 3 repositories | 1 repository (common) |
| Exception handlers dupliqués | GlobalExceptionHandler global |

### 🎯 Résultat

- **Code plus simple** et plus maintenable
- **Pas de duplication** de code
- **Architecture cohérente** avec le reste du projet
- **Plus facile à comprendre** pour l'apprentissage

## 📝 Notes importantes

1. **L'entité User est partagée** : Le package `security` utilise `common.entity.User` existant
2. **Pas de champ `enabled`** : L'entité User n'a pas de flag d'activation
3. **Tous les users en base peuvent se connecter** : Pas de désactivation de compte
4. **Token expiration : 1 heure** : Après 1h, le client doit se reconnecter

## 🎓 Pour aller plus loin

### Fonctionnalités avancées (optionnelles)

1. **Refresh Tokens** : Renouveler l'accès sans redemander le password
2. **Remember Me** : Tokens longue durée
3. **Two-Factor Authentication (2FA)** : Double authentification
4. **OAuth2** : Connexion Google, Facebook, etc.
5. **Blacklist tokens** : Révocation de tokens avant expiration

---

**Documentation générée pour SupplyChainX - Authentification JWT**  
Version : 1.0  
Date : Décembre 2025
