package org.vorthmann.zome.app.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vorthmann.j3d.Platform;
import org.vorthmann.ui.Controller;
import org.vorthmann.ui.DefaultController;
import org.vorthmann.zome.ui.ApplicationUI;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.commands.Command.Failure;
import com.vzome.core.commands.Command.FailureChannel;
import com.vzome.core.editor.DocumentModel;
import com.vzome.core.exporters.Exporter3d;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.model.Connector;
import com.vzome.core.model.Strut;
import com.vzome.core.render.Colors;
import com.vzome.core.render.RenderedManifestation;
import com.vzome.core.render.RenderedModel;
import com.vzome.core.viewing.Lights;
import com.vzome.desktop.controller.RenderingViewer;

public class ApplicationController extends DefaultController
{
	private static final Logger logger = Logger.getLogger( "org.vorthmann.zome.controller" );

    private final Map<String, DocumentController> docControllers = new HashMap<>();
	
	private final ActionListener ui;
    
    private final Properties userPreferences = new Properties();
    
    private final Properties properties = new Properties();
    
    private final Map<Symmetry, RenderedModel> mSymmetryModels = new HashMap<>();

    private RenderingViewer.Factory rvFactory;

    private final com.vzome.core.editor.Application modelApp;

	private final File preferencesFile;

    private int lastUntitled = 0;
    
	public ApplicationController( ActionListener ui, Properties commandLineArgs )
    {
		super();
		
        long starttime = System.currentTimeMillis();

        if ( logger .isLoggable( Level .INFO ) )
            logger .info( "ApplicationController .initialize() starting" );

		this.ui = ui;
        
        File prefsFolder = Platform .getPreferencesFolder();        
        File prefsFile = new File( prefsFolder, "vZome.preferences" );
        if ( ! prefsFile .exists() ) {
            prefsFolder = new File( System.getProperty( "user.home" ) );
            prefsFile = new File( prefsFolder, "vZome.preferences" );
        }
        if ( ! prefsFile .exists() ) {
            prefsFile = new File( prefsFolder, ".vZome.prefs" );
        }
        this.preferencesFile = prefsFile;
        if ( ! prefsFile .exists() ) {
        	logger .config( "Used default preferences." );
        } else {
            try {
                InputStream in = new FileInputStream( prefsFile );
                userPreferences .load( in );
    			logger .config( "User Preferences loaded from " + prefsFile .getAbsolutePath() );
            } catch ( Throwable t ) {
    			logger .severe( "problem reading user preferences: " + t.getMessage() );
            }
        }
        
		Properties defaults = new Properties();
        String defaultRsrc = "org/vorthmann/zome/app/defaultPrefs.properties";
        try {
            ClassLoader cl = ApplicationUI.class.getClassLoader();
            InputStream in = cl.getResourceAsStream( defaultRsrc );
            if ( in != null )
            	defaults .load( in ); // override the core defaults
        } catch ( IOException ioe ) {
        	logger.severe( "problem reading default preferences: " + defaultRsrc );
        }

        // last-wins, so getProperty() will show command-line args overriding user prefs, which override built-in defaults
        properties .putAll( defaults );
        properties .putAll( userPreferences );
        properties .putAll( commandLineArgs );

        final FailureChannel failures = new FailureChannel()
        {	
			@Override
			public void reportFailure( Failure f )
			{
	            mErrors.reportError( USER_ERROR_CODE, new Object[] { f } );
			}
		};
        modelApp = new com.vzome.core.editor.Application( true, failures, properties );
        
        Colors colors = modelApp .getColors();
        
        {
        	boolean useEmissiveColor = ! propertyIsTrue( "no.glowing.selection" );
            // need this set up before we do any loadModel
            String factoryName = getProperty( "RenderingViewer.Factory.class" );
            if ( factoryName == null )
                factoryName = "org.vorthmann.zome.render.java3d.Java3dFactory";
            try {
                Class<?> factoryClass = Class.forName( factoryName );
                Constructor<?> constructor = factoryClass .getConstructor( new Class<?>[] { Colors.class, Boolean.class } );
                rvFactory = (RenderingViewer.Factory) constructor.newInstance( new Object[] { colors, useEmissiveColor } );
            } catch ( Exception e ) {
                mErrors.reportError( "Unable to instantiate RenderingViewer.Factory class: " + factoryName, new Object[] {} );
                System.exit( 0 );
            }
        }

        RenderedModel model;
        
        AlgebraicField field = modelApp .getField( "golden" );
        Symmetry symmetry = field .getSymmetry( "icosahedral" );
        {
            if ( propertyIsTrue( "rzome.trackball" ) )
                model = loadModelPanels( "org/vorthmann/zome/app/rZomeTrackball-vef.vZome" );
            else if ( userHasEntitlement( "developer.extras" ) )
            	model = loadModelPanels( "org/vorthmann/zome/app/icosahedral-developer.vZome" );
            else
                model = loadModelPanels( "org/vorthmann/zome/app/icosahedral-vef.vZome" );
            mSymmetryModels.put( symmetry, model );
        }
        symmetry = field .getSymmetry( "octahedral" );
        {
            // this has to happen after the OctahedralSymmetry constructor, which registers with the field
            model = loadModelPanels( "org/vorthmann/zome/app/octahedral-vef.vZome" );
            mSymmetryModels.put( symmetry, model );
        }

        field = modelApp .getField( "snubDodec" );
        symmetry = field .getSymmetry( "icosahedral" );
        {
            model = loadModelPanels( "org/vorthmann/zome/app/icosahedral-vef.vZome" );
            mSymmetryModels.put( symmetry, model );
        }

        field = modelApp .getField( "rootTwo" );
        symmetry = field .getSymmetry( "octahedral" );
        {
            model = loadModelPanels( "org/vorthmann/zome/app/octahedral-vef.vZome" );
            mSymmetryModels.put( symmetry, model );
            symmetry = field .getSymmetry( "synestructics" );
            mSymmetryModels.put( symmetry, model );
        }

        field = modelApp .getField( "rootThree" );
        symmetry = field .getSymmetry( "octahedral" );
        {
            // yes, reusing the model from above
            mSymmetryModels.put( symmetry, model );
        }
        symmetry = field .getSymmetry( "dodecagonal" );
        {
            model = loadModelPanels( "org/vorthmann/zome/app/dodecagonal.vZome" );
            mSymmetryModels.put( symmetry, model );
        }

        // addStyle( new ModeledShapes( "pentagonal", "pentagonal prismatic", DecagonSymmetry.INSTANCE ) );

        if ( propertyIsTrue( "enable.heptagon.field" ) )
        {
            field = modelApp .getField( "heptagon" );
            symmetry = field .getSymmetry( "octahedral" );
            mSymmetryModels.put( symmetry, model );
        }

        long endtime = System.currentTimeMillis();
        if ( logger .isLoggable( Level .INFO ) )
            logger .log(Level.INFO, "ApplicationController initialization in milliseconds: {0}", ( endtime - starttime ));
   	}
	
	@Override
	public void doAction( String action, ActionEvent event )
	{
		try {
			if ( action .equals( "showAbout" ) 
             || action .equals( "openURL" ) 
			 || action .equals( "quit" ) 
                    )
            {
	        	this .ui .actionPerformed( event );
				return;
            }
			
			if( "launch".equals(action) ) {
	            String sawWelcome = userPreferences .getProperty( "saw.welcome" );
	            if ( sawWelcome == null )
	            {
	                String welcome = properties .getProperty( "welcome" );
	                doAction( "openResource-" + welcome, null );
	                userPreferences .setProperty( "saw.welcome", "true" );
	                FileWriter writer;
	                try {
	                    writer = new FileWriter( preferencesFile );
	                    userPreferences .store( writer, "" );
	                    writer .close();
	                } catch ( IOException e ) {
	                    logger.fine(e.toString());
	                }
	                return;
	            }
                action = "new";
            }

            if ( "new" .equals( action ) ) {
            	String fieldName = properties .getProperty( "default.field" );
                action = "new-" + fieldName;
            }

            if ( action .startsWith( "new-" ) )
            {
                String fieldName = action .substring( "new-" .length() );
                File prototype = new File( Platform .getPreferencesFolder(), "Prototypes/" + fieldName + ".vZome" );
                if ( prototype .exists() ) {
                    logger.log(Level.CONFIG, "Loading default template from {0}", prototype.getCanonicalPath());
                    doFileAction( "newFromTemplate", prototype );
                }
                else
                {
                    // creating a new Document
                    Properties docProps = new Properties();
                    docProps .setProperty( "new.document", "true" );
                    if ( logger .isLoggable( Level .INFO ) )
                        logger .info( "about to create a document" );
                    DocumentModel document = modelApp .createDocument( fieldName );
                    if ( logger .isLoggable( Level .INFO ) )
                        logger .info( "created the document" );
                    String title = "Untitled " + ++lastUntitled;
                    docProps .setProperty( "window.title", title );
                    docProps .setProperty( "edition", this .properties .getProperty( "edition" ) );
                    docProps .setProperty( "version", this .properties .getProperty( "version" ) );
                    docProps .setProperty( "buildNumber", this .properties .getProperty( "buildNumber" ) );
                    docProps .setProperty( "gitCommit", this .properties .getProperty( "gitCommit" ) );
                    DocumentController newest = new DocumentController( document, this, docProps );
                    if ( logger .isLoggable( Level .INFO ) )
                        logger .info( "Created the DocumentController" );
                    newDocumentController( title, newest );
                }
            }
            else if ( action .startsWith( "openResource-" ) )
            {
                Properties docProps = new Properties();
                docProps .setProperty( "reader.preview", "true" );
                String path = action .substring( "openResource-" .length() );
                docProps .setProperty( "window.title", path );
                ClassLoader cl = Thread .currentThread() .getContextClassLoader();
                InputStream bytes = cl .getResourceAsStream( path );
                loadDocumentController( path, bytes, docProps );
            }
            else if ( action .startsWith( "openURL-" ) )
            {
                Properties docProps = new Properties();
                docProps .setProperty( "as.template", "true" );
                String path = action .substring( "openURL-" .length() );
                docProps .setProperty( "window.title", path );
                if ( path .toLowerCase() .endsWith( ".vzome" ) ) {
                    URI uri = new URI( path );
                    URL url = uri .toURL();
                    InputStream bytes = url .openStream();
                    loadDocumentController( path, bytes, docProps );
                }
            }
            else 
            {
                this .mErrors .reportError( UNKNOWN_ACTION, new Object[]{ action } );
			}
		} catch ( Exception e ) {
        	this .mErrors .reportError( UNKNOWN_ERROR_CODE, new Object[]{ e } );
		}
	}

	@Override
    public void doFileAction( String command, File file )
    {
        if ( file != null )
        {
    		Properties docProps = new Properties();
            String path = file .getAbsolutePath();
			docProps .setProperty( "window.title", path );
        	switch ( command ) {

        	case "open":
        		docProps .setProperty( "window.file", path );
				break;

        	case "newFromTemplate":
        		String title = "Untitled " + ++lastUntitled;
        		docProps .setProperty( "window.title", title ); // override the default above
        		docProps .setProperty( "as.template", "true" ); // don't set window.file!
				break;

        	case "openDeferringRedo":
        		docProps .setProperty( "open.undone", "true" );
        		docProps .setProperty( "window.file", path );
				break;

			default:
	        	this .mErrors .reportError( UNKNOWN_ACTION, new Object[]{ command } );
				return;
			}
            try {
                InputStream bytes = new FileInputStream( file );
                loadDocumentController( path, bytes, docProps );
			} catch ( Exception e ) {
	        	this .mErrors .reportError( UNKNOWN_ERROR_CODE, new Object[]{ e } );
			}
        }
    }
	
	private void loadDocumentController( final String name, final InputStream bytes, final Properties properties ) throws Exception
	{
		DocumentModel document = modelApp .loadDocument( bytes );
		DocumentController newest = new DocumentController( document, ApplicationController.this, properties );
		newDocumentController( name, newest );
	}

    RenderingViewer.Factory getJ3dFactory()
    {
        return rvFactory;
    }
    
	@Override
    public boolean userHasEntitlement( String propName )
    {
		switch ( propName ) {

		case "save.files":
            return getProperty( "licensed.user" ) != null;

		case "all.tools":
            return propertyIsTrue( "entitlement.all.tools" );

		case "developer.extras":
            return getProperty( "vZomeDeveloper" ) != null;

		default:
	        // TODO make this work more like developer.extras
	        return propertyIsTrue( "entitlement." + propName );
	        // this IS the backstop controller, so no purpose in calling super
		}
    }

	@Override
    public final String getProperty( String propName )
    {
		switch ( propName ) {
		
		case "formatIsSupported":
            return "true";

		case "untitled.title":
            return "Untitled " + ++lastUntitled;

		case "coreVersion":
            return this .modelApp .getCoreVersion();

		default:
			if ( propName .startsWith( "field.label." ) )
			{
				String fieldName = propName .substring( "field.label." .length() );
	            // TODO implement AlgebraicField.getLabel()
	            switch ( fieldName ) {

	            case "golden":
					return "Zome (Golden)";

	            case "rootTwo":
					return "\u221A2";

	            case "rootThree":
					return "\u221A3";

	            case "heptagon":
					return "Heptagon";

	            case "snubDodec":
					return "Snub Dodec";

				default:
					return fieldName;
				}
			}
			if ( propName .startsWith( "enable." ) && propName .endsWith( ".field" ) )
			{
				String fieldName = propName .substring( "enable." .length() );
				fieldName = fieldName .substring( 0, fieldName .lastIndexOf( ".field" ) );
				
				switch ( fieldName ) {

				case "golden":
					return "false"; // this one is forcibly enabled by the menu, and we don't want it listed twice

				case "dodecagon":
					return "false"; // this is just an alias for rootThree

				default:
					// fall through
				}

				if ( getProperty( "vZomeDeveloper" ) != null )
					return "true"; // developer sees all available fields
				
				switch ( fieldName ) {

				case "rootTwo":
				case "rootThree":
					return "true"; // these two are enabled for everyone

				default:
					// fall through, see if it is explicitly set
				}
			}
	        return properties .getProperty( propName );
		}
    }

    @Override
    public Controller getSubController( final String name )
    {
    	return docControllers .get( name );
    }

    private void newDocumentController( final String name, final DocumentController newest )
    {
    	this .registerDocumentController( name, newest );
        // trigger window creation in the UI
		this .properties() .firePropertyChange( "newDocument", null, newest );
    }

    private void registerDocumentController( final String name, final DocumentController newest )
    {
        this .docControllers .put( name, newest );
        newest .addPropertyListener( new PropertyChangeListener()
        {
			@Override
			public void propertyChange( PropertyChangeEvent evt )
			{
				switch ( evt .getPropertyName() ) {

				case "name":
			        docControllers .remove( name );
			        // important to re-register under the new name, AND get a new listener, or removes won't work
			        newest .removePropertyListener( this );
			        registerDocumentController( (String) evt .getNewValue(), newest );
					break;

				case "visible":
					if ( Boolean.FALSE .equals( evt .getNewValue() ) ) {
						docControllers .remove( name );
						if ( docControllers .isEmpty() )
							// closed the last window, so we're exiting
							System .exit( 0 );
					}
					break;

				default:
					break;
				}
			}
		});
    }

    private RenderedModel loadModelPanels( String path )
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream bytes = cl.getResourceAsStream( path );

        try {
    		DocumentModel document = this .modelApp .loadDocument( bytes );
    		// a RenderedModel that only creates panels
    		document .setRenderedModel( new RenderedModel( document .getField(), true ) 	 	 
    		{
				@Override
    			protected void resetAttributes( RenderedManifestation rm, 	 	 
    					boolean justShape, Strut strut ) {} 	 	 

				@Override
    			protected void resetAttributes(RenderedManifestation rm, 	 	 
    					boolean justShape, Connector m) {} 	 	 
    		} .withColorPanels( false ) ); 
    		document .finishLoading( false, false );
            return document .getRenderedModel();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public Colors getColors()
    {
        return this .modelApp .getColors();
    }

    public Exporter3d getExporter( String format )
    {
        return this .modelApp .getExporter( format );
    }
    
    // public RenderingViewer.Factory getRenderingViewerFactory()
    // {
    // return mRVFactory;
    // }

    public RenderedModel getSymmetryModel( Symmetry symm )
    {
        return mSymmetryModels.get( symm );
    }
    
	@Override
    public String[] getCommandList( String listName )
    {
        if ( listName .startsWith( "fields" ) )
        {
            Set<String> names = modelApp .getFieldNames();
            SortedSet<String> sorted = new TreeSet<String>( names );
            return sorted .toArray( new String[]{} );
        }
        else if ( listName .startsWith( "symmetries." ) )
        {
            String fieldName = listName.substring( 11 );
            AlgebraicField field = modelApp .getField( fieldName );
            Symmetry[] symms = field.getSymmetries();
            String[] result = new String[symms.length];
            for ( int i = 0; i < symms.length; i++ )
                result[i] = symms[i].getName();
            return result;
        }
        return new String[0];
    }

    public Lights getLights()
    {
        return modelApp .getLights();
    }

}
