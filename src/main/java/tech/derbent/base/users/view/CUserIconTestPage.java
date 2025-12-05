package tech.derbent.base.users.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

/** Enhanced test page to demonstrate SVG icon functionality with multiple display methods. 
 * Shows how user icons are displayed using the new SVG data URL approach. */
@Route ("user-icon-test")
@PageTitle ("User Icon Test - SVG Icons")
@AnonymousAllowed
public class CUserIconTestPage extends Div {

	private static final long serialVersionUID = 1L;

	public CUserIconTestPage(@Autowired final CUserService userService) {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setPadding(true);
		mainLayout.setWidthFull();
		
		// Title
		final H2 title = new H2("SVG Icon Test Page");
		title.getStyle().set("color", "#2196F3");
		mainLayout.add(title);
		
		// Description
		final Div description = new Div();
		description.setText("This page demonstrates the SVG icon solution using Vaadin's icon attribute with data URLs. " +
			"Icons are generated as pure SVG for users without profile pictures, showing colored circles with initials.");
		description.getStyle()
			.set("background-color", "#E3F2FD")
			.set("padding", "15px")
			.set("border-radius", "4px")
			.set("margin-bottom", "20px")
			.set("line-height", "1.6");
		mainLayout.add(description);
		
		try {
			final java.util.List<CUser> users = userService.findAll();
			
			if (users.isEmpty()) {
				mainLayout.add(new Div("No users found in database."));
			} else {
				// Section 1: Individual Icon Display
				mainLayout.add(createSection("1. Individual Icon Display", 
					"Each user icon is displayed using the getIcon() method with SVG data URLs"));
				
				final VerticalLayout iconSection = new VerticalLayout();
				iconSection.setSpacing(true);
				iconSection.setPadding(false);
				
				// Display up to 5 users
				final int displayCount = Math.min(5, users.size());
				for (int i = 0; i < displayCount; i++) {
					final CUser user = users.get(i);
					iconSection.add(createIconDisplay(user));
				}
				mainLayout.add(iconSection);
				
				// Section 2: CLabelEntity Display
				mainLayout.add(createSection("2. CLabelEntity Display", 
					"Standard way to display users in the application with icons and names"));
				
				final VerticalLayout labelSection = new VerticalLayout();
				labelSection.setSpacing(true);
				labelSection.setPadding(false);
				
				for (int i = 0; i < displayCount; i++) {
					final CUser user = users.get(i);
					final CLabelEntity userLabel = CLabelEntity.createUserLabel(user);
					userLabel.getStyle()
						.set("padding", "8px")
						.set("background-color", "#f5f5f5")
						.set("border-radius", "4px");
					labelSection.add(userLabel);
				}
				mainLayout.add(labelSection);
				
				// Section 3: Multiple Icons in Row
				mainLayout.add(createSection("3. Multiple Icons in Row", 
					"Demonstrates icon rendering in horizontal layout"));
				
				final HorizontalLayout iconRow = new HorizontalLayout();
				iconRow.setSpacing(true);
				
				for (int i = 0; i < displayCount; i++) {
					final CUser user = users.get(i);
					final Icon icon = user.getIcon();
					icon.getStyle()
						.set("border", "2px solid #ddd")
						.set("border-radius", "4px")
						.set("padding", "4px");
					iconRow.add(icon);
				}
				mainLayout.add(iconRow);
				
				// Section 4: Technical Details
				mainLayout.add(createSection("4. Technical Details", 
					"Information about the SVG icon implementation"));
				
				final Div technicalInfo = new Div();
				technicalInfo.getElement().setProperty("innerHTML",
					"<ul style='line-height: 1.8; color: #555;'>" +
					"<li><strong>Implementation:</strong> Uses Vaadin Icon component with 'icon' attribute set to SVG data URL</li>" +
					"<li><strong>SVG Generation:</strong> Pure SVG content with colored circle and text initials</li>" +
					"<li><strong>Data URL Format:</strong> data:image/svg+xml;charset=utf-8,{URL_ENCODED_SVG}</li>" +
					"<li><strong>Colors:</strong> Consistent colors generated from user name hash</li>" +
					"<li><strong>Size:</strong> 16x16 pixels, scalable without quality loss</li>" +
					"<li><strong>Profile Pictures:</strong> Wrapped in SVG container when available</li>" +
					"</ul>");
				mainLayout.add(technicalInfo);
			}
			
		} catch (final Exception e) {
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading users: " + e.getMessage());
			errorDiv.getStyle()
				.set("background-color", "#ffebee")
				.set("color", "#c62828")
				.set("padding", "15px")
				.set("border-radius", "4px");
			mainLayout.add(errorDiv);
			e.printStackTrace();
		}
		
		add(mainLayout);
	}
	
	private VerticalLayout createSection(final String title, final String description) {
		final VerticalLayout section = new VerticalLayout();
		section.setSpacing(false);
		section.setPadding(false);
		section.setMargin(true);
		
		final H3 sectionTitle = new H3(title);
		sectionTitle.getStyle().set("color", "#1976D2").set("margin-top", "30px");
		
		final Div sectionDesc = new Div();
		sectionDesc.setText(description);
		sectionDesc.getStyle().set("color", "#666").set("margin-bottom", "15px");
		
		section.add(sectionTitle, sectionDesc);
		return section;
	}
	
	private HorizontalLayout createIconDisplay(final CUser user) {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.getStyle()
			.set("padding", "10px")
			.set("background-color", "#f9f9f9")
			.set("border-radius", "4px")
			.set("border", "1px solid #e0e0e0");
		
		// Get the icon
		final Icon icon = user.getIcon();
		icon.getStyle()
			.set("border", "2px solid #ddd")
			.set("border-radius", "4px")
			.set("padding", "2px");
		
		// User info
		final VerticalLayout info = new VerticalLayout();
		info.setSpacing(false);
		info.setPadding(false);
		
		final Span nameSpan = new Span(user.getName() + (user.getLastname() != null ? " " + user.getLastname() : ""));
		nameSpan.getStyle().set("font-weight", "bold");
		
		final Span detailsSpan = new Span("Login: " + user.getLogin() + " | Initials: " + user.getInitials());
		detailsSpan.getStyle().set("font-size", "0.9em").set("color", "#666");
		
		final Span iconInfoSpan = new Span(user.getProfilePictureThumbnail() != null ? 
			"Has profile picture (wrapped in SVG)" : "Generated SVG avatar");
		iconInfoSpan.getStyle().set("font-size", "0.85em").set("color", "#2196F3").set("font-style", "italic");
		
		info.add(nameSpan, detailsSpan, iconInfoSpan);
		
		layout.add(icon, info);
		return layout;
	}
}
