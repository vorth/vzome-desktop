package org.vorthmann.zome.app.impl;

import java.awt.event.ActionEvent;

import org.vorthmann.ui.Controller;
import org.vorthmann.ui.DefaultController;

import com.vzome.core.editor.DocumentModel;

public class UndoRedoController extends DefaultController implements Controller
{
	private final DocumentModel model;

	public UndoRedoController( DocumentModel model )
	{
		super();
		this.model = model;
	}

	@Override
	public void doAction( String action, ActionEvent e ) throws Exception
	{
		switch ( action ) {

		case "undo":
            this .model .undo();
			break;

		case "redo":
        	this .model .redo();
			break;

		case "undoToBreakpoint":
        	this .model .undoToBreakpoint();
			break;

		case "redoToBreakpoint":
        	this .model .redoToBreakpoint();
			break;

		case "setBreakpoint":
        	this .model .setBreakpoint();
			break;

		case "undoAll":
        	this .model .undoAll();
			break;

		case "redoAll":
        	this .model .redoAll( - 1 );
			break;

		default:
            if ( action.startsWith( "redoUntilEdit." ) ) {
                String editNum = action .substring( "redoUntilEdit.".length() );
                this .model .redoAll( Integer.parseInt( editNum ) );
            }
            else
            	super .doAction( action, e );
			break;
		}
	}

}
