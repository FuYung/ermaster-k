package org.insightech.er.editor.model.diagram_contents.element.node.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.insightech.er.editor.controller.editpart.element.node.IResizable;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.connection.ConnectionElement;
import org.insightech.er.editor.model.diagram_contents.element.connection.ConnectionElementLocation;
import org.insightech.er.editor.model.diagram_contents.element.node.Location;
import org.insightech.er.editor.model.diagram_contents.element.node.NodeElement;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.diagram_contents.element.node.table.TableView;
import org.insightech.er.editor.model.diagram_contents.element.node.view.View;
import org.insightech.er.editor.model.settings.CategorySetting;
import org.insightech.er.util.Format;

public class Category extends NodeElement implements IResizable,
		Comparable<Category> {

	private static final long serialVersionUID = -7691417386790834828L;

	private static int MARGIN = 30;

	private List<NodeElement> nodeElementList;

	private Map<NodeElement, Location> nodeLocationMap;

	private Map<ConnectionElement, ConnectionElementLocation> connectionLocationMap;

	private String name;

	public Category() {
		this.nodeElementList = new ArrayList<NodeElement>();
		this.nodeLocationMap = new HashMap<NodeElement, Location>();
		this.connectionLocationMap = new HashMap<ConnectionElement, ConnectionElementLocation>();
	}

	public int getX() {
		return this.getX(null);
	}

	public int getY() {
		return this.getY(null);
	}

	public int getWidth() {
		return this.getWidth(null);
	}

	public int getHeight() {
		return this.getHeight(null);
	}

	public void setLocation(Location location) {
		this.setLocation(null, location);
	}

	public Location getLocation() {
		return getLocation(null);
	}

	public void setContents(List<NodeElement> contetns) {
		for (NodeElement content : nodeElementList) {
			if (!contetns.contains(content)) {
				content.removeCategory(this);
			}
		}
		for (NodeElement content : contetns) {
			if (!nodeElementList.contains(content)) {
				content.addCategory(this);
			}
		}
		this.nodeElementList = contetns;

		if (this.getWidth() == 0) {
			pack();
		}
	}
	
	public void pack() {
		
		int categoryX = 0;
		int categoryY = 0;

		int categoryWidth = 300;
		int categoryHeight = 400;

		if (!nodeElementList.isEmpty()) {
			Location nodeElementLocation = nodeElementList.get(0).getLocationInAll();
			categoryX = nodeElementLocation.x;
			categoryY = nodeElementLocation.y;
			categoryWidth = nodeElementLocation.width;
			categoryHeight = nodeElementLocation.height;

			for (NodeElement nodeElement : nodeElementList) {
				nodeElementLocation = nodeElement.getLocationInAll();
				int x = nodeElementLocation.x;
				int y = nodeElementLocation.y;
				int width = nodeElementLocation.width;
				int height = nodeElementLocation.height;

				if (categoryX > x - MARGIN) {
					width += categoryX - x + MARGIN;
					categoryX = x - MARGIN;
				}
				if (categoryY > y - MARGIN) {
					height += categoryY - y + MARGIN;
					categoryY = y - MARGIN;
				}

				if (x - categoryX + width + MARGIN > categoryWidth) {
					categoryWidth = x - categoryX + width + MARGIN;
				}

				if (y - categoryY + height + MARGIN > categoryHeight) {
					categoryHeight = y - categoryY + height + MARGIN;
				}

			}
		}

		this.setLocation(new Location(categoryX, categoryY, categoryWidth,
				categoryHeight));
	}

	public boolean contains(NodeElement nodeElement) {
		return this.nodeElementList.contains(nodeElement);
	}

	public boolean isVisible(NodeElement nodeElement, ERDiagram diagram) {
		boolean isVisible = false;

		if (this.contains(nodeElement)) {
			isVisible = true;

		} else {
			CategorySetting categorySettings = diagram.getDiagramContents()
					.getSettings().getCategorySetting();

			if (categorySettings.isShowReferredTables()) {
				for (NodeElement referringElement : nodeElement
						.getReferringElementList()) {
					if (this.contains(referringElement)) {
						isVisible = true;
						break;
					}
				}
			}
		}

		return isVisible;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<NodeElement> getContents() {
		return nodeElementList;
	}

	public void add(NodeElement nodeElement) {
		if (!(nodeElement instanceof Category)) {
			this.nodeElementList.add(nodeElement);
			nodeElement.addCategory(this);
		}
	}

	public void remove(NodeElement nodeElement) {
		this.nodeElementList.remove(nodeElement);
		nodeElement.removeCategory(this);
	}

	public List<ERTable> getTableContents() {
		List<ERTable> tableList = new ArrayList<ERTable>();

		for (NodeElement nodeElement : this.nodeElementList) {
			if (nodeElement instanceof ERTable) {
				tableList.add((ERTable) nodeElement);
			}
		}

		return tableList;
	}

	public List<View> getViewContents() {
		List<View> viewList = new ArrayList<View>();

		for (NodeElement nodeElement : this.nodeElementList) {
			if (nodeElement instanceof View) {
				viewList.add((View) nodeElement);
			}
		}

		return viewList;
	}

	public List<TableView> getTableViewContents() {
		List<TableView> tableList = new ArrayList<TableView>();

		for (NodeElement nodeElement : this.nodeElementList) {
			if (nodeElement instanceof TableView) {
				tableList.add((TableView) nodeElement);
			}
		}

		return tableList;
	}

	public Location getNewCategoryLocation(NodeElement element) {
		if (element instanceof Category) {
			return null;
		}

		if (this.contains(element)) {
			Location elementLocation = element.getLocationInAll();
			Location newLocation = calculateCategoryLocation(elementLocation);

			if (!newLocation.equals(this.getLocation())) {
				return newLocation;
			}
		}

		return null;
	}

	public Location getNewCategoryLocation(Location elementLocation) {
		Location newLocation = calculateCategoryLocation(elementLocation);

		if (!newLocation.equals(this.getLocation())) {
			return newLocation;
		}

		return null;
	}

	private Location calculateCategoryLocation(Location elementLocation) {
		Location location = this.getLocation();

		if (elementLocation.x < location.x) {
			location.width += location.x - elementLocation.x;
			location.x = elementLocation.x;
		}
		if (elementLocation.y < location.y) {
			location.height += location.y - elementLocation.y;
			location.y = elementLocation.y;
		}
		if (elementLocation.x + elementLocation.width > location.x
				+ location.width) {
			location.width = elementLocation.x + elementLocation.width
					- location.x;
		}
		if (elementLocation.y + elementLocation.height > location.y
				+ location.height) {
			location.height = elementLocation.y + elementLocation.height
					- location.y;
		}

		return location;
	}
	
	public Location getNodeLocation(NodeElement nodeElement) {
		return this.nodeLocationMap.get(nodeElement);
	}
	
	public void putNodeLocation(NodeElement nodeElement, Location location) {
		this.nodeLocationMap.put(nodeElement, location);
	}

	public void removeNodeLocation(NodeElement nodeElement) {
		this.nodeLocationMap.remove(nodeElement);
	}
	
	public Map<NodeElement, Location> getNodeLocationMap() {
		return this.nodeLocationMap;
	}

	public ConnectionElementLocation getConnectionElementLocation(ConnectionElement connectionElement) {
		return this.connectionLocationMap.get(connectionElement);
	}
	
	public void putConnectionElementLocation(ConnectionElement connectionElement, ConnectionElementLocation location) {
		this.connectionLocationMap.put(connectionElement, location);
	}

	public void removeConnectionElementLocation(ConnectionElement connectionElement) {
		this.connectionLocationMap.remove(connectionElement);
	}
	
	public Map<ConnectionElement, ConnectionElementLocation> getConnectionLocationMap() {
		return this.connectionLocationMap;
	}

	public int compareTo(Category other) {
		int compareTo = 0;

		compareTo = Format.null2blank(this.name).compareTo(
				Format.null2blank(other.name));

		return compareTo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Category clone() {
		Category clone = (Category) super.clone();
		
		clone.nodeElementList = new ArrayList<NodeElement>();
		clone.nodeElementList.addAll(this.nodeElementList);

		clone.nodeLocationMap = new HashMap<NodeElement, Location>();
		clone.nodeLocationMap.putAll(this.nodeLocationMap);

		clone.connectionLocationMap = new HashMap<ConnectionElement, ConnectionElementLocation>();
		clone.connectionLocationMap.putAll(this.connectionLocationMap);

		return clone;
	}

	public String getDescription() {
		return "";
	}

	public String getObjectType() {
		return "category";
	}

}
