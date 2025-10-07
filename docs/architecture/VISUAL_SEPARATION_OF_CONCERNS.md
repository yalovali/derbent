# Visual Guide: Separation of Concerns in Derbent

## Correct Architecture - Current Implementation ✅

```
┌─────────────────────────────────────────────────────────────────┐
│                        VIEW LAYER (UI)                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  CProjectView, CActivityView, CUserView                  │  │
│  │  - Display UI components                                 │  │
│  │  - Handle user interactions                              │  │
│  │  - MAY access ISessionService for UI state              │  │
│  └────────────────┬─────────────────────────────────────────┘  │
└───────────────────┼─────────────────────────────────────────────┘
                    │ Calls business methods
                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SERVICE LAYER (Business Logic)                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  CProjectService, CActivityService, CUserService         │  │
│  │                                                           │  │
│  │  Constructor Injection:                                  │  │
│  │  public CProjectService(                                 │  │
│  │      IProjectRepository repository,                      │  │
│  │      Clock clock,                                        │  │
│  │      ISessionService sessionService,  ← ✅ CORRECT      │  │
│  │      ApplicationEventPublisher eventPublisher            │  │
│  │  ) { ... }                                               │  │
│  │                                                           │  │
│  │  Usage in methods:                                       │  │
│  │  - getCurrentCompany() via sessionService                │  │
│  │  - getActiveUser() via sessionService                    │  │
│  │  - getActiveProject() via sessionService                 │  │
│  └────────────────┬─────────────────────────────────────────┘  │
└───────────────────┼─────────────────────────────────────────────┘
                    │ Uses for data access
                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                 REPOSITORY LAYER (Data Access)                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  IProjectRepository, IActivityRepository                 │  │
│  │  - No ISessionService dependency ✅                      │  │
│  │  - Pure JPA repositories                                 │  │
│  │  - Filtered data queries                                 │  │
│  └────────────────┬─────────────────────────────────────────┘  │
└───────────────────┼─────────────────────────────────────────────┘
                    │ Returns
                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER (Entities)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  CProject, CActivity, CUser                              │  │
│  │                                                           │  │
│  │  Constructor:                                            │  │
│  │  public CProject(String name) {                          │  │
│  │      super(CProject.class, name);                        │  │
│  │  }                                                        │  │
│  │                                                           │  │
│  │  - NO ISessionService parameter ✅                       │  │
│  │  - NO session field ✅                                   │  │
│  │  - Pure domain objects ✅                                │  │
│  │  - JPA/Hibernate compatible ✅                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

KEY PRINCIPLE:
╔═══════════════════════════════════════════════════════════════╗
║  Services CAN have ISessionService (they need context)        ║
║  Entities MUST NOT have ISessionService (pure domain)         ║
╚═══════════════════════════════════════════════════════════════╝
```

## Wrong Architecture - Anti-Pattern ❌

```
┌─────────────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER (Entities)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  ❌ WRONG APPROACH - DO NOT DO THIS                      │  │
│  │                                                           │  │
│  │  public CProject(String name,                            │  │
│  │                  ISessionService sessionService) {       │  │
│  │      this.name = name;                                   │  │
│  │      this.sessionService = sessionService;               │  │
│  │      this.company = sessionService.getCurrentCompany();  │  │
│  │  }                                                        │  │
│  │                                                           │  │
│  │  PROBLEMS:                                               │  │
│  │  ❌ Entities become tied to application context          │  │
│  │  ❌ Breaks JPA serialization                             │  │
│  │  ❌ Can't test entities without Spring                   │  │
│  │  ❌ Can't use in batch jobs                              │  │
│  │  ❌ Violates Single Responsibility Principle             │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Dependency Flow Diagram

```
┌────────────────┐
│  ISessionService│◄───────────────────┐
│   (Interface)   │                    │
└────────────────┘                    │
        ▲                              │
        │                              │
        │ implements                   │ injects
        │                              │
┌────────────────┐              ┌──────────────┐
│CWebSessionService│            │  Services    │
│(for production) │            │              │
└────────────────┘              │ CProjectSvc  │
                                 │ CActivitySvc │
┌────────────────┐              │ CUserSvc     │
│CMockSessionSvc │◄─────────────┤              │
│  (for tests)   │              └──────────────┘
└────────────────┘                      │
                                        │ uses
                                        ▼
                                 ┌──────────────┐
                                 │ Repositories │
                                 └──────────────┘
                                        │
                                        │ returns
                                        ▼
                                 ┌──────────────┐
                                 │   Entities   │
                                 │              │
                                 │  NO session! │
                                 └──────────────┘
```

## Real-World Analogy: Library System

```
┌─────────────────────────────────────────────────────────┐
│                    LIBRARIAN (Service)                   │
│                                                          │
│  Needs to know:                                         │
│  ✅ Who is borrowing? (Current User)                   │
│  ✅ Which library branch? (Current Company)            │
│  ✅ Library card status? (User permissions)            │
│                                                          │
│  Gets this info from: Library Management System         │
│                       (ISessionService)                 │
└─────────────────────────────────────────────────────────┘
                            │
                            │ borrows
                            ▼
┌─────────────────────────────────────────────────────────┐
│                      BOOK (Entity)                       │
│                                                          │
│  Properties:                                            │
│  ✅ Title                                               │
│  ✅ Author                                              │
│  ✅ ISBN                                                │
│  ✅ Publication Year                                    │
│                                                          │
│  Does NOT need to know:                                 │
│  ❌ Who is borrowing it                                │
│  ❌ Which library it's in                              │
│  ❌ Current user's permissions                         │
│                                                          │
│  The book is just a book!                              │
└─────────────────────────────────────────────────────────┘

Why is this separation important?

1. The BOOK can be moved between libraries
2. The BOOK can be catalogued without a borrower
3. The BOOK can be stored in the database independently
4. The BOOK doesn't change based on who's holding it

Same with our entities!
```

## Comparison: Repository Pattern

```
┌─────────────────────────────────────────────────────────┐
│              Is this a violation of SoC?                 │
│                                                          │
│  public CProjectService(                                │
│      IProjectRepository repository  ← Data Access       │
│  ) { ... }                                              │
│                                                          │
│  Answer: NO! ✅                                         │
│  Services need data access, so they inject repositories.│
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│              Is this a violation of SoC?                 │
│                                                          │
│  public CProjectService(                                │
│      ISessionService sessionService  ← Context Access   │
│  ) { ... }                                              │
│                                                          │
│  Answer: NO! ✅                                         │
│  Services need context, so they inject session service. │
└─────────────────────────────────────────────────────────┘

It's the SAME PATTERN!
Both are legitimate dependencies of the service layer.
```

## Testing Example

```java
┌─────────────────────────────────────────────────────────┐
│                 Testing with Mock Session                │
└─────────────────────────────────────────────────────────┘

@Test
void testProjectFilteredByCompany() {
    // Arrange: Create mocks
    IProjectRepository mockRepo = mock(IProjectRepository.class);
    ISessionService mockSession = mock(ISessionService.class);
    Clock mockClock = Clock.systemDefaultZone();
    
    // Setup mock behavior
    CCompany testCompany = new CCompany("Acme Corp");
    when(mockSession.getCurrentCompany()).thenReturn(testCompany);
    
    // Create service with mocked dependencies
    CProjectService service = new CProjectService(
        mockRepo, 
        mockClock, 
        mockSession,    ← Easy to mock!
        mockEventPublisher
    );
    
    // Act
    service.findAll();
    
    // Assert: Verify correct company filter
    verify(mockRepo).findByCompanyId(testCompany.getId());
}

✅ This is only possible BECAUSE ISessionService is injected!
```

## Summary: The "Concern" in Separation of Concerns

```
┌──────────────────────────────────────────────────────────────┐
│                   What are the "Concerns"?                    │
└──────────────────────────────────────────────────────────────┘

Concern 1: DOMAIN MODELING
├─ Handled by: Entities (CProject, CActivity, CUser)
└─ Responsibility: Represent business concepts

Concern 2: DATA PERSISTENCE  
├─ Handled by: Repositories (IProjectRepository, etc.)
└─ Responsibility: Store and retrieve data

Concern 3: BUSINESS LOGIC
├─ Handled by: Services (CProjectService, etc.)
├─ Needs: Context (session), Data (repository), Time (clock)
└─ Responsibility: Implement workflows and rules

Concern 4: USER CONTEXT
├─ Handled by: Session Service (ISessionService)
└─ Responsibility: Track current user, project, company

Concern 5: USER INTERFACE
├─ Handled by: Views (CProjectView, etc.)
└─ Responsibility: Display and interact with user

Each concern is SEPARATED into its own layer!
Services using ISessionService does NOT violate this separation.
It's exactly what the service layer SHOULD do!
```

## Final Verdict

```
╔═══════════════════════════════════════════════════════════════╗
║                      FINAL ANSWER                             ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  Question: Does ISessionService in service constructors      ║
║            violate separation of concerns?                   ║
║                                                               ║
║  Answer:   NO ✅                                             ║
║                                                               ║
║  The current architecture is CORRECT and follows:            ║
║  ✅ Spring Framework best practices                          ║
║  ✅ Dependency Injection principles                          ║
║  ✅ Layered architecture patterns                            ║
║  ✅ Domain-Driven Design principles                          ║
║  ✅ SOLID principles                                         ║
║                                                               ║
║  NO CHANGES NEEDED                                           ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```
