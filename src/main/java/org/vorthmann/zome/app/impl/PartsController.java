
//(c) Copyright 2013, Scott Vorthmann.

package org.vorthmann.zome.app.impl;

import org.vorthmann.ui.DefaultController;

import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.math.Polyhedron;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.model.Connector;
import com.vzome.core.model.Manifestation;
import com.vzome.core.render.Color;
import com.vzome.core.render.OrbitSource;
import com.vzome.core.render.RenderedManifestation;
import com.vzome.core.render.RenderingChanges;

public class PartsController extends DefaultController implements RenderingChanges
{    
    private String getStrutData( Polyhedron poly, AlgebraicNumber length, OrbitSource orbits )
    {
        Direction orbit = poly .getOrbit();
        StringBuffer buf = new StringBuffer();
        orbit .getLengthExpression( buf, length );
        Color color = orbits .getColor( orbit );
        return orbit .getName() + ":" + color .getRGB() + ":" + buf;
    }

    public void reset()
    {}

    public void manifestationAdded( RenderedManifestation rendered )
    {
        Manifestation m = rendered .getManifestation();
        Polyhedron poly = rendered .getShape();
        if ( poly == null )
            return;
        AlgebraicNumber length = poly .getLength();
        if ( length != null )
            properties() .firePropertyChange( "addStrut-" + getStrutData( poly, length, rendered .getOrbitSource() ), null, poly );
        else if ( m instanceof Connector )
            properties() .firePropertyChange( "addBall", null, null );
    }

    public void manifestationRemoved( RenderedManifestation rendered )
    {
        Manifestation m = rendered .getManifestation();
        if ( m instanceof Connector )
            properties() .firePropertyChange( "removeBall", null, null );
        else
        {
            Polyhedron poly = rendered .getShape();
            if ( poly != null )
            {
                // now emit prop changes for the BOM table panel
                AlgebraicNumber length = poly .getLength();
                if ( length != null )
                    properties() .firePropertyChange( "removeStrut-" + getStrutData( poly, length, rendered .getOrbitSource() ), null, poly );
            }
        }
    }

    public void manifestationSwitched( RenderedManifestation from, RenderedManifestation to )
    {}

    public void glowChanged( RenderedManifestation manifestation )
    {}

    public void colorChanged( RenderedManifestation manifestation )
    {}

    public void locationChanged( RenderedManifestation manifestation )
    {}

    public void orientationChanged( RenderedManifestation manifestation )
    {}

    public void shapeChanged( RenderedManifestation manifestation )
    {}

    public void enableFrameLabels()
    {}

    public void disableFrameLabels()
    {}
}
