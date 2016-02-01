
//(c) Copyright 2008, Scott Vorthmann.  All rights reserved.

package org.vorthmann.zome.app.impl;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.vorthmann.ui.DefaultController;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.editor.Tool;
import com.vzome.core.editor.Tools;
import com.vzome.core.math.symmetry.Symmetry;

public class ToolsController extends DefaultController implements PropertyChangeListener
{
    private final Tools model;

	private Symmetry symmetry;

	private final AlgebraicField field;

	public ToolsController( Tools model, AlgebraicField field, Symmetry symmetry )
    {
		super();
		this .model = model;
		this.field = field;
		this .symmetry = symmetry;
		
		model .addPropertyListener( this );
	}

	public void doAction( String action, ActionEvent e ) throws Exception
    {
    	// TODO first-class support for modes in ToolEvent
    	
        int modifiers = e .getModifiers();
        if ( toolNames .contains( action ) )
        {
            Tool tool = getTool( action );
	        this .model .applyTool( tool, modifiers );
        }
        else if ( action .startsWith( "newTool/" ) )
        {
            String name = action .substring( "newTool/" .length() );
            int nextDot = name .indexOf( "." );
            String group = name .substring( 0, nextDot );
            this .model .createTool( name, group, symmetry );
        }
        else
            super .doAction( action, e );
    }

    public String[] getCommandList( String listName )
    {
        if ( "tool.instances" .equals( listName ) )
            return (String[]) toolNames .toArray( new String[]{} );

        return super .getCommandList( listName );
    }

    private final ArrayList toolNames = new ArrayList();
            
    public Tool getTool( String toolName )
    {
        return (Tool) model .get( toolName );
    }

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{
		switch ( evt .getPropertyName() ) {

		case "symmetry":
			String name = (String) evt .getNewValue();
			this .symmetry = this .field .getSymmetry( name );
			break;

		case "tool.instances":
			name = (String) evt .getNewValue();
            toolNames .add( name );
			// forward from the model to the UI
            properties() .firePropertyChange( "tool.instances", null, name );
			break;

		default:
			break;
		}
	}
}
