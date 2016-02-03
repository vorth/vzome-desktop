package org.vorthmann.zome.app.impl;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.vorthmann.ui.Controller;
import org.vorthmann.ui.DefaultController;

import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.Panel;
import com.vzome.core.model.Strut;
import com.vzome.core.render.RenderedManifestation;
import com.vzome.desktop.controller.RenderingViewer;

/**
 * Controller for handling contextual menu commands that apply only when a Manifestation can be picked.
 * 
 * We can assume that enableContextualCommands() will always be called before doAction() or getProperty(),
 * so the relevant commands will only be enabled if the pickedManifestation was set.
 * 
 * @author vorth
 *
 */
public class PickingController extends DefaultController implements Controller
{
	private final RenderingViewer viewer;
	private final Controller delegate;
	
	private transient Manifestation pickedManifestation;
	
	private final List<PickingTool> tools = new ArrayList<PickingTool>();

	public PickingController( RenderingViewer viewer, Controller delegate )
	{
		this.viewer = viewer;
		this.delegate = delegate;
	}

	@Override
	public void doAction( String action, ActionEvent e ) throws Exception
	{
		for ( PickingTool pickingTool : tools ) {
			if ( pickingTool .doManifestationAction( this .pickedManifestation, action) ) {
				this .pickedManifestation = null;
				return;
			}
		}
        switch ( action ) {

        case "undoToManifestation":
        case "symmTool-icosahedral":
        case "setSymmetryCenter":
        case "setSymmetryAxis":
        case "lookAtBall":
        case "setBuildOrbitAndLength":
        case "selectSimilarSize":
        	this .delegate .doManifestationAction( this .pickedManifestation, action );
        	break;
            
		default:
			this .delegate .doAction( action, e );
		}

	}

	@Override
	public boolean[] enableContextualCommands( String[] menu, MouseEvent e )
	{
        RenderedManifestation rm = this .viewer .pickManifestation( e );
        pickedManifestation = null;
        if ( rm != null && rm.isPickable() )
        	pickedManifestation = rm.getManifestation();

        boolean[] result = this .delegate .enableContextualCommands( menu, e );

		for ( PickingTool pickingTool : tools ) {
			result = pickingTool .enableCommands( this .pickedManifestation, menu, result );
		}

		for ( int i = 0; i < menu.length; i++ ) {
            String menuItem = menu[i];
            switch ( menuItem ) {

			case "undoToManifestation":
            	result[ i ] = pickedManifestation != null;
				break;

			case "lookAtBall":
	        case "symmTool-icosahedral":
	        case "setSymmetryCenter":
            	result[ i ] = pickedManifestation instanceof Connector;
				break;

			case "setSymmetryAxis":
			case "selectSimilarSize":
            	result[ i ] = pickedManifestation instanceof Strut;
				break;

			case "showPanelVertices":
            	result[ i ] = pickedManifestation instanceof Panel;
				break;

			default:
				if ( menuItem .startsWith( "showProperties-" ) )
					result[i] = pickedManifestation != null;
                break;
			}
        }
        return result;
    }

	@Override
	public String getProperty( String propName )
	{
		switch ( propName ) {

		case "objectProperties":
		case "objectColor":
			return this .delegate .getManifestationProperty( this .pickedManifestation, propName );

		default:
	 		return this .delegate .getProperty( propName );
		}
	}
}
