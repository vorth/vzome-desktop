package org.vorthmann.zome.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.vorthmann.j3d.J3dComponentFactory;
import org.vorthmann.ui.Controller;

public class ModelPanel extends JPanel implements PropertyChangeListener
{
	private static final String TOOLTIP_PREFIX = "<html><b>";
	private static final String TOOLTIP_SUFFIX = "</b><br><br><p>Right-click to configure this tool.</p></html>";

	private Component monocularCanvas; //, leftEyeCanvas, rightEyeCanvas;
    private MouseListener monocularClicks; //, leftEyeClicks, rightEyeClicks;
    private final JToolBar oldToolBar, firstToolbar, secondToolbar, bookmarkBar; // TODO these don't need to be fields
    private final JScrollPane firstScroller, secondScroller, bookmarkScroller;
    private final boolean isEditor;
	private final Controller controller, view;
	private final JPanel mMonocularPanel;
	private final ToolConfigDialog toolConfigDialog;
	private Map<String, AbstractButton> toolCreationButtons = new HashMap<String, AbstractButton>();
	private final ControlActions enabler;

	public ModelPanel( Controller controller, ControlActions enabler, boolean isEditor, boolean fullPower )
	{
		super( new BorderLayout() );
		this .controller = controller;
		this.enabler = enabler;
        this .view = controller .getSubController( "viewPlatform" );
        this .isEditor = isEditor;
        
        controller .addPropertyListener( this );

        JPanel monoStereoPlusToolbar = new JPanel();
        monoStereoPlusToolbar .setLayout( new BorderLayout() );
        this .add( monoStereoPlusToolbar, BorderLayout.CENTER );

        JPanel monoStereoPanel = new JPanel();
        monoStereoPlusToolbar .add( monoStereoPanel, BorderLayout.CENTER );
		CardLayout monoStereoCardLayout = new CardLayout();
		monoStereoPanel .setLayout( monoStereoCardLayout );
        boolean showStereo =  "true" .equals( view .getProperty( "stereo" ) );

        mMonocularPanel = new JPanel( new BorderLayout() );
        {
            mMonocularPanel .setPreferredSize( new Dimension( 2000, 2000 ) );
        }
        monoStereoPanel .add( mMonocularPanel, "mono" );
//        JPanel stereoPanel = new JPanel();
//        {
//            GridLayout grid = new GridLayout( 1, 2 );
//            stereoPanel .setLayout( grid );
//            leftEyeCanvas = ( (J3dComponentFactory) controller ) .createJ3dComponent( "mainViewer-leftEye" );
//            stereoPanel .add( leftEyeCanvas );
//            rightEyeCanvas = ( (J3dComponentFactory) controller ) .createJ3dComponent( "mainViewer-rightEye" );
//            stereoPanel .add( rightEyeCanvas );
//        }
//        monoStereoPanel .add( stereoPanel, "stereo" );
//        if ( showStereo )
//            monoStereoCardLayout .show( monoStereoPanel, "stereo" );
//        else
            monoStereoCardLayout .show( monoStereoPanel, "mono" );
        view .addPropertyListener( new PropertyChangeListener()
        {
        	@Override
        	public void propertyChange( PropertyChangeEvent chg )
        	{
        		if ( "stereo" .equals( chg .getPropertyName() ) )
        			if ( ((Boolean) chg .getNewValue()) )
        				monoStereoCardLayout .show( monoStereoPanel, "stereo" );
        			else
        				monoStereoCardLayout .show( monoStereoPanel, "mono" );
        	}
        } );

        this .toolConfigDialog = new ToolConfigDialog( (JFrame) this.getParent() );

        if ( isEditor )
        {
            if ( ! controller .propertyIsTrue( "no.toolbar" ) )
            {
            	// -------------------- Create the dynamic toolbar
            	
                boolean hasOldToolBar = controller .propertyIsTrue( "original.tools" );

                this .firstToolbar = new JToolBar();
                this .firstToolbar .setFloatable( false );
                this .firstToolbar .setOrientation( JToolBar.HORIZONTAL );
//                this .firstToolbar .setToolTipText( "Click on objects to select them, and enable creation of new tools accordingly." );
                this .firstScroller = new JScrollPane( this .firstToolbar, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
                this .firstScroller .setBorder( null );
                this .add( this .firstScroller, BorderLayout .NORTH );

                this .bookmarkBar = new JToolBar();
                this .bookmarkBar .setFloatable( false );
                this .bookmarkBar .setOrientation( JToolBar.VERTICAL );
                this .bookmarkBar .setToolTipText( "Selection bookmarks" );
                this .bookmarkScroller = new JScrollPane( this .bookmarkBar, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
                this .bookmarkScroller .setBorder( null );
                monoStereoPlusToolbar .add( this .bookmarkScroller, BorderLayout .LINE_START );

                final Controller toolsController = controller .getSubController( "tools" );

                AbstractButton button;

                if ( controller .propertyIsTrue( "supports.symmetry.icosahedral" ) ) {
                    button = newToolButton( toolsController, "icosahedral", "Create an icosahedral symmetry tool",
                		"<p>" +
                        		"Each tool produces up to 59 copies of the input<br>" +
                        		"selection, using the rotation symmetries of an<br>" +
                        		"icosahedron.  To create a tool, select a single<br>" +
                        		"ball that defines the center of symmetry.<br>" +
                        		"<br>" +
                        		"Combine with a point reflection tool to achieve<br>" +
                        		"all 120 symmetries of the icosahedron, including<br>" +
                        		"reflections.<br>" +
                		"</p>" );
                    firstToolbar .add( button );
                	button = newToolButton( toolsController, "octahedral", "Create an octahedral symmetry tool",
                		"<p>" +
                        		"Each tool produces up to 23 copies of the input<br>" +
                        		"selection, using the rotation symmetries of a<br>" +
                        		"cube or octahedron.  To create a tool, select a<br>" +
                        		"ball that defines the center of symmetry, and<br>" +
                        		"a single blue or green strut, defining one of<br>" +
                        		"five possible orientations for the symmetry.<br>" +
                        		"<br>" +
                        		"Combine with a point reflection tool to achieve<br>" +
                        		"all 48 symmetries of the octahedron, including<br>" +
                        		"reflections.<br>" +
                		"</p>" );
                    firstToolbar .add( button );
                	button = newToolButton( toolsController, "tetrahedral", "Create a tetrahedral symmetry tool",
                		"<p>" +
                        		"Each tool produces up to 11 copies of the input<br>" +
                        		"selection, using the rotation symmetries of a<br>" +
                        		"tetrahedron.  To create a tool, select a ball<br>" +
                        		"that defines the center of symmetry, and a single<br>" +
                        		"blue or green strut, defining one of five<br>" +
                        		"possible orientations for the symmetry.<br>" +
                        		"<br>" +
                        		"Combine with a point reflection tool to achieve<br>" +
                        		"all 24 symmetries of the tetrahedron, including<br>" +
                        		"reflections.<br>" +
                		"</p>" );
                    firstToolbar .add( button );
                } else {
                	button = newToolButton( toolsController, "octahedral", "Create an octahedral symmetry tool",
                		"<p>" +
                        		"Each tool produces up to 23 copies of the input<br>" +
                        		"selection, using the rotation symmetries of a<br>" +
                        		"cube or octahedron.  To create a tool, select a<br>" +
                        		"ball that defines the center of symmetry.<br>" +
                        		"<br>" +
                        		"Combine with a point reflection tool to achieve<br>" +
                        		"all 48 symmetries of the octahedron, including<br>" +
                        		"reflections.<br>" +
                		"</p>" );
                    firstToolbar .add( button );
                	button = newToolButton( toolsController, "tetrahedral", "Create a tetrahedral symmetry tool",
                		"<p>" +
                        		"Each tool produces up to 11 copies of the input<br>" +
                        		"selection, using the rotation symmetries of a<br>" +
                        		"tetrahedron.  To create a tool, select a ball<br>" +
                        		"that defines the center of symmetry.<br>" +
                        		"<br>" +
                        		"Combine with a point reflection tool to achieve<br>" +
                        		"all 24 symmetries of the tetrahedron, including<br>" +
                        		"reflections.<br>" +
                		"</p>" );
                    firstToolbar .add( button );
                }
                button = newToolButton( toolsController, "point reflection", "Create a point reflection tool",
                		"<p>" +
                        		"Each tool duplicates the selection by reflecting<br>" +
                        		"each point through the defined center.  To create a<br>" +
                        		"tool, select a single ball that defines that center.<br>" +
                		"</p>" );
                firstToolbar .add( button );
                button = newToolButton( toolsController, "mirror", "Create a mirror reflection tool",
                		"<p>" +
                        		"Each tool duplicates the selection by reflecting<br>" +
                        		"each object in a mirror plane.  To create a<br>" +
                        		"tool, define the mirror plane by selecting a single<br>" +
                        		"panel, or by selecting a strut orthogonal to the<br>" +
                        		"plane and a ball lying in the plane.<br>" +
                		"</p>" );
                firstToolbar .add( button );
                button = newToolButton( toolsController, "axial symmetry", "Create a rotational symmetry tool",
                		"<p>" +
                        		"Each tool creates enough copies of the selected objects to<br>" +
                        		"create rotational symmetry around an axis.  To create a tool,<br>" +
                        		"select a strut that defines that axis,  You can also define<br>" +
                        		"the direction and center independently, by selecting a ball<br>" +
                        		"for the center and a strut for the axis.  Note: not all struts<br>" +
                        		"correspond to rotational symmetries!<br>" +
                        		"<br>" +
                        		"Combine with a point reflection or mirror reflection tool to<br>" +
                        		"achieve more symmetries.<br>" +
                		"</p>" );
                firstToolbar .add( button );
                
                firstToolbar .addSeparator();
                
                button = newToolButton( toolsController, "scaling", "Create a scaling tool",
                		"<p>" +
                        		"Each tool enlarges or shrinks the selected objects,<br>" +
                        		"relative to a central point.  To create a tool,<br>" +
                        		"select a ball representing the central point, and<br>" +
                        		"two struts from the same orbit (color) with different<br>" +
                        		"sizes.<br>" +
                        		"<br>" +
                        		"The selection order matters.  First select a strut<br>" +
                        		"that you want to enlarge or shrink, then select a<br>" +
                        		"strut that has the desired target size.<br>" +
                		"</p>" );
                firstToolbar .add( button );
                button = newToolButton( toolsController, "rotation", "Create a rotation tool",
                		"<p>" +
                        		"Each tool rotates the selected objects around an axis<br>" +
                        		"of symmetry.  To create a tool, select a strut that<br>" +
                        		"defines that axis.  You can also define the direction<br>" +
                        		"and center independently, by selecting a ball for the<br>" +
                        		"center and a strut for the axis.  Note: not all struts<br>" +
                        		"correspond to rotational symmetries!<br>" +
                        		"<br>" +
                        		"The direction of rotation depends on the strut<br>" +
                        		"orientation, which is hard to discover, but easy to<br>" +
                        		"control, by dragging out a new strut.<br>" +
                        		"<br>" +
                        		"By default, the input selection will be moved to the new,<br>" +
                        		"rotated orientation.  After creating a tool, you can<br>" +
                        		"right-click to configure the tool to create a copy, instead.<br>" +
                		"</p>" );
                firstToolbar .add( button );
                button = newToolButton( toolsController, "translation", "Create a translation tool",
                		"<p>" +
                        		"Each tool moves the selected objects to a new location.<br>" +
                        		"To create a tool, select two balls that are separated by<br>" +
                        		"your desired translation offset.  Order of selection<br>" +
                        		"matters: the first ball selected is the \"from\" location,<br>" +
                        		"and the second is the \"to\" location.<br>" +
                        		"<br>" +
                        		"By default, the input selection will be moved to the new<br>" +
                        		"location.  If you want to copy rather than move, you can<br>" +
                        		"right-click after creating the tool, to configure it.<br>" +
                		"</p>" );
                firstToolbar .add( button );
                
                firstToolbar .addSeparator();

                if ( controller .propertyIsTrue( "supports.symmetry.icosahedral" ) ) {
	                button = newToolButton( toolsController, "redsquash1", "Create a weak red squash tool",
	                		"<p>" +
	                        		"Each tool applies a \"squash\" transformation to the<br>" +
	                        		"selected objects, compressing along a red axis.  To create<br>" +
	                        		"a tool, select a ball as the center of the mapping, and a<br>" +
	                        		"red strut as the direction of the compression.  The ball and<br>" +
	                        		"strut need not be collinear.<br>" +
	                        		"<br>" +
	                        		"The mapping comes from the usual Zome projection of the<br>" +
	                        		"120-cell.  It is the mapping that transforms the central,<br>" +
	                        		"blue dodecahedron into the compressed form in the next<br>" +
	                        		"layer outward.<br>" +
	                        		"<br>" +
	                        		"By default, the input selection will be removed, and replaced<br>" +
	                        		"with the squashed equivalent.  If you want to keep the inputs,<br>" +
	                        		"you can right-click after creating the tool, to configure it.<br>" +
	                		"</p>" );
	                firstToolbar .add( button );
	                button = newToolButton( toolsController, "redstretch1", "Create a weak red stretch tool",
	                		"<p>" +
	                        		"Each tool applies a \"stretch\" transformation to the<br>" +
	                        		"selected objects, stretching along a red axis.  To create<br>" +
	                        		"a tool, select a ball as the center of the mapping, and a<br>" +
	                        		"red strut as the direction of the stretch.  The ball and<br>" +
	                        		"strut need not be collinear.<br>" +
	                        		"<br>" +
	                        		"The mapping comes from the usual Zome projection of the<br>" +
	                        		"120-cell.  It is the inverse of the mapping that transforms<br>" +
	                        		"the central, blue dodecahedron into the compressed form in<br>" +
	                        		"the next layer outward.<br>" +
	                        		"<br>" +
	                        		"By default, the input selection will be removed, and replaced<br>" +
	                        		"with the stretched equivalent.  If you want to keep the inputs,<br>" +
	                        		"you can right-click after creating the tool, to configure it.<br>" +
	                		"</p>" );
	                firstToolbar .add( button );
	                
	                button = newToolButton( toolsController, "yellowsquash", "Create a yellow squash tool",
	                		"<p>" +
	                        		"Each tool applies a \"squash\" transformation to the<br>" +
	                        		"selected objects, compressing along a yellow axis.  To create<br>" +
	                        		"a tool, select a ball as the center of the mapping, and a<br>" +
	                        		"yellow strut as the direction of the compression.  The ball and<br>" +
	                        		"strut need not be collinear.<br>" +
	                        		"<br>" +
	                        		"The mapping comes from the usual Zome projection of the<br>" +
	                        		"120-cell.  It is the mapping that transforms the central,<br>" +
	                        		"blue dodecahedron into the compressed form along a yellow axis.<br>" +
	                        		"<br>" +
	                        		"By default, the input selection will be removed, and replaced<br>" +
	                        		"with the squashed equivalent.  If you want to keep the inputs,<br>" +
	                        		"you can right-click after creating the tool, to configure it.<br>" +
	                		"</p>" );
	                firstToolbar .add( button );
	                button = newToolButton( toolsController, "yellowstretch", "Create a yellow stretch tool",
	                		"<p>" +
	                        		"Each tool applies a \"stretch\" transformation to the<br>" +
	                        		"selected objects, stretching along a yellow axis.  To create<br>" +
	                        		"a tool, select a ball as the center of the mapping, and a<br>" +
	                        		"yellow strut as the direction of the stretch.  The ball and<br>" +
	                        		"strut need not be collinear.<br>" +
	                        		"<br>" +
	                        		"The mapping comes from the usual Zome projection of the<br>" +
	                        		"120-cell.  It is the inverse of the mapping that transforms<br>" +
	                        		"the central, blue dodecahedron into the compressed form along<br>" +
	                        		"a yellow axis.<br>" +
	                        		"<br>" +
	                        		"By default, the input selection will be removed, and replaced<br>" +
	                        		"with the stretched equivalent.  If you want to keep the inputs,<br>" +
	                        		"you can right-click after creating the tool, to configure it.<br>" +
	                		"</p>" );
	                firstToolbar .add( button );
	                
	                button = newToolButton( toolsController, "redsquash2", "Create a strong red squash tool",
	                		"<p>" +
	                        		"Each tool applies a \"squash\" transformation to the<br>" +
	                        		"selected objects, compressing along a red axis.  To create<br>" +
	                        		"a tool, select a ball as the center of the mapping, and a<br>" +
	                        		"red strut as the direction of the compression.  The ball and<br>" +
	                        		"strut need not be collinear.<br>" +
	                        		"<br>" +
	                        		"The mapping comes from the usual Zome projection of the<br>" +
	                        		"120-cell.  It is the mapping that transforms the central,<br>" +
	                        		"blue dodecahedron into the compressed form in the second<br>" +
	                        		"layer outward along a red axis.<br>" +
	                        		"<br>" +
	                        		"By default, the input selection will be removed, and replaced<br>" +
	                        		"with the squashed equivalent.  If you want to keep the inputs,<br>" +
	                        		"you can right-click after creating the tool, to configure it.<br>" +
	                		"</p>" );
	                firstToolbar .add( button );
	                button = newToolButton( toolsController, "redstretch2", "Create a strong red stretch tool",
	                		"<p>" +
	                        		"Each tool applies a \"stretch\" transformation to the<br>" +
	                        		"selected objects, stretching along a red axis.  To create<br>" +
	                        		"a tool, select a ball as the center of the mapping, and a<br>" +
	                        		"red strut as the direction of the stretch.  The ball and<br>" +
	                        		"strut need not be collinear.<br>" +
	                        		"<br>" +
	                        		"The mapping comes from the usual Zome projection of the<br>" +
	                        		"120-cell.  It is the inverse of the mapping that transforms<br>" +
	                        		"the central, blue dodecahedron into the compressed form in<br>" +
	                        		"the second layer outward along a red axis.<br>" +
	                        		"<br>" +
	                        		"By default, the input selection will be removed, and replaced<br>" +
	                        		"with the stretched equivalent.  If you want to keep the inputs,<br>" +
	                        		"you can right-click after creating the tool, to configure it.<br>" +
	                		"</p>" );
	                firstToolbar .add( button );
	            }
                
                button = newToolButton( toolsController, "linear map", "Create a linear map tool",
                		"<p>" +
                        		"<b>For experts and Linear Algebra students...</b><br>" +
                        		"<br>" +
                        		"Each tool applies a linear transformation to the selected<br>" +
                        		"objects, possibly rotating, stretching, and compressing.  To<br>" +
                        		"create a tool, select a ball as the center of the mapping,<br>" +
                        		"three struts (in order) to define the input basis, and three<br>" +
                        		"more struts to define the output basis.<br>" +
                        		"<br>" +
                        		"You can omit the input basis if it would consist of three<br>" +
                        		"identical blue struts at right angles; the three struts you<br>" +
                        		"select will be interpreted as the output basis.<br>" +
                        		"<br>" +
                        		"By default, the input selection will be removed, and replaced<br>" +
                        		"with the transformed equivalent.  If you want to keep the inputs,<br>" +
                        		"you can right-click after creating the tool, to configure it.<br>" +
                		"</p>" );
                firstToolbar .add( button );

                this .secondToolbar = new JToolBar();
                this .secondToolbar .setFloatable( false );
                this .secondToolbar .setOrientation( JToolBar.HORIZONTAL );
//                this .secondToolbar .setToolTipText( "All commands and tools apply to the currently selected objects." );
                this .secondScroller = new JScrollPane( this .secondToolbar, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
                this .secondScroller .setBorder( null );
                monoStereoPlusToolbar .add( this .secondScroller, BorderLayout .NORTH );

                button = newBookmarkButton( toolsController );
                bookmarkBar .add( button );
                bookmarkBar .addSeparator();

                addBookmark( toolsController .getSubController( "bookmark.builtin/ball at origin" ) );

                button = makeEditButton( enabler, "delete", "Delete selected objects" );
                secondToolbar .add( button );
                button = makeEditButton( enabler, "hideball", "Hide selected objects" );
                secondToolbar .add( button );
                
                secondToolbar .addSeparator();

                button = makeEditButton( enabler, "joinballs", "Connect balls in a loop" );
                secondToolbar .add( button );
                button = makeEditButton( enabler, "chainballs", "Connect balls in a chain" );
                secondToolbar .add( button );
                button = makeEditButton( enabler, "joinBallsAllToLast", "Connect all balls to last selected" );
                secondToolbar .add( button );
                button = makeEditButton( enabler, "joinBallsAllPossible", "Connect balls in all possible ways" );
                secondToolbar .add( button );
                button = makeEditButton( enabler, "panel", "Make a panel polygon" );
                secondToolbar .add( button );
                button = makeEditButton( enabler, "centroid", "Construct centroid of points" );
                secondToolbar .add( button );
                
                secondToolbar .addSeparator();
                
                // we want this presentation order to be controlled here, not in the core
                addTool( toolsController .getSubController( "icosahedral.builtin/icosahedral around origin" ) );
                addTool( toolsController .getSubController( "octahedral.builtin/octahedral around origin" ) );
                addTool( toolsController .getSubController( "tetrahedral.builtin/tetrahedral around origin" ) );
                addTool( toolsController .getSubController( "point reflection.builtin/reflection through origin" ) );
                addTool( toolsController .getSubController( "mirror.builtin/reflection through XY plane" ) );
                addTool( toolsController .getSubController( "axial symmetry.builtin/symmetry around red through origin" ) );
                
                secondToolbar .addSeparator();

                addTool( toolsController .getSubController( "scaling.builtin/scale down" ) );
                addTool( toolsController .getSubController( "scaling.builtin/scale up" ) );
                addTool( toolsController .getSubController( "rotation.builtin/rotate around red through origin" ) );
                addTool( toolsController .getSubController( "translation.builtin/b1 move along +X" ) );
               
                secondToolbar .addSeparator();

            	toolsController .addPropertyListener( new PropertyChangeListener()
            	{
					@Override
					public void propertyChange( PropertyChangeEvent evt )
					{
						switch ( evt .getPropertyName() ) {

						case "tool.added":
				            if ( evt .getOldValue() == null )
				            {
				            	Controller controller = (Controller) evt .getNewValue();
				                String kind = controller .getProperty( "kind" );
				        		if ( kind .equals( "bookmark" ) )
				        			addBookmark( controller );
				        		else
				        			addTool( controller );
				            }
							break;

						default:
							break;
						}
				    }
				});
                
            	if ( hasOldToolBar ) {
                    // --------------------------------------- Create the fixed toolbar.

                    this .oldToolBar = new JToolBar( "vZome Toolbar" );
                    this .oldToolBar .setOrientation( JToolBar.VERTICAL );
                	this .add( oldToolBar, BorderLayout .LINE_END );

                    button = makeLegacyEditButton( enabler, "joinballs", "Join two or more selected balls" );
                    oldToolBar.add( button );
                    button.setRolloverEnabled( true );

                    String fieldName = controller.getProperty( "field.name" );
                    if ( "golden" .equals( fieldName ) ) {
                        button = makeLegacyEditButton( enabler, "icosasymm-golden", "Repeat selection with chiral icosahedral symmetry" );
                        oldToolBar.add( button );
                        button.setRolloverEnabled( true );
                    } else if ( "snubDodec" .equals( fieldName ) ) {
                        button = makeLegacyEditButton( enabler, "icosasymm-snubDodec", "Repeat selection with chiral icosahedral symmetry" );
                        oldToolBar.add( button );
                        button.setRolloverEnabled( true );
                    } else {
                        button = makeLegacyEditButton( enabler, "octasymm", "Repeat selection with chiral octahedral symmetry" );
                        oldToolBar.add( button );
                        button.setRolloverEnabled( true );
                    }

               		button = makeLegacyEditButton( enabler, "tetrasymm", "Repeat selection with chiral tetrahedral symmetry" );
                    oldToolBar.add( button );
                    button.setRolloverEnabled( true );

                    button = makeLegacyEditButton( enabler, "axialsymm", "Repeat selection with symmetry around an axis" );
                    oldToolBar.add( button );
                    button.setRolloverEnabled( true );

                    button = makeLegacyEditButton( enabler, "pointsymm", "Reflect selection through origin" );
                    oldToolBar.add( button );
                    button.setRolloverEnabled( true );

                    button = makeLegacyEditButton( enabler, "mirrorsymm", "Reflect selection through mirror" );
                    oldToolBar.add( button );
                    button.setRolloverEnabled( true );

                    button = makeLegacyEditButton( enabler, "translate", "Repeat selection translated along symmetry axis" );
                    oldToolBar.add( button );
                    button.setRolloverEnabled( true );

                    if ( fullPower )
                    {
                        button = makeLegacyEditButton( enabler, "centroid", "Construct centroid of points" );
                        oldToolBar.add( button );
                        button.setRolloverEnabled( true );
                    }

                    button = makeLegacyEditButton( enabler, "hideball", "Hide selected objects" );
                    oldToolBar.add( button );
                    button.setRolloverEnabled( true );

                    button = makeLegacyEditButton( enabler, "panel", "Make a panel polygon" );
                    oldToolBar.add( button );
                    button.setRolloverEnabled( true );
            	}
            	else
                	this .oldToolBar = null;
            }
            else {
            	this .oldToolBar = null;
            	this .secondToolbar = null;
            	this .firstToolbar = null;
            	this .bookmarkBar = null;
            	firstScroller = null; secondScroller = null; bookmarkScroller = null;
            }
        }
        else {
        	this .oldToolBar = null;
        	this .secondToolbar = null;
        	this .firstToolbar = null;
        	this .bookmarkBar = null;
        	firstScroller = null; secondScroller = null; bookmarkScroller = null;
        }

        new SwingWorker<Controller, Object>()
        {
			@Override
			protected Controller doInBackground() throws Exception
			{
				return controller .asController();
			}
			
			@Override
			protected void done()
			{
				try {
					setController( get() );
				} catch ( InterruptedException | ExecutionException e ) {
					e.printStackTrace();
				}
				super.done();
			}
		} .execute();
	}
	
	private void setController( Controller controller )
	{
        Controller monoController = controller .getSubController( "monocularPicking" );
//        Controller leftController = controller .getSubController( "leftEyePicking" );
//        Controller rightController = controller .getSubController( "rightEyePicking" );

        monocularCanvas = ( (J3dComponentFactory) controller ) .createJ3dComponent( "mainViewer-monocular" );
        mMonocularPanel .add( monocularCanvas, BorderLayout.CENTER );

        monocularClicks = new ContextualMenuMouseListener( monoController , new PickerContextualMenu( monoController, enabler, "monocular" ) );
//        leftEyeClicks = new ContextualMenuMouseListener( leftController , new PickerContextualMenu( leftController, enabler, "leftEye" ) );
//        rightEyeClicks = new ContextualMenuMouseListener( rightController , new PickerContextualMenu( rightController, enabler, "rightEye" ) );
        monocularCanvas .addMouseListener( monocularClicks );
//        leftEyeCanvas .addMouseListener( leftEyeClicks );
//        rightEyeCanvas .addMouseListener( rightEyeClicks );
	}
	
	private void addTool( Controller controller )
	{
		if ( controller == null )
			// the field may not support the tool that was requested
			return;
		this .addTool( controller, controller .getProperty( "label" ) );
	}
	
	private void addTool( Controller controller, String label )
	{
		String kind = controller .getProperty( "kind" );
        String iconPath = "/icons/tools/small/" + kind + ".png";
        String tooltip = TOOLTIP_PREFIX + label + TOOLTIP_SUFFIX;
        JButton button = makeEditButton2( tooltip, iconPath );
		button .setActionCommand( "apply" );
		button .addActionListener( controller );
		button .addMouseListener( new MouseAdapter()
		{
			@Override
			public void mousePressed( MouseEvent e )
			{
				maybeShowPopup( e );
			}

			@Override
			public void mouseReleased( MouseEvent e )
			{
				maybeShowPopup( e );
			}

			private void maybeShowPopup( MouseEvent e )
			{
				if ( e.isPopupTrigger() ) {
					toolConfigDialog .showTool( button, controller );
				}
			}
		} );
		controller .addPropertyListener( new PropertyChangeListener()
		{
			@Override
			public void propertyChange( PropertyChangeEvent evt )
			{
				if ( "label" .equals( evt .getPropertyName() ) )
				{
					String label = (String) evt .getNewValue();
					String tooltip = TOOLTIP_PREFIX + label + TOOLTIP_SUFFIX;
					button .setToolTipText( tooltip );
				}
			}
		});
		secondToolbar .add( button );
    	}
	
	private void addBookmark( Controller controller )
	{
		String name = controller .getProperty( "label" );
		String iconPath = "/icons/tools/small/bookmark.png";
		String tooltip = TOOLTIP_PREFIX + name + TOOLTIP_SUFFIX;
		JButton button = makeEditButton2( tooltip, iconPath );
		button .setActionCommand( "apply" );
		button .addActionListener( controller );
		tooltip = "<html><b>" + name + "</b></html>";
		button .setToolTipText( tooltip );
		bookmarkBar .add( button );
	}
	
	private AbstractButton newToolButton( Controller toolsController, String group, String title, String helpHtml )
	{
		String iconPath = "/icons/tools/newTool/" + group + ".png";
		String html = "<html><img src=\"" + ModelPanel.class.getResource( iconPath ) + "\">&nbsp;&nbsp;<b>" + title
					+ "</b><br><br>" + helpHtml + "</html>";
		final JButton button = makeEditButton2( html, iconPath );
		button .setActionCommand( "createTool" );
		Controller buttonController = toolsController .getSubController( group );
		button .addActionListener( buttonController );
		button .setEnabled( buttonController != null && buttonController .propertyIsTrue( "enabled" ) );
		if ( buttonController != null )
			buttonController .addPropertyListener( new PropertyChangeListener()
			{
				@Override
				public void propertyChange( PropertyChangeEvent evt )
				{
					switch ( evt .getPropertyName() ) {

					case "enabled":
						button .setEnabled( (Boolean) evt .getNewValue() );
						break;
					}
				}
			});
		else
			System .out .println( "no controller for tool Factory: " + group );
		this .toolCreationButtons .put( group, button );
		return button;
	}
	
	private AbstractButton newBookmarkButton( Controller toolsController )
	{
		String iconPath = "/icons/tools/newTool/bookmark.png";
		String html = "<html><img src=\"" + ModelPanel.class.getResource( iconPath ) + "\">&nbsp;&nbsp;<b>" + "Create a selection bookmark"
					+ "</b><br><br>A selection bookmark lets you re-create<br>any selection at a later time.</html>";
		final JButton button = makeEditButton2( html, iconPath );
		button .setActionCommand( "createTool" );
		Controller buttonController = toolsController .getSubController( "bookmark" );
		button .addActionListener( buttonController );
		button .setEnabled( buttonController != null && buttonController .propertyIsTrue( "enabled" ) );
		if ( buttonController != null )
			buttonController .addPropertyListener( new PropertyChangeListener()
			{
				@Override
				public void propertyChange( PropertyChangeEvent evt )
				{
					switch ( evt .getPropertyName() ) {

					case "enabled":
						button .setEnabled( (Boolean) evt .getNewValue() );
						break;
					}
				}
			});
		else
			System .out .println( "no controller for tool Factory: bookmark" );
		this .toolCreationButtons .put( "bookmark", button );
		return button;
	}
	
	private AbstractButton makeEditButton( ControlActions enabler, String command, String tooltip )
	{
		AbstractButton button = makeEditButton2( tooltip, "/icons/tools/small/" + command + ".png" );
		button = enabler .setButtonAction( command, button );
		return button;
	}
	
	private AbstractButton makeLegacyEditButton( ControlActions enabler, String command, String tooltip )
	{
		String imageName = command;
		if ( imageName .endsWith( "-roottwo" ) )
			imageName = command .substring( 0, command.length() - 8 );
		else if ( imageName .endsWith( "-golden" ) )
			imageName = command .substring( 0, command.length() - 7 );
		AbstractButton button = makeEditButton2( tooltip, "/icons/" + imageName + "_on.png" );
		button = enabler .setButtonAction( command, button );
		Dimension dim = new Dimension( 100, 63 );
		button .setPreferredSize( dim );
		button .setMaximumSize( dim );
		return button;
	}
	
	private JButton makeEditButton2( String tooltip, String imgLocation )
	{
        // Create and initialize the button.
		JButton button = new JButton();
        URL imageURL = getClass() .getResource( imgLocation );
        if ( imageURL != null ) {
        	Icon icon = new ImageIcon( imageURL, tooltip );
        	button .setIcon( icon );

        	Dimension dim = new Dimension( icon .getIconWidth()+1, icon .getIconHeight()+1 );
        	button .setPreferredSize( dim );
        	button .setMaximumSize( dim );

        	// the rest will only work if setRolloverEnabled(true) is called
        	// after adding to the toolbar!
        	//        		imageURL = getClass() .getResource( "/icons/" + imageName + "_on.png" );
        	//        		icon = new ImageIcon( imageURL, label );
        	//        		button .setRolloverIcon( icon );
        } else
        	System.err.println( "Resource not found: " + imgLocation );

        button .setVerticalTextPosition( SwingConstants.TOP );
        button .setHorizontalTextPosition( SwingConstants.CENTER );
        button .setToolTipText( tooltip );
		return button;
	}

	@Override
	public void propertyChange( PropertyChangeEvent e )
	{
		switch ( e .getPropertyName() ) {
		
		case "editor.mode":
	        if ( isEditor )
	        {
	            if ( "article" .equals( e .getNewValue() ) ) {
	            	if ( this .oldToolBar != null )
	            		this .oldToolBar .setVisible( false );
	            	this .firstScroller .setVisible( false );
	            	this .secondScroller .setVisible( false );
	            	this .bookmarkScroller .setVisible( false );
	                monocularCanvas .removeMouseListener( monocularClicks );
//	                leftEyeCanvas .removeMouseListener( leftEyeClicks );
//	                rightEyeCanvas .removeMouseListener( rightEyeClicks );
	            }
	            else if ( ! "true" .equals( this .controller .getProperty( "no.toolbar" ) ) ) {
	            	if ( this .oldToolBar != null )
	            		this .oldToolBar .setVisible( true );
	            	this .firstScroller .setVisible( true );
	            	this .secondScroller .setVisible( true );
	            	this .bookmarkScroller .setVisible( true );
	                monocularCanvas .addMouseListener( monocularClicks );
//	                leftEyeCanvas .addMouseListener( leftEyeClicks );
//	                rightEyeCanvas .addMouseListener( rightEyeClicks );
	            }
	        }
			break;

		default:
			break;
		}
	}

	public Dimension getRenderedSize()
	{
		return this .mMonocularPanel .getSize();
	}
	
	private static class PickerContextualMenu extends ContextualMenu
	{
		private final Controller controller;
		
		public PickerContextualMenu( Controller controller, ControlActions enabler, String key )
		{
			super();
			this .controller = controller;
			boolean oldTools = controller .propertyIsTrue( "original.tools" );
            this .setLightWeightPopupEnabled( false );

			this .add( setMenuAction( "copyThisView", new JMenuItem( "Copy This View" ) ) );
			this .add( setMenuAction( "useCopiedView", new JMenuItem( "Use Copied View" ) ) );

			this .addSeparator();

			this .add( setMenuAction( "lookAtBall", new JMenuItem( "Look At This" ) ) );
			this .add( setMenuAction( "lookAtOrigin", new JMenuItem( "Look At Origin" ) ) );

			if ( oldTools ) {
				this .add( setMenuAction( "lookAtSymmetryCenter", new JMenuItem( "Look At Symmetry Center" ) ) );
				this .addSeparator();
				this .add( setMenuAction( "setSymmetryCenter", new JMenuItem( "Set Symmetry Center" ) ) );
				this .add( setMenuAction( "setSymmetryAxis", new JMenuItem( "Set Symmetry Axis" ) ) );
			}

			this .addSeparator();

			this .add( setMenuAction( "setWorkingPlane", new JMenuItem( "Set Working Plane" ) ) );
			this .add( setMenuAction( "setWorkingPlaneAxis", new JMenuItem( "Set Working Plane Axis" ) ) );

			this .addSeparator();

			this .add( setMenuAction( "selectCollinear", new JMenuItem( "Select Collinear" ) ) );
			this .add( setMenuAction( "selectParallelStruts", new JMenuItem( "Select Parallel Struts" ) ) );
			this .add( setMenuAction( "selectSimilarSize", new JMenuItem( "Select Similar Struts" ) ) );

			this .add( setMenuAction( "undoToManifestation", new JMenuItem( "Undo Including This" ) ) );

			this .addSeparator();

			this .add( enabler .setMenuAction( "setBackgroundColor", new JMenuItem( "Set Background Color..." ) ) );

			this .addSeparator();

			this .add( setMenuAction( "setBuildOrbitAndLength", new JMenuItem( "Build With This" ) ) );
			this .add( enabler .setMenuAction( "showProperties-"+key, new JMenuItem( "Show Properties" ) ) );
		}
		
		private JMenuItem setMenuAction( String action, JMenuItem control )
		{
			control .setEnabled( true );
			control .setActionCommand( action );
        	control .addActionListener( this .controller );
			return control;
		}
	}
}
