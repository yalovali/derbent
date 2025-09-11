package tech.derbent.screens.view;

import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.screens.domain.CGridEntity;

public class CComponentGridEntity extends CDiv {

	private static final long serialVersionUID = 1L;
	private CGrid grid;
	private CGridEntity gridEntity;

	public CComponentGridEntity(CGridEntity gridEntity) {
		super();
		this.gridEntity = gridEntity;
		createContent();
	}

	private void createContent() {
		grid = new CGrid<>(CGridEntity.class);
		// get the class from CGridEntity dataprovider
		grid.addColumn(null)
		// get columns from selectedFields and create columns in the grid
		this.add(grid);
	}
}
