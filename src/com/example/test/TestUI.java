package com.example.test;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.widproj.widget.MultiItemSelector;
import com.widproj.widget.MultiItemSelector.MItem;
import com.widproj.widget.util.MitemJSONUtil;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("test")
public class TestUI extends UI {
	
	private String json;
	int count = 1;

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = TestUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		setContent(layout);

		Button deSerializeButton = new Button("De serialize");
		layout.addComponent(deSerializeButton);
		
		Button serializeButton = new Button("Serialize");
		layout.addComponent(serializeButton);
		
		Label parentLabel = new Label("Parent");
		parentLabel.setStyleName("mItemLabelBold");
		layout.addComponent(parentLabel);
		MultiItemSelector parent = new MultiItemSelector();
		
		for(int i = 0; i < 10; i++){
			MItem parentItem = parent.addMItem("Item " + i);
			
			//chid datasource
			for(int j = 0; j < 5; j++){
				parentItem.addChildMItem("SubItem " + i + "." + j);
			}
		}
		layout.addComponent(parent);
		
		parent.setCurrentMItem("Item 1");
		
		
		MultiItemSelector child = new MultiItemSelector();
		layout.addComponent(child);
		child.setCaption("Child - custom caption");
		child.setCaptionStyleName("mItemLabelBold");
		
		parent.setChildMultiItemSelector(child);
		
		serializeButton.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				String json = MitemJSONUtil.getJSON(parent);
				TestUI.this.json = json;
			}
		});
		
		deSerializeButton.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				MitemJSONUtil.populateMultiItemSelector(parent, json);
				parent.setCurrentMItem("Item 1");
			}
		});
	}

}