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
	
	//To distinguish between ordinary MItem checkBox click an 'selectAll' checkBox click
	private boolean enableSelectAllClickListener = true;
	
	//Keeps track of currently clicked MItem
	private MItem alreadySelectedMItem = null;
	
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
				if(enableSelectAllClickListener){
					boolean selected = selectAllCheckBox.getValue();
					
					for(MItem container : componentDataSource.values()){
						container.setSelected(selected);
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
	
	public void refresh(){
		mItemsLayout.removeAllComponents();
		boolean isAllSelected = true;
		
		for(MItem mItem : componentDataSource.values()){
			//Cases when the item being added through setComponentDatasource()
			if(mItem.getMItemLayout().getListeners(LayoutClickEvent.class).size() == 0){
				mItem.getMItemLayout().addLayoutClickListener(new MItemLayoutClickListener(mItem));
			}
			if(mItem.getCheckBox().getListeners(ValueChangeEvent.class).size() == 0){
				mItem.getCheckBox().addValueChangeListener(new MItemSelectionListener(mItem));
			}
			
			mItemsLayout.addComponent(mItem.getMItemLayout());
			
			if(!mItem.isSelected()){
				isAllSelected = false;
			}
		}
		
		selectAllMItem.setSelected(isAllSelected);
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
		enableSelectAllClickListener = false;
		selectAllCheckBox.setValue(false);
		enableSelectAllClickListener= true;
		
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
		mItemPanel.setHeight(height);
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
		if(alreadySelectedMItem != null){
			this.child.setComponentDataSource(alreadySelectedMItem.getChildDataSource());
		}
	}
	
	public MultiItemSelector getChildMultiItemSelector(){
		return this.child;
	}
	
	public void removeChildMultiItemSelector(){
		this.child = null;
	}
	
	public void setCurrentMItem(Object mItemId){
		MItem mItem = this.componentDataSource.get(mItemId);
		if(mItem != null){
			setAlreadySelectedMItem(mItem);
		}
	}
	
	public MItem getCurrentMItem(){
		return alreadySelectedMItem;
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
			return childDataSource;
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
			if(child != null && MultiItemSelector.this.alreadySelectedMItem == this){
				child.refresh();
			}
			return mItem;
		}
		
		public MItem getChildMItem(Object mItemId){
			return this.childDataSource.get(mItemId);
		}
		
		public void removeChildMItem(Object mItemId){
			MItem removedItem = componentDataSource.remove(mItemId);
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
			setAlreadySelectedMItem(mItem);
			
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
			//Unselecting any item is also unselecting selectAllCheckBox
			if(!mItem.isSelected()){
				enableSelectAllClickListener = false;
				selectAllCheckBox.setValue(false);
				enableSelectAllClickListener = true;				
			}
		}
		
	}

	private void setAlreadySelectedMItem(MItem newMItem) {
		if(newMItem != null){
			//Highlighting the currently selected MItem
			if(alreadySelectedMItem != null){
				alreadySelectedMItem.setCaptionStyleName(normalLabel);
			}
			newMItem.setCaptionStyleName(boldLabel);
			alreadySelectedMItem = newMItem;
			
			//Populating Child
			if(MultiItemSelector.this.child != null){
				MultiItemSelector.this.child.setComponentDataSource(newMItem.getChildDataSource());
			}
		}
	}
}
