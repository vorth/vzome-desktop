
//(c) Copyright 2010, Scott Vorthmann.

package org.vorthmann.zome.app.impl;

import java.awt.event.ActionEvent;

import com.vzome.core.editor.Tool;

public class ToolEvent extends ActionEvent {
	
	private final Tool tool;
	private final int modes;

	public ToolEvent( Tool tool, int modifiers, ToolsController toolsController, ActionEvent e )
	{
		super( toolsController, 0, "toolApplied", modifiers );
		
		this .tool = tool;
		this .modes = modifiers;
	}

	public Tool getTool()
	{
		return tool;
	}

	public int getModes()
	{
		return modes;
	}

}
