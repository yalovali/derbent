# Multi-User Safety - Quick Reference Card

**🎯 Golden Rule:** Services are singletons (ONE instance shared by ALL users). Never store user-specific data in service instance fields.

---

## ✅ SAFE Patterns

### Service Structure
```java
@Service
public class CGoodService extends CAbstractService<CEntity> {
    // ✅ Dependencies only
    private final IEntityRepository repository;
    private final Clock clock;
    private final ISessionService sessionService;
    
    public CGoodService(IEntityRepository repo, Clock clock, ISessionService session) {
        super(repo, clock, session);
        this.repository = repo;
        this.sessionService = session;
    }
}
```

### Getting User Context
```java
// ✅ Retrieve from session each time
public List<CEntity> findAll() {
    CUser user = sessionService.getActiveUser()
        .orElseThrow(() -> new IllegalStateException("No user"));
    return repository.findByUserId(user.getId());
}
```

### Session Storage
```java
// ✅ Store user-specific data in session
public void savePreference(String pref) {
    VaadinSession.getCurrent().setAttribute("pref", pref);
}

public String getPreference() {
    return (String) VaadinSession.getCurrent().getAttribute("pref");
}
```

### Static Constants
```java
// ✅ Constants are safe
private static final String MENU_TITLE = "Activities";
private static final int MAX_RESULTS = 100;
private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
```

---

## ❌ UNSAFE Patterns

### Instance State
```java
@Service
public class CBadService {
    // ❌ WRONG! Shared across ALL users
    private CUser currentUser;
    private List<CEntity> userCache;
    
    public void setUser(CUser user) {
        this.currentUser = user;  // User B overwrites User A!
    }
}
```

### Static Mutable State
```java
@Service
public class CBadService {
    // ❌ WRONG! Shared and not thread-safe
    private static Map<Long, CUser> userCache = new HashMap<>();
    private static List<CEntity> data = new ArrayList<>();
}
```

### Caching Without Isolation
```java
@Service
public class CBadService {
    // ❌ WRONG! Cache shared by all users
    private Map<Long, List<CEntity>> cache = new HashMap<>();
    
    public List<CEntity> getData(Long id) {
        // User B sees User A's cached data!
        return cache.computeIfAbsent(id, 
            k -> repository.findById(k));
    }
}
```

---

## 🔍 Quick Safety Checks

Before committing, verify:

- [ ] No mutable instance fields (except injected dependencies)
- [ ] No static mutable collections
- [ ] All user context from `sessionService` per-method
- [ ] No caching of user-specific data in service
- [ ] Asked: "What if 100 users call this simultaneously?"

---

## 📍 Where to Store State

| State Type | Storage | Example |
|------------|---------|---------|
| User identity | VaadinSession | Current user, company |
| User preferences | VaadinSession | UI settings, filters |
| Temporary UI state | Component fields | Form data |
| Application config | Properties/DB | System settings |
| Business data | Database | Activities, projects |
| Constants | static final | Menu titles, limits |

---

## 🚨 Warning Signs

**If you see users reporting:**
- Seeing other users' data → Instance field storing user data
- Data corruption under load → Thread-safety issue  
- Inconsistent state → State leaking between requests
- Random NullPointerExceptions → Race conditions

**→ Review multi-user advisory immediately!**

---

## 📚 Full Documentation

- **Complete Guide:** `docs/architecture/multi-user-singleton-advisory.md`
- **Checklist:** `docs/development/multi-user-development-checklist.md`
- **Report:** `MULTI_USER_READINESS_REPORT.md`
- **Standards:** `docs/architecture/coding-standards.md`
- **Patterns:** `docs/architecture/service-layer-patterns.md`

---

## 💡 Remember

**Singleton = ONE instance for ALL users**

If you store something in:
- **Instance field** → Visible to ALL users
- **Static field** → Visible to ALL users
- **VaadinSession** → Visible to ONE user ✅
- **Local variable** → Visible to ONE method call ✅
- **Database** → Persistent, proper isolation ✅

---

**When in doubt:** Retrieve from session or database each time!
