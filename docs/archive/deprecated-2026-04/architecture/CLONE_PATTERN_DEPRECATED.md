# DEPRECATED: Clone Pattern Documentation

**⚠️ This documentation is DEPRECATED as of 2026-01-19**

**Use instead:** [COPY_PATTERN.md](./COPY_PATTERN.md)

## Migration Guide

The old `createClone()` pattern has been replaced with `copyEntityTo()`.

### Old Pattern (DEPRECATED):
```java
public EntityClass createClone(CCloneOptions options) {
    EntityClass clone = super.createClone(options);
    // copy fields...
    return clone;
}
```

### New Pattern (CURRENT):
```java
@Override
protected void copyEntityTo(CEntityDB<?> target, CAbstractService serviceTarget, CCloneOptions options) {
    super.copyEntityTo(target, serviceTarget, options);
    if (target instanceof MyEntity targetEntity) {
        copyField(this::getField, targetEntity::setField);
    }
}
```

See [COPY_PATTERN.md](./COPY_PATTERN.md) for complete documentation.
