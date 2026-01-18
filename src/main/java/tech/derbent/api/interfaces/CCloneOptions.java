package tech.derbent.api.interfaces;

import java.util.Objects;

/** Configuration options for entity cloning operations. Determines which fields and relationships should be included in the clone. */
public class CCloneOptions {

	/** Builder pattern for creating clone options. */
	public static class Builder {

		private CloneDepth depth = CloneDepth.BASIC_ONLY;
		private Class<?> targetEntityClass = null;
		private boolean includeRelations = false;
		private boolean includeAttachments = false;
		private boolean includeComments = false;
		private boolean includeAllCollections = false;
		private boolean cloneStatus = false;
		private boolean cloneWorkflow = false;
		private boolean resetDates = true;
		private boolean resetAssignments = true;

		public CCloneOptions build() {
			return new CCloneOptions(targetEntityClass, depth, includeRelations, includeAttachments, includeComments, includeAllCollections,
					cloneStatus, cloneWorkflow, resetDates, resetAssignments);
		}

		public Builder cloneStatus(final boolean cloneStatus) {
			this.cloneStatus = cloneStatus;
			return this;
		}

		public Builder cloneWorkflow(final boolean cloneWorkflow) {
			this.cloneWorkflow = cloneWorkflow;
			return this;
		}

		public Builder depth(final CloneDepth depth) {
			this.depth = depth;
			return this;
		}

		public Builder includeAllCollections(final boolean includeAllCollections) {
			this.includeAllCollections = includeAllCollections;
			return this;
		}

		public Builder includeAttachments(final boolean includeAttachments) {
			this.includeAttachments = includeAttachments;
			return this;
		}

		public Builder includeComments(final boolean includeComments1) {
			includeComments = includeComments1;
			return this;
		}

		public Builder includeRelations(final boolean includeRelations1) {
			includeRelations = includeRelations1;
			return this;
		}

		public Builder resetAssignments(final boolean resetAssignments1) {
			resetAssignments = resetAssignments1;
			return this;
		}

		public Builder resetDates(final boolean resetDates1) {
			resetDates = resetDates1;
			return this;
		}

		public Builder targetClass(final Class<?> targetEntityClass) {
			this.targetEntityClass = targetEntityClass;
			return this;
		}
	}

	/** Clone depth levels determining what gets copied. */
	public enum CloneDepth {
		/** Clone only basic fields (name, description, dates, numeric values). Excludes: relations, collections, attachments, comments, status,
		 * workflow. */
		BASIC_ONLY,
		/** Clone basic fields plus parent/child relationships. Includes: basic fields + parentId, parentType. */
		WITH_RELATIONS,
		/** Clone basic fields, relations, and file attachments. Includes: BASIC + RELATIONS + attachments collection. */
		WITH_ATTACHMENTS,
		/** Clone basic fields, relations, and comments. Includes: BASIC + RELATIONS + comments collection. */
		WITH_COMMENTS,
		/** Clone everything including all collections and relations. Includes: all fields + attachments + comments + links + tags. */
		FULL_DEEP_CLONE
	}

	private final CloneDepth depth;
	private final Class<?> targetEntityClass;
	private final boolean includeRelations;
	private final boolean includeAttachments;
	private final boolean includeComments;
	private final boolean includeAllCollections;
	private final boolean cloneStatus;
	private final boolean cloneWorkflow;
	private final boolean resetDates;
	private final boolean resetAssignments;

	/** Creates clone options with default settings. - Clone to same entity type - Basic fields only - Reset dates and assignments - Clear status and
	 * workflow */
	public CCloneOptions() {
		this(null, CloneDepth.BASIC_ONLY, false, false, false, false, false, false, true, true);
	}

	/** Creates clone options with all parameters.
	 * @param targetEntityClass     target entity type (null for same type)
	 * @param depth                 clone depth level
	 * @param includeRelations      whether to copy parent/child relationships
	 * @param includeAttachments    whether to copy file attachments
	 * @param includeComments       whether to copy comments
	 * @param includeAllCollections whether to copy all collections
	 * @param cloneStatus           whether to copy status field
	 * @param cloneWorkflow         whether to copy workflow field
	 * @param resetDates            whether to clear date fields
	 * @param resetAssignments      whether to clear assignment fields */
	public CCloneOptions(final Class<?> targetEntityClass, final CloneDepth depth, final boolean includeRelations, final boolean includeAttachments,
			final boolean includeComments, final boolean includeAllCollections, final boolean cloneStatus, final boolean cloneWorkflow,
			final boolean resetDates, final boolean resetAssignments) {
		this.targetEntityClass = targetEntityClass;
		this.depth = Objects.requireNonNull(depth, "Clone depth cannot be null");
		this.includeRelations = includeRelations;
		this.includeAttachments = includeAttachments;
		this.includeComments = includeComments;
		this.includeAllCollections = includeAllCollections;
		this.cloneStatus = cloneStatus;
		this.cloneWorkflow = cloneWorkflow;
		this.resetDates = resetDates;
		this.resetAssignments = resetAssignments;
	}

	/** Creates clone options with specified depth.
	 * @param depth the clone depth level */
	public CCloneOptions(final CloneDepth depth) {
		this(null, depth, false, false, false, false, false, false, true, true);
	}

	// Getters
	public CloneDepth getDepth() { return depth; }

	public Class<?> getTargetEntityClass() { return targetEntityClass; }

	/** Checks if all collections should be included. */
	public boolean includesAllCollections() {
		return includeAllCollections || depth == CloneDepth.FULL_DEEP_CLONE;
	}

	/** Checks if the clone depth includes attachments. Now uses explicit flag instead of CloneDepth enum. */
	public boolean includesAttachments() {
		return includeAttachments || depth == CloneDepth.WITH_ATTACHMENTS || depth == CloneDepth.FULL_DEEP_CLONE;
	}

	/** Checks if the clone depth includes comments. Now uses explicit flag instead of CloneDepth enum. */
	public boolean includesComments() {
		return includeComments || depth == CloneDepth.WITH_COMMENTS || depth == CloneDepth.FULL_DEEP_CLONE;
	}

	/** Checks if the clone depth includes relations. Now uses explicit flag instead of CloneDepth enum. */
	public boolean includesRelations() {
		return includeRelations || depth == CloneDepth.WITH_RELATIONS || depth == CloneDepth.WITH_ATTACHMENTS || depth == CloneDepth.WITH_COMMENTS
				|| depth == CloneDepth.FULL_DEEP_CLONE;
	}

	public boolean isCloneStatus() { return cloneStatus; }

	public boolean isCloneWorkflow() { return cloneWorkflow; }

	/** Checks if this is a full deep clone. */
	public boolean isFullDeepClone() { return depth == CloneDepth.FULL_DEEP_CLONE; }

	public boolean isResetAssignments() { return resetAssignments; }

	public boolean isResetDates() { return resetDates; }

	@Override
	public String toString() {
		return "CCloneOptions{depth=" + depth + ", targetClass=" + (targetEntityClass != null ? targetEntityClass.getSimpleName() : "same")
				+ ", includeRelations=" + includeRelations + ", includeAttachments=" + includeAttachments + ", includeComments=" + includeComments
				+ ", includeAllCollections=" + includeAllCollections + ", cloneStatus=" + cloneStatus + ", cloneWorkflow=" + cloneWorkflow
				+ ", resetDates=" + resetDates + ", resetAssignments=" + resetAssignments + "}";
	}
}
