package com.feofus.erp.widget;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class MultiItemSelector extends VerticalLayout{
	
	private static final long serialVersionUID = 1L;
	
	private List<MItemClickListener> mItemClickListeners = new LinkedList<>();
	private Map<Object, MItem> componentDataSource = new LinkedHashMap<>();
	
	private Panel mItemPanel = new Panel();
	private VerticalLayout mItemsLayout = new VerticalLayout();
	
	private MItem selectAllMItem = new MItem("Select all", "Select all");
	private CheckBox selectAllCheckBox = selectAllMItem.getCheckBox();
	
	//Keeps track of number of selected items. Useful for the functionality of selectall
	private int selections = 0;
	
	//To distinguish between ordinary MItem checkBox click an 'selectAll' checkBox click
	private boolean listeningToSelectAll = true;
	
	//To make the item select only one item a time
	private boolean enableSingleSelection = false;
	
	//Keeps track of currently clicked MItem
	private MItem clickedMItem = null;
	
	//Keeps track of last selected check box
	private CheckBox lastCheckedCheckBox = null;
	
	//Each MustiItemSelector can have a child
	//Its componentDataSource will be populated by the parent
	private MultiItemSelector child = null;
		
	//Layout dimensions
	private String auto = "-1px";
	private String full = "100%";
	private String _80px = "80px";
	
	/* **************************
	 * Add to styles
	 * 
	 .mutiSelectBorder{
	 border-radius: 7px;
	}
	
	.mItemLabelBold {
		font-weight: bold;
		line-height: normal;
	}
	
	.mItemLabelNormal {
		font-weight: normal;
		line-height: normal;
	}
	 * **************************/
	private String roundBorder = "mutiSelectBorder";
	private String normalLabel = "mItemLabelNormal";
	private String boldLabel = "mItemLabelBold";
	
	public MultiItemSelector(){
		
		super.setWidth(full);
		
		mItemsLayout.setSizeUndefined();
		mItemsLayout.setSpacing(false);
		
		mItemPanel.setWidth(full);
		mItemPanel.setHeight(_80px);
		mItemPanel.setContent(mItemsLayout);
		mItemPanel.setStyleName(roundBorder);
		
		super.addComponent(selectAllMItem.getMItemLayout());
		super.addComponent(mItemPanel);
		
		selectAllCheckBox = selectAllMItem.getCheckBox();
		selectAllCheckBox.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(MultiItemSelector.this.listeningToSelectAll){
					boolean selected = MultiItemSelector.this.selectAllCheckBox.getValue();
					if(selected){
						selectAll();
					} else {
						unselectAll();
					}
				}
			}
		});
		
		refresh();
	}
	
	public void setCaption(String caption){
		selectAllMItem.setCaption(caption);
	}
	
	public void setCaptionStyleName(String styleName){
		selectAllMItem.setCaptionStyleName(styleName);
	}
	
	public boolean isSingleItemSelectionEnabled(){
		return enableSingleSelection;
	}
	
	public void setSingleItemSelection(boolean enable){
		if(enableSingleSelection != enable){
			changeSelectorMode(enable);
		}
	}
	
	public void changeSelectorMode(boolean enable){
		if(enable){
			//No need for selectall check box
			//It will be automatically unchecked on unchecking mItems
			selectAllMItem.getCheckBox().setVisible(false);
			
			//Keep the selections if there is at most one item selected else clear all selections
			if(selections > 1){
				for(MItem mItem : componentDataSource.values()){
					mItem.setSelected(false);
				}
				selections = 0;
			}
		} else {
			//need selectall check box
			selectAllMItem.getCheckBox().setVisible(true);
		}
		
		enableSingleSelection = enable;
	}
	public void refresh(){
		mItemsLayout.removeAllComponents();
		selections = 0;
		boolean isAllSelected = true;
		if(componentDataSource != null){
			for(MItem mItem : componentDataSource.values()){
				//Cases when the item being added through setComponentDatasource()
				if(mItem.getMItemLayout().getListeners(LayoutClickEvent.class).size() == 0){
					mItem.getMItemLayout().addLayoutClickListener(new MItemLayoutClickListener(mItem));
				}
				if(mItem.getCheckBox().getListeners(ValueChangeEvent.class).size() == 0){
					mItem.getCheckBox().addValueChangeListener(new MItemSelectionListener(mItem));
				}
				
				this.mItemsLayout.addComponent(mItem.getMItemLayout());
				
				if(mItem.isSelected()){
					//Don't be confused. This assignment will be used only
					//if selections == 1(Single item selected is the last checked one).
					lastCheckedCheckBox = mItem.getCheckBox();
					
					selections++;
				} else {
					isAllSelected = false;
				}
			}
		} else {
			isAllSelected = false;
		}
		
		if(selections != 1){
			if(enableSingleSelection){
				unselectAll();
			}
			lastCheckedCheckBox = null;
		}
		
		listeningToSelectAll = false;
		selectAllMItem.setSelected(isAllSelected);
		listeningToSelectAll = true;
	}
	
	public void unselectAll(){
		for(MItem mItem : componentDataSource.values()){
			mItem.setSelected(false);
		}
		selections = 0;
	}
	
	public void selectAll(){
		for(MItem mItem : componentDataSource.values()){
			mItem.setSelected(true);
		}
		selections = componentDataSource.size();
	}
	
	public MItem addMItem(Object MItemId) throws IllegalArgumentException, IllegalStateException{
		return addMItem(MItemId, MItemId.toString());
	}
	
	public MItem addMItem(Object mItemId, String caption) throws IllegalStateException{
		
		if(componentDataSource.containsKey(mItemId)){
			throw new IllegalStateException("Duplicate item id : " + mItemId.toString());
		}
		
		MItem mItem = new MItem(mItemId, caption);
		componentDataSource.put(mItemId, mItem);
		mItemsLayout.addComponent(mItem.getMItemLayout());

		mItem.getMItemLayout().addLayoutClickListener(new MItemLayoutClickListener(mItem));
		mItem.getCheckBox().addValueChangeListener(new MItemSelectionListener(mItem));
		
		//Adding new item also means setting select all false
		listeningToSelectAll = false;
		selectAllCheckBox.setValue(false);
		listeningToSelectAll= true;
		
		return mItem;
	}
	
	public MItem getMItem(Object mItemId){
		return componentDataSource.get(mItemId);
	}
	
	public void removeMItem(Object mItemId){
		MItem removedItem = componentDataSource.remove(mItemId);
		if(removedItem != null){
			mItemsLayout.removeComponent(removedItem.getMItemLayout());
		}
	}
	
	public void clear(){
		componentDataSource.clear();
		refresh();
	}
	
	public Map<Object, MItem> getComponentDataSource() {
		return componentDataSource;
	}
	
	public void setComponentDataSource(Map<Object, MItem> componentDataSource) {
		this.componentDataSource = componentDataSource;
		refresh();
	}
	
	public void addMItemClickListener(MItemClickListener listener){
		if(listener != null){
			mItemClickListeners.add(listener);
		}
	}
	
	public void clearMItemClickListeners(){
		mItemClickListeners.clear();
	}
	
	private void fireMItemClickEvent(MItemClickEvent event){
		if(event != null){
			for(MItemClickListener listener: mItemClickListeners){
				listener.action(event);
			}
		}
	}
	
	public void setHeight(String height){
		if(height != null){
			mItemPanel.setHeight(height);
		}
	}
	
	public void setHeight(float height, Unit unit){
		mItemPanel.setHeight(height, unit);
	}
	
	public List<MItem> getSelectedMItems(){
		List<MItem> selectedItems = new LinkedList<>();
		for(MItem item : componentDataSource.values()){
			if(item.isSelected()){
				selectedItems.add(item);
			}
		}
		return selectedItems;
	}
	
	public void setChildMultiItemSelector(MultiItemSelector child){
		this.child = child;
		if(this.child != null){
			if(clickedMItem != null){
				this.child.setComponentDataSource(clickedMItem.getChildDataSource());
			}
		}
	}
	
	public MultiItemSelector getChildMultiItemSelector(){
		return child;
	}
	
	public void removeChildMultiItemSelector(){
		child = null;
	}
	
	public void setCurrentMItem(Object mItemId){
		MItem mItem = componentDataSource.get(mItemId);
		if(mItem != null){
			setClickedMItem(mItem);
		}
	}
	
	public MItem getCurrentMItem(){
		return clickedMItem;
	}
	
	public void checkMItem(Object mItemId){
		MItem mItem = componentDataSource.get(mItemId);
		if(mItem != null){
			mItem.setSelected(true);
		}
	}

	public class MItem implements Serializable{

		private static final long serialVersionUID = 1L;
		
		final private Object mItemId;
		private String mItemCaption;
		private Map<String, Object> mItemProperties = new HashMap<String, Object>();
		
		private Map<Object, MItem> childDataSource = new LinkedHashMap<>();

		final private CheckBox mItemCheckBox = new CheckBox();
		final private Label mItemLabel = new Label();
		final private HorizontalLayout mItemLayout = new HorizontalLayout();
		
		@SuppressWarnings("unused")
		private MItem(){
			mItemId = null;//Only to suppress the error message 'final field itemId may not be initialized'
		}
		
		public MItem(Object mItemId, String caption) {
			this.mItemId = mItemId;
			this.mItemCaption = caption;
			
			configureMItemCheckBox();
			configureMItemLabel();
			configureMItemLayout();
		}

		private void configureMItemLayout() {
			mItemLayout.setWidth(auto);
			mItemLayout.setHeight(auto);
			mItemLayout.setSpacing(true);
			
			mItemLayout.addComponent(mItemCheckBox);
			mItemLayout.setComponentAlignment(mItemCheckBox, Alignment.MIDDLE_LEFT);
			
			mItemLayout.addComponent(mItemLabel);
			mItemLayout.setComponentAlignment(mItemLabel, Alignment.MIDDLE_LEFT);
			
		}

		private void configureMItemLabel() {
			mItemLabel.setValue(mItemCaption);
			mItemLabel.setWidth(full);
			mItemLabel.setHeight(auto);
			mItemLabel.setStyleName("mItemLabelNormal");
		}

		private void configureMItemCheckBox() {
			mItemCheckBox.setWidth(auto);
			mItemCheckBox.setHeight(auto);
		}

		public Object getMItemId() {
			return mItemId;
		}
		
		public String getCaption() {
			return mItemCaption;
		}
		
		public void setCaption(String caption){
			this.mItemCaption = caption;
			mItemLabel.setValue(caption);
		}

		public boolean isSelected(){
			return mItemCheckBox.getValue();
		}
		
		public void setSelected(boolean newValue){
			this.mItemCheckBox.setValue(newValue);
		}


		public Map<String, Object> getProperties() {
			return mItemProperties;
		}
		
		public void setProperties(Map<String, Object> propertyMap){
			this.mItemProperties = propertyMap;
		}
		
		public Object getProperty(String propertyName){
			return this.mItemProperties.get(propertyName);
		}
		
		public void setProperty(String name, Object value){
			this.mItemProperties.put(name, value);
		}
		protected HorizontalLayout getMItemLayout() {
			return mItemLayout;
		}
		
		protected CheckBox getCheckBox(){
			return mItemCheckBox;
		}
		
		protected void setCaptionStyleName(String styleName){
			mItemLabel.setStyleName(styleName);
		}
		
		protected Map<Object, MItem> getChildDataSource(){
			return this.childDataSource;
		}
		
		public MItem addChildMItem(Object MItemId) throws IllegalArgumentException, IllegalStateException{
			return addChildMItem(MItemId, MItemId.toString());
		}
		
		public MItem addChildMItem(Object mItemId, String caption) throws IllegalStateException{
			
			if(this.childDataSource.containsKey(mItemId)){
				throw new IllegalStateException("Duplicate item id : " + mItemId.toString());
			}
			
			MItem mItem = new MItem(mItemId, caption);
			this.childDataSource.put(mItemId, mItem);
			
			//Child needs to be refreshed if item is
			//added through parent and the parent is the selected one
			if(child != null && MultiItemSelector.this.clickedMItem == this){
				child.refresh();
			}
			return mItem;
		}
		
		public MItem getChildMItem(Object mItemId){
			return this.childDataSource.get(mItemId);
		}
		
		public void removeChildMItem(Object mItemId){
			MItem removedItem = this.childDataSource.remove(mItemId);
			if(removedItem != null && MultiItemSelector.this.child != null){
				MultiItemSelector.this.child.refresh();
			}
		}
		
		public List<MItem> getSelectedChildren(){
			List<MItem> selectedChildren = new LinkedList<>();
			for(MItem item : this.childDataSource.values()){
				if(item.isSelected()){
					selectedChildren.add(item);
				}
			}
			return selectedChildren;
		} 
	}	
	
	public interface MItemClickListener extends Serializable{
		public void action(MItemClickEvent event);
	}
	
	public class MItemClickEvent implements Serializable{
		private static final long serialVersionUID = 1L;
		private Object itemId;
		private String caption;
		private boolean isMItemSlected;
		
		public MItemClickEvent(Object item, String caption, boolean isItemSlected){
			this.itemId = item;
			this.caption = caption;
			this.isMItemSlected = isItemSlected;
		}
		
		public Object getItemId() {
			return itemId;
		}
		public String getCaption() {
			return caption;
		}
		public boolean isSelected() {
			return isMItemSlected;
		}
	}
	
	private class MItemLayoutClickListener implements LayoutClickListener{
		private static final long serialVersionUID = 1L;
		private MItem mItem;
		
		public MItemLayoutClickListener(MItem mItem){
			this.mItem = mItem;
		}

		@Override
		public void layoutClick(LayoutClickEvent event) {
			setClickedMItem(mItem);
			
			//Firing event
			MItemClickEvent clickEvent = new MItemClickEvent(mItem.getMItemId(), mItem.getCaption(), mItem.isSelected());
			MultiItemSelector.this.fireMItemClickEvent(clickEvent);
		}
	} 
	
	private class MItemSelectionListener implements ValueChangeListener{
		
		private static final long serialVersionUID = 1L;
		private MItem mItem;
		
		public MItemSelectionListener(MItem mItem) {
			this.mItem = mItem;
		}

		@Override
		public void valueChange(ValueChangeEvent event) {
			if(mItem.isSelected()){
				//if single item selection is enabled then uncheck the current check box
				if(MultiItemSelector.this.enableSingleSelection){
					if(MultiItemSelector.this.lastCheckedCheckBox != null){
						MultiItemSelector.this.lastCheckedCheckBox.setValue(false);
					}
				}
				
				MultiItemSelector.this.lastCheckedCheckBox = mItem.getCheckBox();
				
				MultiItemSelector.this.selections++;
				if(MultiItemSelector.this.selections == MultiItemSelector.this.componentDataSource.size()){
					//Check selectall without the need to check the data MItems
					MultiItemSelector.this.listeningToSelectAll = false;
					MultiItemSelector.this.selectAllCheckBox.setValue(true);
					MultiItemSelector.this.listeningToSelectAll = true;
				}
			} else {
				//Unselecting any item is also unselecting selectAllCheckBox
				MultiItemSelector.this.listeningToSelectAll = false;
				MultiItemSelector.this.selectAllCheckBox.setValue(false);
				MultiItemSelector.this.listeningToSelectAll = true;
				
				MultiItemSelector.this.selections--;
			}
		}
		
	}

	private void setClickedMItem(MItem newMItem) {
		if(newMItem != null){
			//Highlighting the currently selected MItem
			if(MultiItemSelector.this.clickedMItem != null){
				MultiItemSelector.this.clickedMItem.setCaptionStyleName(normalLabel);
			}
			newMItem.setCaptionStyleName(boldLabel);
			MultiItemSelector.this.clickedMItem = newMItem;
			
			//Populating Child
			if(MultiItemSelector.this.child != null){
				MultiItemSelector.this.child.setComponentDataSource(newMItem.getChildDataSource());
			}
		}
	}
}
