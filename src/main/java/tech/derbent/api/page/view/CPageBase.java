package tech.derbent.api.page.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import tech.derbent.api.entity.view.CAbstractPage;

public abstract class CPageBase extends CAbstractPage {

	private static final long serialVersionUID = 1L;

	public CPageBase() {
		super();
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {/**/
	}

	@Override
	protected void setupToolbar() {/**/
	}
}
