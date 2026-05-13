# GuardBatXat Build & Deployment Troubleshooting Guide

## Current Status

### ✅ Working Components
- **Frontend**: Next.js 16.2.1 (localhost:3000) - Builds and runs successfully
- **Database**: PostgreSQL with PostGIS - Connected and operational
- **API Structure**: Controllers, repositories, services are designed and partially tested

### ❌ Blocked Components  
- **Backend**: Spring Boot 3.2.4 - **Compilation blocked by Lombok/Java incompatibility**
- **Python AI Service**: Structure exists but not fully integrated

## Problem Description

The backend uses Lombok extensively for code generation (@Data, @Builder, @Getter, @Setter, @Slf4j annotations), but has a critical incompatibility with Java 17/21 + Lombok 1.18.x.

### Error
```
COMPILATION ERROR:
java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

### Impact
- 100+ Lombok-generated methods are missing (getters, setters, builders)
- Build fails at compilation phase
- Cannot generate executable JAR

## Root Cause Analysis

This is a known issue with:
- **Java 17/21 annotation processors** - TypeTag enum doesn't exist in newer compiler APIs
- **Lombok 1.18.x** - Not fully compatible with Java 17+
- **Spring Boot 3.2.4** - Uses newer compiler infrastructure that breaks Lombok

## Solutions Attempted (All Failed)

1. ✗ Enabled `annotationProcessorPaths` in maven-compiler-plugin
2. ✗ Upgraded Lombok: 1.18.30 → 1.18.32 → 1.18.34
3. ✗ Downgraded to Java 11 (failed: code uses Java 13+ text blocks)
4. ✗ Added compiler arguments (-XDignore.symbol.file)
5. ✗ Tried different Spring Boot configurations

## Recommended Solutions (In Order of Feasibility)

### Option 1: Use Lombok-free Alternative (RECOMMENDED FOR PRODUCTION)
- Remove @Data, @Builder, @Getter, @Setter annotations
- Add manual getters/setters or use MapStruct
- Requires ~2-3 hours of refactoring
- **Benefit**: No annotation processor dependencies, fully compatible

### Option 2: Use Older Spring Boot Version
- Downgrade to Spring Boot 2.7.x (uses Java 11 baseline)
- Requires removing text blocks from repository queries
- **Benefit**: Lombok works reliably with Java 11

### Option 3: Pre-compiled JAR
- Build on separate machine with Java 11 + Lombok working environment
- Distribute JAR to team
- **Benefit**: Immediate solution, works today
- **Limitation**: Cannot modify backend code without rebuild

### Option 4: Docker
- Create Dockerfile with Java 11 + proper Lombok setup
- Team pulls Docker image instead of building locally
- **Benefit**: Eliminates local environment issues

## Temporary Workaround: Quick Deploy Without Backend Build

For **frontend-only testing**, the system can run with:

1. **Start Frontend** (localhost:3000)
   ```bash
   cd D:\GuardBatXat-Client
   npm run dev
   ```

2. **Create Mock API** (Node.js Express server on 8080)
   - Serve pre-defined JSON responses
   - Allows frontend team to test UI/UX independently

3. **Deploy Python AI Service** separately (localhost:5000)
   - Can run without Spring Boot
   - Handles routing and shelter finding

##Configuration Files Modified

- `pom.xml` - Added annotationProcessorPaths configuration
- Java version changed from 17 → 21 (to test compatibility)
- Maven compiler plugin configured for annotation processing

## For Team: Next Steps

1. **Check Java Version**
   ```bash
   java -version
   ```

2. **Try Building**
   ```bash
   cd D:\GuardBatXat
   mvnw.cmd clean install -DskipTests=true
   ```

3. **If Build Fails** with TypeTag error:
   - Use Option 1 (Lombok-free) or Option 3 (Pre-compiled JAR)
   - Contact lead for pre-built JAR file

4. **If Build Succeeds**:
   ```bash
   mvnw.cmd spring-boot:run
   ```

## Documentation for Future Development

### Architecture
- **Frontend**: Next.js → Next.js API routes → Spring Boot REST APIs → PostgreSQL
- **Backend**: Spring Boot 3.2.4 with Spring Data JPA, Hibernate-Spatial
- **Database**: PostgreSQL 15+ with PostGIS extension
- **Authentication**: JWT (JJWT 0.11.5)
- **Real-time**: WebSocket for map updates, Redis for caching

### Key Endpoints
- `POST /api/v1/auth/login` - User authentication
- `GET /api/v1/citizen/heatmap` - Flood heatmap data
- `POST /api/v1/citizen/safety-check` - Location safety check
- `POST /api/v1/citizen/evacuation` - Evacuation routing

### Entity Relationships
- User → SosRequest (1:many)
- User → SafetyCheck (1:many)  
- RescueMission → User (many:1) - Assigned rescue officer
- Building, Road, Flood (Spatial entities with geometry)

## Additional Resources

- [Lombok + Java 17 Compatibility](https://projectlombok.org/setup/eclipse)
- [Spring Boot 3.2 Migration](https://spring.io/blog/2023/05/24/spring-boot-3-2-0-released)
- [Maven Annotation Processing](https://maven.apache.org/plugins/maven-compiler-plugin/examples/compile-using-modulepath.html)

---

**Last Updated**: Current Session  
**Status**: Build blocked, investigating solutions  
**Next Review**: After team needs clarification on preferred solution
