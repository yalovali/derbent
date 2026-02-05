# 📊 Graphviz Documentation Visual Guide

## What You'll See After Running Documentation Generation

This guide shows what to expect from the generated Graphviz/Doxygen documentation.

---

## 🏠 Main Page (index.html)

**What it shows:**
- Project name and version
- Project brief description
- Quick navigation links
- Module overview

**How to access:**
```bash
./scripts/generate-graphviz-docs.sh --open
# Or manually: xdg-open docs/graphviz-output/html/index.html
```

**Key sections:**
- Classes (list, hierarchy, alphabetical)
- Files (list, directory structure)
- Search functionality (top-right)

---

## 📚 Class List Page (annotated.html)

**What it shows:**
- All documented classes in alphabetical order
- Brief description of each class
- Quick links to class details

**Example classes you'll see:**
```
CActivity - Activity entity for project tasks
CActivityService - Service layer for activity management
CActivityView - Vaadin view for activity management
CAbstractService<T> - Base service class
CEntityDB<T> - Base database entity class
CProject - Project entity
CUser - User entity
... and 380+ more classes
```

**Navigation:**
Click "Classes" → "Class List" in top menu

---

## 🌳 Class Hierarchy Page (hierarchy.html)

**What it shows:**
Visual tree of class inheritance relationships

**Example hierarchy:**
```
CEntityDB<T>
├── CEntity<T>
│   ├── CUser
│   └── CCompany
└── CEntityOfProject<T>
    ├── CActivity
    ├── CProject
    ├── CMeeting
    └── CDecision

CAbstractService<T>
├── CActivityService
├── CProjectService
├── CUserService
└── CMeetingService

CAbstractEntityDBPage<T>
├── CActivityView
├── CProjectView
└── CUserView
```

**Navigation:**
Click "Classes" → "Class Hierarchy" in top menu

---

## 🔍 Individual Class Page

**What each class page shows:**

### 1. Class Overview
```
CActivity Class Reference

Activity entity representing a project task with status tracking,
time estimation, and user assignment.

Inheritance diagram:
[Visual diagram showing: CEntityDB → CEntityOfProject → CActivity]

Collaboration diagram:
[Visual diagram showing relationships with CProject, CUser, CStatus, CWorkflow]
```

### 2. Public Members Section
```
Public Member Functions
- save() : CActivity
  Saves the activity to database with validation
  
- delete() : void
  Deletes the activity if not referenced
  
- setStatus(CStatus status) : void
  Updates activity status and triggers workflow
  
- assignUser(CUser user) : void
  Assigns user to activity
```

### 3. Call Graph (if enabled)
```
[Visual diagram showing method call relationships]

save() is called by:
- CActivityService.saveActivity()
- CActivityView.handleSave()
- CWorkflowService.processTransition()
```

### 4. Caller Graph (if enabled)
```
[Visual diagram showing what this method calls]

save() calls:
- validate()
- updateTimestamp()
- repository.save()
- workflowService.checkTransition()
```

### 5. Source Code
```java
// Syntax-highlighted source code with line numbers
public class CActivity extends CEntityOfProject<CActivity> {
    private String name;
    private CStatus status;
    // ... more code
}
```

---

## 📁 File List Page (files.html)

**What it shows:**
All source files organized by package

**Example structure:**
```
src/main/java/tech/derbent/
├── app/
│   ├── activities/
│   │   ├── domain/
│   │   │   └── CActivity.java
│   │   ├── service/
│   │   │   └── CActivityService.java
│   │   └── view/
│   │       └── CActivityView.java
│   ├── projects/
│   ├── meetings/
│   └── users/
├── api/
│   ├── services/
│   ├── domains/
│   └── interfaces/
└── base/
```

**Navigation:**
Click "Files" → "File List" in top menu

---

## 🗂️ Directory Graph

**What it shows:**
Visual representation of directory dependencies

**Example:**
```
[Box: activities/domain] ──depends on──> [Box: api/domains]
         │
         │ uses
         ↓
[Box: activities/service] ──depends on──> [Box: api/services]
         │
         │ injects
         ↓
[Box: activities/view] ──depends on──> [Box: api/ui]
```

**Features:**
- Color-coded by directory type
- Shows dependency relationships
- Helps understand module coupling

---

## 🔍 Search Functionality

**How it works:**
1. Type in search box (top-right corner)
2. See instant suggestions
3. Click to navigate to result

**What you can search for:**
- Class names: `CActivity`, `CUser`, `CProject`
- Method names: `save`, `delete`, `validate`
- Package names: `activities`, `projects`, `users`
- Keywords: `workflow`, `status`, `entity`

**Pro tip:**
Press `/` key to focus search box

---

## 📊 Diagram Types Explained

### 1. Inheritance Diagram (Class Diagram)
```
Shows "is-a" relationships

     ┌─────────────┐
     │  CEntityDB  │
     └──────┬──────┘
            │
     ┌──────▼──────────┐
     │ CEntityOfProject│
     └──────┬──────────┘
            │
     ┌──────▼──────┐
     │  CActivity  │
     └─────────────┘
```

**Indicates:** CActivity extends CEntityOfProject which extends CEntityDB

### 2. Collaboration Diagram
```
Shows "uses" relationships

┌─────────────┐     ┌──────────┐
│  CActivity  │────>│ CStatus  │
└──────┬──────┘     └──────────┘
       │
       │ contains
       │
       ▼
┌─────────────┐     ┌──────────┐
│  CProject   │<────│  CUser   │
└─────────────┘     └──────────┘
```

**Indicates:** CActivity uses CStatus, belongs to CProject, assigned to CUser

### 3. Call Graph
```
Shows method call flow (forward)

handleSave()
    │
    ├──> validate()
    │
    ├──> save()
    │     │
    │     └──> repository.save()
    │
    └──> showSuccessMessage()
```

**Indicates:** What methods are called from this method

### 4. Caller Graph
```
Shows method call flow (backward)

                save()
                  ▲
                  │
        ┌─────────┼─────────┐
        │         │         │
  saveActivity() │   updateActivity()
                  │
            processWorkflow()
```

**Indicates:** What methods call this method

---

## 🎨 Color Coding

### In Diagrams:
- **Blue boxes**: Classes
- **Green boxes**: Interfaces
- **Yellow boxes**: Abstract classes
- **Red boxes**: Deprecated classes
- **Solid arrows**: Direct relationships
- **Dashed arrows**: Indirect/weak relationships

### In Navigation:
- **Bold text**: Current page
- **Blue links**: Clickable navigation
- **Gray text**: Package/namespace names

---

## 💡 Tips for Navigation

### Quick Access Shortcuts:
1. **Homepage**: Click project name (top-left)
2. **Search**: Press `/` key
3. **Back**: Browser back button
4. **Related Classes**: Click diagram nodes
5. **Source Code**: "Go to source" link on class pages

### Best Practices:
1. **Start with Class Hierarchy** to understand architecture
2. **Use Search** for specific classes or methods
3. **Follow Diagrams** to understand relationships
4. **Read Source Browser** for implementation details
5. **Check Call Graphs** for method dependencies

### Bookmarking Strategy:
Create bookmarks for frequently used pages:
- Main index
- Class hierarchy
- Your module's classes (e.g., activities, projects)
- Abstract base classes (CEntityDB, CAbstractService)

---

## 📈 Example Exploration Workflow

### Scenario: Understanding How Activities Work

**Step 1: Find the Activity Class**
```
1. Open: docs/graphviz-output/html/index.html
2. Click: "Classes" → "Class List"
3. Search: "CActivity"
4. Click: CActivity link
```

**Step 2: Understand Structure**
```
1. View inheritance diagram (see CEntityOfProject parent)
2. View collaboration diagram (see CProject, CUser relationships)
3. Read class description
4. Scan public members list
```

**Step 3: Understand Behavior**
```
1. Find save() method
2. View caller graph (who calls save?)
3. View call graph (what does save() call?)
4. Click through to related classes
```

**Step 4: Review Implementation**
```
1. Scroll to source code section
2. Read implementation
3. Click on type names to jump to definitions
4. Follow cross-references
```

---

## 🎯 What Makes This Different from JavaDoc

### Graphviz/Doxygen Advantages:
- ✅ **Visual diagrams** (JavaDoc has none)
- ✅ **Call graphs** show actual method flows
- ✅ **Collaboration diagrams** show relationships
- ✅ **Directory graphs** show module organization
- ✅ **UML-style notation** for clarity
- ✅ **Interactive navigation** between diagrams
- ✅ **Complete source browser** with cross-refs

### JavaDoc Advantages:
- ✅ **Standard Java tool** (built-in)
- ✅ **IDE integration** (Ctrl+Click)
- ✅ **Faster generation** (no graphs)
- ✅ **Simpler setup** (no Graphviz needed)

### Best Practice: Use Both!
- **JavaDoc**: For IDE quick reference
- **Doxygen**: For architecture understanding and documentation

---

## 📊 File Size Expectations

Typical documentation size for Derbent:

```
docs/graphviz-output/
├── html/                   ~50-150 MB
│   ├── *.html             ~15-30 MB
│   ├── *.png              ~30-80 MB (diagrams)
│   ├── *.svg              ~5-20 MB (if enabled)
│   └── search/            ~5-20 MB
└── doxygen-warnings.log   ~1-5 MB
```

**Factors affecting size:**
- Call graphs ON: +50-100 MB
- Interactive SVG ON: +20-50 MB
- Source code inclusion: +10-30 MB

---

## 🚀 Getting Started Checklist

After generating documentation, explore in this order:

- [ ] 1. Open main page (`index.html`)
- [ ] 2. Check class hierarchy to understand architecture
- [ ] 3. Search for a class you know (e.g., `CActivity`)
- [ ] 4. Explore one class page completely
- [ ] 5. Try clicking on diagram nodes
- [ ] 6. Use search to find specific methods
- [ ] 7. View directory graphs to see module structure
- [ ] 8. Browse source code with syntax highlighting
- [ ] 9. Bookmark pages you use frequently
- [ ] 10. Share documentation URL with team

---

## 📚 Related Documentation

- **Setup**: [GRAPHVIZ_DOCUMENTATION_GUIDE.md](GRAPHVIZ_DOCUMENTATION_GUIDE.md) - Complete installation and configuration
- **Examples**: [GRAPHVIZ_QUICK_START_EXAMPLES.md](GRAPHVIZ_QUICK_START_EXAMPLES.md) - Common use cases and workflows
- **Scripts**: [scripts/README.md](../scripts/README.md) - Automation script reference

---

## 🎓 Summary

The Graphviz/Doxygen documentation provides:

✅ **Visual Architecture Understanding** via diagrams  
✅ **Complete API Reference** with cross-links  
✅ **Call Flow Analysis** with call/caller graphs  
✅ **Interactive Navigation** through code relationships  
✅ **Source Code Browser** with syntax highlighting  
✅ **Powerful Search** across all documentation  

**Best For:**
- Understanding large codebases
- Onboarding new developers
- Architecture reviews
- Refactoring planning
- API documentation

**Generate Now:**
```bash
sudo apt-get install doxygen graphviz
./scripts/generate-graphviz-docs.sh --open
```

Happy exploring! 🚀
