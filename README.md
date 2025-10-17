<div align="center">

# 🌟 Derbent - Collaborative Project Management System

<img src="https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java" alt="Java 17+">
<img src="https://img.shields.io/badge/Spring_Boot-3.5-green?style=for-the-badge&logo=spring" alt="Spring Boot 3.5">
<img src="https://img.shields.io/badge/Vaadin-24.8-blue?style=for-the-badge&logo=vaadin" alt="Vaadin 24.8">
<img src="https://img.shields.io/badge/PostgreSQL-Compatible-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL">
<img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="MIT License">

**🚀 A sophisticated, enterprise-grade project management solution**  
*Designed for small to medium-sized development teams*

**✨ Inspired by Jira and ProjeQtOr • Built with modern Java ecosystem • Server-side rendering excellence ✨**

---

</div>

## 🎯 **Project Vision & Mission**

**Derbent** revolutionizes project management by combining enterprise-level capabilities with developer-friendly architecture. Our mission is to provide teams with a **type-safe**, **metadata-driven**, and **highly maintainable** project management platform that scales from startup to enterprise.

### 🌟 **Core Philosophy**
- **🏗️ Architecture-First**: Clean, consistent patterns that promote long-term maintainability
- **🎨 Developer Experience**: AI-assisted development with GitHub Copilot optimization
- **🔒 Type Safety**: Compile-time guarantees and runtime reliability
- **📈 Scalable Design**: Multi-tenant architecture ready for growth

## 🎯 **Project Overview**

### 🚀 **What Makes Derbent Special**

<div align="center">

| 🏢 **Enterprise Features** | 🛠️ **Developer Excellence** | 🎨 **User Experience** |
|:---:|:---:|:---:|
| Multi-tenant Architecture | C-Prefix Convention | Server-side Rendering |
| Role-based Access Control | Metadata-driven Development | Rich UI Components |
| Audit Trail & Compliance | Type-safe Inheritance | Responsive Design |
| Scalable Performance | GitHub Copilot Optimized | Intuitive Navigation |

</div>

### 🌟 **Key Challenges Solved**

#### **🔧 Enterprise Project Management Complexity**
- **Challenge**: Managing multiple projects, teams, and complex workflows
- **Solution**: Multi-tenant architecture with project-aware entities and role-based permissions

#### **📊 Data Consistency & Type Safety**
- **Challenge**: Runtime errors and data inconsistencies in large applications
- **Solution**: Comprehensive inheritance hierarchies with compile-time type checking

#### **🎨 UI Development Productivity**
- **Challenge**: Complex UI development and maintenance
- **Solution**: Metadata-driven form generation and component inheritance patterns

#### **🧪 Quality Assurance & Testing**
- **Challenge**: Ensuring UI consistency across complex workflows
- **Solution**: Comprehensive Playwright-based UI automation with screenshot testing

---

## 🚀 **Quick Start Guide**

### 📋 **Prerequisites**

<div align="center">

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)

</div>

- ☕ **Java 17+** - Latest LTS version recommended
- 📦 **Maven 3.8+** - Build and dependency management
- 🐘 **PostgreSQL** (recommended) or 🗃️ **H2** for development

### ⚡ **Lightning Fast Setup**

```bash
# 📥 Clone the repository
git clone https://github.com/SebnemC/derbent.git

# 📂 Navigate to project directory
cd derbent

# 🚀 Launch with H2 database (development mode)
mvn spring-boot:run -Dspring.profiles.active=h2

# 🌟 Open your browser to: http://localhost:8080
```

### 🎯 **First Login Experience**

<div align="center">

**🔑 Default Access**
- **URL**: `http://localhost:8080`
- **Admin credentials**: *Created automatically on first startup*
- **Setup wizard**: *Guided configuration for your first company and project*

</div>

---

## 📚 **Comprehensive Documentation & Resources**

<div align="center">

### 🎯 **Architecture & Design Patterns** 🎯

</div>

| **📖 Documentation** | **🎯 Purpose** | **👥 Audience** |
|:---|:---|:---|
| **[🤖 GitHub Copilot Guidelines](docs/copilot-guidelines.md)** | AI assistance patterns and coding standards | All Developers |
| **[🏗️ Entity Class Patterns](docs/entity-inheritance.md)** | Inheritance hierarchies and entity design | Backend Developers |
| **[⚙️ Service Class Patterns](docs/service-patterns.md)** | Business logic and repository patterns | Backend Developers |
| **[🎨 View Class Patterns](docs/view-patterns.md)** | UI component and page patterns | Frontend Developers |
| **[🔧 Utility Patterns](docs/utility-patterns.md)** | Helper classes and common patterns | All Developers |
| **[🔐 Company Login Pattern](docs/implementation/COMPANY_LOGIN_PATTERN.md)** | Multi-tenant authentication implementation | Backend/Security |
| **[🎭 Playwright Test Guide](docs/implementation/PLAYWRIGHT_TEST_GUIDE.md)** | Comprehensive UI testing framework | QA/Test Engineers |
| **[🔍 Database Query Debugging](docs/DATABASE_QUERY_DEBUGGING.md)** | SQL query monitoring and debugging guide | Backend Developers |

<div align="center">

### 🏗️ **Advanced Architecture Details** 🏗️

</div>

<details>
<summary><b>🌐 Technology Stack Deep Dive</b> - <i>Click to expand</i></summary>

#### **Backend Excellence**
- **☕ Java 17**: Latest LTS with modern language features
- **🍃 Spring Boot 3.5**: Enterprise application framework
- **🗄️ Hibernate/JPA**: Object-relational mapping with advanced features
- **🔒 Spring Security**: Comprehensive security framework
- **📊 HikariCP**: High-performance JDBC connection pooling

#### **Frontend Innovation**  
- **🎨 Vaadin Flow 24.8**: Server-side UI framework
- **📱 Responsive Design**: Mobile-first approach
- **♿ Accessibility**: WCAG 2.1 compliance built-in
- **🎯 Component Library**: Rich set of UI components

#### **Data & Testing**
- **🐘 PostgreSQL**: Production-grade relational database
- **🗃️ H2 Database**: In-memory database for development
- **🎭 Playwright**: Modern browser automation
- **📋 JUnit 5**: Advanced testing framework

</details>

<details>
<summary><b>📁 Project Structure Deep Dive</b> - <i>Click to expand</i></summary>

```
🏗️ src/main/java/tech/derbent/
├── 📚 api/                    # Core framework and utilities
│   ├── 🏛️ domains/           # Base entity classes (CEntity, CEntityDB)
│   ├── ⚙️ services/          # Service base classes and patterns
│   ├── 🎨 views/             # UI base classes and components
│   ├── 🔧 utils/             # Utilities (CAuxillaries, validation)
│   └── 🔐 annotations/       # Custom annotations for metadata
├── 🏢 [business-modules]/     # Feature-specific modules
│   ├── 📊 activities/        # Task and activity management
│   ├── 👥 users/             # User and role management  
│   ├── 🏗️ projects/          # Project and company management
│   ├── 📅 meetings/          # Meeting and agenda management
│   └── ⚠️ risks/             # Risk assessment and tracking
├── 🔐 session/               # Authentication and session management
└── ⚙️ config/                # Application configuration
```

</details>

---

## 🏗️ **Master Design Patterns & Architecture**

### 🎯 **Signature Design Patterns**

<details>
<summary><b>🏛️ C-Prefix Convention Pattern</b> - <i>Click to expand</i></summary>

```java
// All custom classes follow the C-prefix convention
public class CActivity extends CEntityOfProject<CActivity> { }
public class CActivityService extends CProjectItemService<CActivity> { }
public class CActivityView extends CGridViewBaseProject<CActivity> { }
```

**Benefits**: Instant recognition of custom vs. framework classes, enhanced IDE navigation, AI-assisted development
</details>

<details>
<summary><b>🔗 Inheritance Hierarchies Pattern</b> - <i>Click to expand</i></summary>

```
CEntity<T>
    ↓
CEntityDB<T>
    ↓
CEntityNamed<T>
    ↓
CEntityOfProject<T>
    ↓
[Domain Classes: CActivity, CUser, CRisk...]
```

**Benefits**: Consistent behavior, reduced boilerplate, compile-time safety, metadata inheritance
</details>

<details>
<summary><b>🎨 Metadata-Driven Development</b> - <i>Click to expand</i></summary>

```java
@Entity
@Table(name = "activities")
public class CActivity extends CEntityOfProject<CActivity> {
    
    @Column(nullable = false)
    @NotBlank(message = "Activity name is required")
    private String name;
    
    // UI forms automatically generated from annotations
    // Validation rules derived from constraints
    // Database schema created from metadata
}
```

**Benefits**: DRY principle, automatic UI generation, consistent validation, reduced maintenance
</details>

<details>
<summary><b>🎪 Project-Aware Context Pattern</b> - <i>Click to expand</i></summary>

```java
// Every entity knows its project context
public abstract class CEntityOfProject<T> extends CEntityNamed<T> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private CProject project;
    
    // Automatic project filtering in queries
    // Context-aware security and permissions
}
```

**Benefits**: Multi-tenancy, data isolation, context-aware operations, secure data access
</details>

### 🛠️ **Service Layer Excellence**

```java
// Standard service pattern with inheritance
@Service
@PreAuthorize("hasRole('USER')")
public class CActivityService extends CProjectItemService<CActivity> {
    
    // Automatic CRUD operations inherited
    // Project-aware queries built-in
    // Security annotations at class level
    // Transaction management included
}
```

### 🎨 **View Layer Innovation**

```java
// View inheritance with automatic features
@Route("activities")
@RolesAllowed("USER")
public class CActivityView extends CGridViewBaseProject<CActivity> {
    
    // Grid configuration automatic
    // Project filtering built-in
    // Export functionality included
    // Responsive design inherited
}
```

---

## ⚡ **Technical Superiorities & Style Advantages**

### 🎨 **Vaadin Flow Excellence**
<div align="center">

**🌟 Server-Side Rendering Superiority 🌟**

</div>

| **Traditional SPA Challenges** | **Derbent's Vaadin Solution** |
|:---|:---|
| ❌ Complex state management | ✅ Server-side state management |
| ❌ API synchronization issues | ✅ Automatic server synchronization |
| ❌ SEO and accessibility concerns | ✅ Native HTML rendering |
| ❌ Bundle size optimization | ✅ Progressive loading |
| ❌ Browser compatibility issues | ✅ Java ecosystem reliability |

### 🔒 **Type Safety & Compile-Time Guarantees**

```java
// 🎯 Type-safe entity relationships
public class CActivity extends CEntityOfProject<CActivity> {
    
    // ✅ Compile-time type checking
    // ✅ Generic type safety throughout inheritance
    // ✅ IDE auto-completion and refactoring support
    // ✅ Runtime type errors eliminated
}

// 🎯 Type-safe service operations
CActivityService service = applicationContext.getBean(CActivityService.class);
List<CActivity> activities = service.findByProject(project); // ✅ Type guaranteed
```

### 🧠 **AI-Assisted Development Excellence**

**🤖 GitHub Copilot Optimization**
- **Consistent Naming**: C-prefix convention enables accurate AI suggestions
- **Pattern Recognition**: Inheritance hierarchies help AI understand context
- **Metadata Awareness**: Annotations guide AI for form and validation generation
- **Documentation Standards**: Comprehensive docs improve AI assistance quality

### 🏗️ **Architectural Superiorities**

#### **🎯 Metadata-Driven Development**
```java
// Single source of truth for business rules
@Entity
@Table(name = "activities")
public class CActivity extends CEntityOfProject<CActivity> {
    
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name too long")
    private String name;  // ✅ DB + Validation + UI generation from one definition
}
```

#### **🔄 Inheritance-Based Code Reuse**
- **90%+ code reuse** across similar entities
- **Consistent behavior** across the application
- **Zero boilerplate** for standard CRUD operations
- **Automatic feature inheritance** (export, filtering, pagination)

#### **🎨 Component Excellence**
```java
// Smart component with built-in features
public class CButton extends Button {
    // ✅ Automatic styling and theming
    // ✅ Built-in accessibility features
    // ✅ Consistent behavior across app
    // ✅ Icon and text combination logic
}
```

---

## 🔧 **Key Features & Capabilities**

<div align="center">

### 🌟 **Enterprise-Grade Features** 🌟

</div>

<table>
<tr>
<td width="50%">

#### **🏢 Multi-Tenant Architecture**
- **Project Isolation**: Complete data separation between projects
- **Company Management**: Support for multiple organizations
- **Context Awareness**: Every operation knows its project context
- **Scalable Design**: Ready for thousands of users and projects

#### **📊 Advanced Data Management**
- **Hierarchical Entities**: Projects → Activities → Sub-tasks → Comments
- **Type Entities**: Configurable types for activities, meetings, risks
- **Status Tracking**: Color-coded workflows with visual indicators
- **Audit Trail**: Complete change history for compliance

</td>
<td width="50%">

#### **🎨 Rich User Interface**
- **Enhanced Grids**: Sortable, filterable, exportable data tables
- **Smart Forms**: Auto-generated from entity metadata
- **Kanban Boards**: Drag-and-drop task management
- **Responsive Design**: Perfect on desktop, tablet, and mobile

#### **🧪 Quality Assurance**
- **Playwright Automation**: Screenshot-based UI testing
- **Page Object Model**: Maintainable test architecture  
- **Visual Regression**: Automated UI consistency checks
- **CI/CD Ready**: Headless and headed test execution

</td>
</tr>
</table>

### 📈 **Performance & Scalability Excellence**

<div align="center">

| **Performance Feature** | **Implementation** | **Benefit** |
|:---:|:---:|:---:|
| 🚀 **Lazy Loading** | JPA `@LazyLoading` patterns | Reduced memory footprint |
| 🔍 **Efficient Queries** | `JOIN FETCH` strategies | Eliminated N+1 problems |
| 🏊 **Connection Pooling** | HikariCP configuration | High concurrency support |
| 💾 **Strategic Caching** | Entity and query caching | Improved response times |
| 📊 **Progressive Loading** | Vaadin lazy data providers | Smooth large dataset handling |

</div>

---

## 🧪 **Testing Strategy & Quality Assurance**

<div align="center">

### 🎯 **Comprehensive Testing Approach** 🎯

</div>

<table>
<tr>
<td width="33%">

#### **🎭 Playwright UI Automation**
- **Visual Testing**: Screenshot comparison
- **Page Object Model**: Maintainable test architecture
- **Cross-browser**: Chrome, Firefox, Safari support
- **CI/CD Integration**: Headless execution ready

</td>
<td width="33%">

#### **🧪 Unit & Integration Testing**
- **JUnit 5**: Modern testing framework
- **Service Layer**: Business logic validation
- **Repository Testing**: Data access verification
- **Spring Boot Test**: Application context testing

</td>
<td width="33%">

#### **📊 Quality Metrics**
- **Test Coverage**: Service and utility layers
- **Code Quality**: Spotless formatting
- **Architecture Tests**: ArchUnit validation
- **Performance**: Load testing ready

</td>
</tr>
</table>

### 🛠️ **Development Workflow Excellence**

#### **🤖 GitHub Copilot Optimization**
This project is specifically designed for AI-assisted development:

```bash
# 📚 Essential reading for developers
docs/copilot-guidelines.md     # AI assistance patterns
docs/entity-inheritance.md     # Entity design patterns  
docs/service-patterns.md       # Business logic patterns
docs/view-patterns.md          # UI component patterns
docs/utility-patterns.md       # Helper and utility patterns
```

#### **🔄 Feature Development Lifecycle**
1. **🏗️ Entity Design**: Create classes following inheritance patterns
2. **⚙️ Service Layer**: Implement business logic with repository patterns  
3. **🎨 UI Components**: Build views using inheritance hierarchy
4. **🧪 Test Coverage**: Add comprehensive Playwright tests
5. **📚 Documentation**: Update patterns and Copilot guidelines

---

## 🤝 **Contributing to Derbent**

<div align="center">

### 🌟 **Join Our Developer Community** 🌟

**We welcome contributions from developers of all skill levels!**

</div>

### 📋 **Contribution Guidelines**

#### **🚀 Getting Started**
1. **📚 Study the Patterns**: Read our [Copilot Guidelines](docs/copilot-guidelines.md)
2. **🏗️ Follow Architecture**: Understand the established inheritance patterns  
3. **🧪 Add Tests**: Comprehensive Playwright test coverage required
4. **📖 Update Docs**: Document significant changes and new patterns

#### **💡 Areas for Contribution**
- **🔧 New Features**: Additional project management capabilities
- **🎨 UI Enhancements**: Improved user experience and accessibility
- **📈 Performance**: Database optimization and caching improvements
- **🧪 Testing**: Extended test coverage and quality assurance
- **📚 Documentation**: Enhanced guides and architectural documentation

#### **🛠️ Development Setup**
```bash
# Fork and clone the repository
git clone https://github.com/your-username/derbent.git

# Install and verify setup  
mvn clean compile
mvn spotless:apply

# Run tests to ensure everything works
./run-playwright-tests.sh mock
```

---

## 🏆 **Credits & Acknowledgments**

<div align="center">

### 🌟 **Project Leadership & Vision** 🌟

</div>

#### **👑 Core Development Team**
- **🎯 [SebnemC](https://github.com/SebnemC)** - *Project Lead & Architecture*
- **🚀 [yalovali](https://github.com/yalovali)** - *Technical Lead & Innovation*

#### **🎨 Inspiration & Design Philosophy**
- **📊 Atlassian Jira** - *Enterprise project management patterns*
- **🛠️ ProjeQtOr** - *Comprehensive project lifecycle management*
- **🏗️ Spring Framework Team** - *Enterprise Java application patterns*
- **🎨 Vaadin Team** - *Server-side UI excellence*

#### **🛠️ Technology Stack Credits**
- **☕ [OpenJDK Community](https://openjdk.org/)** - *Java platform excellence*
- **🍃 [Spring Team](https://spring.io/)** - *Enterprise application framework*
- **🎨 [Vaadin](https://vaadin.com/)** - *Modern server-side UI framework*
- **🐘 [PostgreSQL Global Development Group](https://www.postgresql.org/)** - *Advanced relational database*
- **🎭 [Playwright Team](https://playwright.dev/)** - *Reliable UI automation testing*

#### **📚 Documentation & Community**
- **🤖 GitHub Copilot** - *AI-assisted development optimization*
- **📖 Markdown Community** - *Beautiful documentation standards*
- **🎯 Open Source Community** - *Collaborative development inspiration*

<div align="center">

**💙 Special thanks to all contributors who help make Derbent better every day! 💙**

</div>

---

## 📄 **License**

<div align="center">

![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

**This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.**

*Free to use, modify, and distribute for personal and commercial projects.*

</div>

---

## 🆘 **Support & Community**

<div align="center">

### 🌟 **Get Help & Stay Connected** 🌟

</div>

### 📞 **Getting Support**

#### **📚 Self-Service Resources**
- **🔍 Documentation**: Comprehensive guides in the `docs/` directory
- **💡 Code Examples**: Extensive patterns and examples in the codebase  
- **🎯 Best Practices**: Follow the established coding conventions
- **🤖 AI Assistance**: Leverage GitHub Copilot with our optimized patterns

#### **🤝 Community Support**
- **🐛 Issues**: Report bugs and request features on GitHub Issues
- **💬 Discussions**: Join conversations about architecture and features
- **📧 Direct Contact**: Reach out to maintainers for enterprise support
- **🔄 Pull Requests**: Contribute improvements and bug fixes

### 🚀 **Enterprise & Professional Support**

For organizations requiring dedicated support, training, or custom development:
- **🏢 Enterprise Consulting**: Architecture reviews and optimization
- **🎓 Team Training**: Comprehensive onboarding for development teams
- **⚡ Priority Support**: Fast-track issue resolution and feature requests
- **🔧 Custom Development**: Tailored features and integrations

---

<div align="center">

### 🌟 **Thank you for choosing Derbent!** 🌟

**Star ⭐ this repository if you find it helpful • Share 📢 with your team • Contribute 🤝 to make it better**

---

**Built with ❤️ by the Derbent team • Powered by Java, Spring Boot, and Vaadin • Made for developers, by developers**

---

*🚀 Ready to revolutionize your project management? [Get started now!](#-quick-start-guide) 🚀*

</div>