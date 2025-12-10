package tech.derbent.api.annotations;

/** Enum to explicitly specify the UI component type to be created for a field.
 * This provides fine-grained control over component selection, overriding the default
 * type inference based on field type and metadata.
 * 
 * When not specified (AUTO), the component type is inferred from field type, data provider,
 * and other metadata attributes. */
public enum ComponentType {
	
	/** Automatically determine component type based on field type and metadata (default behavior) */
	AUTO,
	
	/** Single-select ComboBox (CNavigableComboBox) for entity or string selection */
	COMBOBOX,
	
	/** Multi-select ComboBox for Set/List fields */
	MULTISELECT_COMBOBOX,
	
	/** Grid-based list selector with checkboxes for Set/List fields */
	GRID_SELECTOR,
	
	/** Dual list selector (available/selected lists) for Set/List fields */
	DUAL_LIST_SELECTOR,
	
	/** Text field for string input */
	TEXTFIELD,
	
	/** Text area for multi-line string input */
	TEXTAREA,
	
	/** Number field for numeric input */
	NUMBERFIELD,
	
	/** Date picker for LocalDate fields */
	DATEPICKER,
	
	/** Time picker for LocalTime fields */
	TIMEPICKER,
	
	/** Date-time picker for LocalDateTime/Instant fields */
	DATETIMEPICKER,
	
	/** Checkbox for boolean fields */
	CHECKBOX,
	
	/** Radio button group for enum or small value sets */
	RADIOBUTTONS,
	
	/** Color picker ComboBox for color selection */
	COLORPICKER,
	
	/** Icon selector ComboBox for icon selection */
	ICONSELECTOR,
	
	/** Picture/image selector for byte[] image data */
	PICTURESELECTOR
}
