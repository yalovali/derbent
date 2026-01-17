package tech.derbent.api.interfaces;

import java.util.Objects;

/**
 * Configuration options for entity cloning operations.
 * Determines which fields and relationships should be included in the clone.
 */
public class CCloneOptions {

    /**
     * Clone depth levels determining what gets copied.
     */
    public enum CloneDepth {
        /**
         * Clone only basic fields (name, description, dates, numeric values).
         * Excludes: relations, collections, attachments, comments, status, workflow.
         */
        BASIC_ONLY,

        /**
         * Clone basic fields plus parent/child relationships.
         * Includes: basic fields + parentId, parentType.
         */
        WITH_RELATIONS,

        /**
         * Clone basic fields, relations, and file attachments.
         * Includes: BASIC + RELATIONS + attachments collection.
         */
        WITH_ATTACHMENTS,

        /**
         * Clone basic fields, relations, and comments.
         * Includes: BASIC + RELATIONS + comments collection.
         */
        WITH_COMMENTS,

        /**
         * Clone everything including all collections and relations.
         * Includes: all fields + attachments + comments + links + tags.
         */
        FULL_DEEP_CLONE
    }

    private final CloneDepth depth;
    private final Class<?> targetEntityClass;
    private final boolean cloneStatus;
    private final boolean cloneWorkflow;
    private final boolean resetDates;
    private final boolean resetAssignments;

    /**
     * Creates clone options with default settings.
     * - Clone to same entity type
     * - Basic fields only
     * - Reset dates and assignments
     * - Clear status and workflow
     */
    public CCloneOptions() {
        this(null, CloneDepth.BASIC_ONLY, false, false, true, true);
    }

    /**
     * Creates clone options with specified depth.
     * 
     * @param depth the clone depth level
     */
    public CCloneOptions(final CloneDepth depth) {
        this(null, depth, false, false, true, true);
    }

    /**
     * Creates clone options with all parameters.
     * 
     * @param targetEntityClass target entity type (null for same type)
     * @param depth clone depth level
     * @param cloneStatus whether to copy status field
     * @param cloneWorkflow whether to copy workflow field
     * @param resetDates whether to clear date fields
     * @param resetAssignments whether to clear assignment fields
     */
    public CCloneOptions(
            final Class<?> targetEntityClass,
            final CloneDepth depth,
            final boolean cloneStatus,
            final boolean cloneWorkflow,
            final boolean resetDates,
            final boolean resetAssignments) {
        this.targetEntityClass = targetEntityClass;
        this.depth = Objects.requireNonNull(depth, "Clone depth cannot be null");
        this.cloneStatus = cloneStatus;
        this.cloneWorkflow = cloneWorkflow;
        this.resetDates = resetDates;
        this.resetAssignments = resetAssignments;
    }

    // Getters
    public CloneDepth getDepth() {
        return depth;
    }

    public Class<?> getTargetEntityClass() {
        return targetEntityClass;
    }

    public boolean isCloneStatus() {
        return cloneStatus;
    }

    public boolean isCloneWorkflow() {
        return cloneWorkflow;
    }

    public boolean isResetDates() {
        return resetDates;
    }

    public boolean isResetAssignments() {
        return resetAssignments;
    }

    /**
     * Checks if the clone depth includes relations.
     */
    public boolean includesRelations() {
        return depth == CloneDepth.WITH_RELATIONS
                || depth == CloneDepth.WITH_ATTACHMENTS
                || depth == CloneDepth.WITH_COMMENTS
                || depth == CloneDepth.FULL_DEEP_CLONE;
    }

    /**
     * Checks if the clone depth includes attachments.
     */
    public boolean includesAttachments() {
        return depth == CloneDepth.WITH_ATTACHMENTS
                || depth == CloneDepth.FULL_DEEP_CLONE;
    }

    /**
     * Checks if the clone depth includes comments.
     */
    public boolean includesComments() {
        return depth == CloneDepth.WITH_COMMENTS
                || depth == CloneDepth.FULL_DEEP_CLONE;
    }

    /**
     * Checks if this is a full deep clone.
     */
    public boolean isFullDeepClone() {
        return depth == CloneDepth.FULL_DEEP_CLONE;
    }

    /**
     * Builder pattern for creating clone options.
     */
    public static class Builder {
        private CloneDepth depth = CloneDepth.BASIC_ONLY;
        private Class<?> targetEntityClass = null;
        private boolean cloneStatus = false;
        private boolean cloneWorkflow = false;
        private boolean resetDates = true;
        private boolean resetAssignments = true;

        public Builder depth(final CloneDepth depth) {
            this.depth = depth;
            return this;
        }

        public Builder targetEntityClass(final Class<?> targetEntityClass) {
            this.targetEntityClass = targetEntityClass;
            return this;
        }

        public Builder cloneStatus(final boolean cloneStatus) {
            this.cloneStatus = cloneStatus;
            return this;
        }

        public Builder cloneWorkflow(final boolean cloneWorkflow) {
            this.cloneWorkflow = cloneWorkflow;
            return this;
        }

        public Builder resetDates(final boolean resetDates) {
            this.resetDates = resetDates;
            return this;
        }

        public Builder resetAssignments(final boolean resetAssignments) {
            this.resetAssignments = resetAssignments;
            return this;
        }

        public CCloneOptions build() {
            return new CCloneOptions(
                    targetEntityClass,
                    depth,
                    cloneStatus,
                    cloneWorkflow,
                    resetDates,
                    resetAssignments);
        }
    }

    @Override
    public String toString() {
        return "CCloneOptions{depth=" + depth
                + ", targetClass=" + (targetEntityClass != null ? targetEntityClass.getSimpleName() : "same")
                + ", cloneStatus=" + cloneStatus
                + ", cloneWorkflow=" + cloneWorkflow
                + ", resetDates=" + resetDates
                + ", resetAssignments=" + resetAssignments
                + "}";
    }
}
