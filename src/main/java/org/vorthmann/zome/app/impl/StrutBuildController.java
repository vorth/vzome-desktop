package org.vorthmann.zome.app.impl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;

import org.vorthmann.j3d.MouseTool;
import org.vorthmann.j3d.MouseToolDefault;
import org.vorthmann.j3d.MouseToolFilter;
import org.vorthmann.j3d.Trackball;
import org.vorthmann.ui.DefaultController;
import org.vorthmann.ui.LeftMouseDragAdapter;

import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.construction.Construction;
import com.vzome.core.construction.Point;
import com.vzome.core.construction.Polygon;
import com.vzome.core.construction.Segment;
import com.vzome.core.math.symmetry.Axis;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.model.Panel;
import com.vzome.core.model.Strut;

public class StrutBuildController extends DefaultController implements PickingTool
{
	private final PreviewStrut previewStrut;
    
    private boolean useGraphicalViews = false, showStrutScales = false, useWorkingPlane = false;

    private Segment workingPlaneAxis = null;
    
    private final MouseTool previewStrutStart, previewStrutRoll, previewStrutPlanarDrag, previewStrutLength;
    
    private final MouseTool aggregate;

	public StrutBuildController()
	{
        useGraphicalViews = "true".equals( app .getProperty( "useGraphicalViews" ) );
        showStrutScales = "true" .equals( app .getProperty( "showStrutScales" ) );

        previewStrut = new PreviewStrut( field, mainScene, cameraController );
        
        previewStrutLength = new MouseToolFilter( cameraController .getZoomScroller() )
        {
            public void mouseWheelMoved( MouseWheelEvent e )
            {
                LengthController length = previewStrut .getLengthModel();
                if ( length != null )
                {
                    // scroll to scale the preview strut (when it is rendered)
                    length .getMouseTool() .mouseWheelMoved( e );
                    // don't adjustPreviewStrut() here, let the prop change trigger it,
                    // so we don't flicker for every tick of the mousewheel
                }
                else
                {
                    // no strut build in progress, so zoom the view
                    super .mouseWheelMoved( e );
                }
            }
        };

        // drag events to render or realize the preview strut;
        //   only works when drag starts over a ball
        previewStrutStart = new LeftMouseDragAdapter( new ManifestationPicker( imageCaptureViewer )
        {                
            protected void dragStarted( Manifestation target, boolean b )
            {
                if ( target instanceof Connector )
                {
                    mErrors .clearError();
                    Point point = (Point) ((Connector) target) .getConstructions() .next();
                    AlgebraicVector workingPlaneNormal = null;
                    if ( useWorkingPlane && (workingPlaneAxis != null ) )
                    	workingPlaneNormal = workingPlaneAxis .getOffset();
                    previewStrut .startRendering( symmetryController, point, workingPlaneNormal );
                }
            }

			protected void dragFinished( Manifestation target, boolean b )
            {
                previewStrut .finishPreview( documentModel );
            }
        } );

        // trackball to adjust the preview strut (when it is rendered)
        previewStrutRoll = new LeftMouseDragAdapter( new Trackball()
        {
            protected void trackballRolled( Quat4d roll )
            {
                previewStrut .trackballRolled( roll );
            }
        } );
        
        // working plane drag events to adjust the preview strut (when it is rendered)
        previewStrutPlanarDrag = new LeftMouseDragAdapter( new MouseToolDefault()
        {
			@Override
			public void mouseDragged( MouseEvent e )
			{
				Point3d imagePt = new Point3d();
				Point3d eyePt = new Point3d();
				imageCaptureViewer .pickPoint( e, imagePt, eyePt );
				previewStrut .workingPlaneDrag( imagePt, eyePt );
			}
        } );
        
        this .aggregate = new MouseToolDefault() {
			
			@Override
			public void detach( Component canvas )
			{
				previewStrutLength .detach( canvas );
				previewStrutStart .detach( canvas );
				previewStrutRoll .detach( canvas );
				previewStrutPlanarDrag .detach( canvas );
			}
			
			@Override
			public void attach( Component canvas )
			{
				previewStrutLength .attach( canvas );
				previewStrutStart .attach( canvas );
				previewStrutRoll .attach( canvas );
				previewStrutPlanarDrag .attach( canvas );
			}
		};
	}

    @Override
	public MouseTool getMouseTool()
    {
		return this .aggregate;
	}

	@Override
	public String getProperty( String name )
	{
		switch ( name ) {
		
		case "useWorkingPlane":
            return Boolean .toString( this .useWorkingPlane );

		case "workingPlaneDefined":
            return Boolean .toString( this .workingPlaneAxis != null );

		case "useGraphicalViews":
            return Boolean.toString( this .useGraphicalViews );

		case "showStrutScales":
            return Boolean.toString( this .showStrutScales );

		default:
			return super .getProperty( name );
		}
	}
	
	@Override
    public void setProperty( String name, Object value )
    {
		switch ( name ) {

		case "useGraphicalViews":
            this .useGraphicalViews = "true" .equals( value );
            properties().firePropertyChange( name, false, this .useGraphicalViews );
            return;

		case "showStrutScales":
            boolean old = showStrutScales;
            this .showStrutScales = "true" .equals( value );
            properties().firePropertyChange( "showStrutScales", old, this.showStrutScales );
            return;

		default:
	        super.setProperty( name, value );
		}
    }

	@Override
    public void doAction( String action, ActionEvent e ) throws Exception
    {
		switch ( action ) {
		
		case "toggleWorkingPlane":
            useWorkingPlane = ! useWorkingPlane;
			break;

		case "toggleOrbitViews":
        	{
        		boolean old = useGraphicalViews;
            	useGraphicalViews = ! old;
            	properties().firePropertyChange( "useGraphicalViews", old, this.useGraphicalViews );
        	}
        	break;

		case "toggleStrutScales":
			{
				boolean old = showStrutScales;
				showStrutScales = ! old;
				properties().firePropertyChange( "showStrutScales", old, this.showStrutScales );
			}
			break;

		default:
			super .doAction( action, e );
		}
    }

	@Override
	public boolean doManifestationAction( Manifestation pickedManifestation, String action )
	{
        Construction singleConstruction = null;
        if ( pickedManifestation != null )
            singleConstruction = (Construction) pickedManifestation .getConstructions().next();
		switch ( action ) {
		
	    case "setWorkingPlaneAxis":
	    	this .workingPlaneAxis = (Segment) singleConstruction;
	    	this .properties() .firePropertyChange( "workingPlaneDefined", false, true );
			return true;

	    case "setWorkingPlane":
	    	this .workingPlaneAxis = this .documentModel .getPlaneAxis( (Polygon) singleConstruction );
	    	this .properties() .firePropertyChange( "workingPlaneDefined", false, true );
			return true;

        case "setBuildOrbitAndLength": {
            AlgebraicVector offset = ((Strut) pickedManifestation) .getOffset();
            Axis zone = symmetryController .getZone( offset );
            Direction orbit = zone .getOrbit();
            AlgebraicNumber length = zone .getLength( offset );
			symmetryController .availableController .doAction( "enableDirection." + orbit .getName(), null );
	        symmetryController .buildController .doAction( "setSingleDirection." + orbit .getName(), null );
	        LengthController lmodel = (LengthController) symmetryController .buildController .getSubController( "currentLength" );
	        lmodel .setActualLength( length );
        	}
        	return true;
            
		default:
			return false;
		}
	}

	@Override
	public boolean[] enableCommands( Manifestation pickedManifestation, String[] menu, boolean[] result )
	{
		for ( int i = 0; i < menu.length; i++ ) {
            String menuItem = menu[i];
            switch ( menuItem ) {

			case "setWorkingPlaneAxis":
			case "setBuildOrbitAndLength":
            	result[ i ] = pickedManifestation instanceof Strut;
				break;

			case "setWorkingPlane":
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
}
