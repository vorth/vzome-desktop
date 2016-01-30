package org.vorthmann.zome.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import org.vorthmann.j3d.Platform;
import org.vorthmann.ui.Controller;

public class DocumentMenuBar extends JMenuBar implements PropertyChangeListener
{
	private static final int COMMAND = Platform.getKeyModifierMask();
	
	private static final int COMMAND_OPTION = COMMAND | InputEvent.ALT_MASK;

	private static final int COMMAND_SHIFT = COMMAND | InputEvent.SHIFT_MASK;

	private static final int CONTROL = InputEvent.ALT_MASK;
	
	private static final int CONTROL_OPTION = InputEvent.CTRL_MASK | InputEvent.ALT_MASK;
	
    private JMenuItem setColorMenuItem, showToolsMenuItem, zomicMenuItem, pythonMenuItem;

    private JMenu import3dSubmenu;

	private final ControlActions actions;
	
	private final boolean fullPower;

	private final Controller controller;
	
	public DocumentMenuBar( final Controller controller, ControlActions actions )
	{
		this .actions = actions;
		this .controller = controller;

		controller .addPropertyListener( this );
		
        String initSystem = controller .getProperty( "symmetry" );

        String fieldName = controller .getProperty( "field.name" );

        // TODO: compute these booleans once in DocumentFrame, and don't recompute here
        
        boolean readerPreview = controller .propertyIsTrue( "reader.preview" );

        boolean isEditor = controller .userHasEntitlement( "model.edit" ) && ! readerPreview;

        this .fullPower = isEditor; // && controller .userHasEntitlement( "all.tools" );

        boolean developerExtras = controller .userHasEntitlement( "developer.extras" );

        boolean enable4d = developerExtras || ( fullPower && controller .userHasEntitlement( "4d.symmetries" ) );

        boolean metaModels = developerExtras || ( fullPower && controller .userHasEntitlement( "meta.models" ) );
        
        boolean canSave = controller .userHasEntitlement( "save.files" );

        boolean isGolden = "golden" .equals( fieldName );
        
        boolean isHeptagon = "heptagon" .equals( fieldName );

        boolean isSnubDodec = "snubDodec" .equals( fieldName );

        boolean isRootTwo = "rootTwo" .equals( fieldName );

        boolean isRootThree = "rootThree" .equals( fieldName );

        // ----------------------------------------- File menu

        JMenu menu = new JMenu( "File" );

        if ( fullPower )
        {
            JMenu submenu = new JMenu( "New Model..." );
            submenu.add( enableIf( isEditor || readerPreview, createMenuItem( "Zome (Golden Field)", "new", KeyEvent.VK_N, COMMAND ) ) );
            submenu.add( createMenuItem( "\u221A2 Field", "new-rootTwo" ) );
            submenu.add( createMenuItem( "\u221A3 Field", "new-rootThree" ) );
            if ( "true" .equals( controller .getProperty( "enable.heptagon.field" ) ) )
                submenu.add( createMenuItem( "Heptagon Field", "new-heptagon" ) );
            if ( "true" .equals( controller .getProperty( "enable.snub.dodec.field" ) ) )
                submenu.add( createMenuItem( "Snub Dodec Field", "new-snubDodec" ) );
            menu.add( submenu );
        }
        else
        {
            menu.add( createMenuItem( "New Model...", "new", KeyEvent.VK_N, COMMAND ) );
        }
        menu.add( createMenuItem( "Open...", "open", KeyEvent.VK_O, COMMAND ) );
        menu.add( createMenuItem( "Open URL...", "openURL" ) );
        menu.add( createMenuItem( "Open As New Model...", "newFromTemplate" ) );
        if ( developerExtras )
            menu.add( createMenuItem( "Open Deferred...", "openDeferringRedo" ) );
        menu.addSeparator();
        menu.add( createMenuItem( "Close", "close", KeyEvent.VK_W, COMMAND ) );
        menu .add( enableIf( canSave, createMenuItem( "Save...", "save", KeyEvent.VK_S, COMMAND ) ) );
        menu .add( enableIf( canSave, createMenuItem( "Save As...", "saveAs" ) ) );
        menu .add( enableIf( canSave, createMenuItem( "Save Default", "saveDefault" ) ) );

        menu.addSeparator();

        JMenu submenu = new JMenu( "Import 3D..." );
        import3dSubmenu = submenu;
//        import3dSubmenu.add( createFileMenuItem( "Zomod", true, "import.zomod", "zomod" ) );
        import3dSubmenu.add( createMenuItem( "4D VEF projection", "import.vef" ) );
        menu.add( import3dSubmenu );
        import3dSubmenu .setEnabled( fullPower );

        submenu = new JMenu( "Export Faithful 3D..." );
        submenu .add( createMenuItem( "POV-Ray", "export.pov" ) );
        submenu .add( createMenuItem( "WebGL JSON", "export.json" ) );
        submenu .add( createMenuItem( "VRML", "export.vrml" ) );
        if ( developerExtras )
        {
            submenu .addSeparator();
            submenu.add( createMenuItem( "vZome history detail", "export.history" ) );
        }
        menu.add( submenu );
        submenu .setEnabled( fullPower && canSave );

        submenu = new JMenu( "Export Abstract 3D..." );
        submenu .add( createMenuItem( "STEP", "export.step" ) );
        submenu .add( createMenuItem( "VEF", "export.vef" ) );
        submenu .add( createMenuItem( "OFF", "export.off" ) );
        submenu .add( createMenuItem( "StL", "export.StL" ) );
        submenu .add( createMenuItem( "AutoCAD DXF", "export.dxf" ) );
        if ( controller .userHasEntitlement( "export.pdb" ) )
        {
            submenu .add( createMenuItem( "PDB", "export.pdb" ) );
        }
        if ( controller .userHasEntitlement( "export.seg" ) )
        {
            submenu .add( createMenuItem( "Mark Stock .seg", "export.seg" ) );
        }
        if ( controller .userHasEntitlement( "export.partslist" ) )
        {
            submenu.add( createMenuItem( "bill of materials", "export.partslist" ) );
        }
        if ( developerExtras )
        {
            submenu .addSeparator();
            submenu.add( createMenuItem( "COLLADA digital asset exchange", "export.dae" ) );
            submenu.add( createMenuItem( "Second Life", "export.2life" ) );
            submenu.add( createMenuItem( "Maximum XYZ", "export.size" ) );
            submenu.add( createMenuItem( "vZome part geometry", "export.partgeom" ) );
        }
        menu.add( submenu );
        submenu .setEnabled( fullPower && canSave );

        menu.addSeparator();

//        if ( controller .userHasEntitlement( "export.zomespace" ) )
//        {
//            submenu = new JMenu( "Export Article..." );
//            submenu .add( createMenuItem( "Zomespace", "export.zomespace" ) );
//            menu.add( submenu );
//            submenu .setEnabled( fullPower && canSave );
//        }

        submenu = new JMenu( "Capture Image..." );
        submenu .add( createMenuItem( "JPEG", "capture.jpg" ) );
        submenu .add( createMenuItem( "PNG", "capture.png" ) );
        submenu .add( createMenuItem( "GIF", "capture.gif" ) );
        submenu .add( createMenuItem( "BMP", "capture.bmp" ) );
        menu.add( submenu );

        menu .add( createMenuItem( "Capture Animation...", "capture-animation" ) );

        menu.add( enableIf( isEditor, createMenuItem( "Capture PDF or SVG...", "snapshot.2d" ) ) );

        menu.addSeparator();
        menu.add( createMenuItem( "Quit", "quit", KeyEvent.VK_Q, COMMAND ) );

        this .add( menu );

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Edit menu

        menu = new JMenu( "Edit" );
        menu .add( withAccelerator( KeyEvent.VK_Z, COMMAND, withAction( "undoRedo", "undo",  new JMenuItem( "Undo" ) ) ) );
        menu .add( withAccelerator( KeyEvent.VK_Y, COMMAND, withAction( "undoRedo", "redo",  new JMenuItem( "Redo" ) ) ) );
        menu .add( withAccelerator( KeyEvent.VK_Z, COMMAND_OPTION, withAction( "undoRedo", "undoAll",  new JMenuItem( "Undo All" ) ) ) );
        menu .add( withAccelerator( KeyEvent.VK_Y, COMMAND_OPTION, withAction( "undoRedo", "redoAll",  new JMenuItem( "Redo All" ) ) ) );
        if ( developerExtras )
        {
            menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            menu .add( withAccelerator( KeyEvent.VK_B, COMMAND_SHIFT, withAction( "undoRedo", "undoToBreakpoint", new JMenuItem( "Undo To Breakpoint" ) ) ) );
            menu .add( withAccelerator( KeyEvent.VK_B, COMMAND_OPTION, withAction( "undoRedo", "redoToBreakpoint", new JMenuItem( "Redo To Breakpoint" ) ) ) );
            menu .add( withAccelerator( KeyEvent.VK_B, COMMAND, withAction( "undoRedo", "setBreakpoint", new JMenuItem( "Set Breakpoint" ) ) ) );
            menu .add( withAction( "undoRedo", "redoUntilEdit", new JMenuItem( "Redo to Edit Number..." ) ) );
        }
        menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        menu .add( enableIf( isEditor, createMenuItem( "Copy", ( "copy" ), KeyEvent.VK_C, COMMAND ) ) );
        menu .add( enableIf( isEditor, createMenuItem( "Paste", ( "paste" ), KeyEvent.VK_V, COMMAND ) ) );

        menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        menu .add( enableIf( isEditor, createMenuItem( "Select All", ( "selectAll" ), KeyEvent.VK_A, COMMAND ) ) );
        menu .add( enableIf( isEditor, createMenuItem( "Select Neighbors", ( "selectNeighbors" ), KeyEvent.VK_A, COMMAND_OPTION ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Invert Selection", ( "invertSelection" ) ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Deselect Balls", ( "unselectBalls" ) ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Deselect Struts", ( "unselectStruts" ) ) ) );
        // menu .add( createMenuItem( "Select First Octant", getExclusiveAction( "test.pick.cube" ) ) );

        menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        menu .add( enableIf( isEditor, createMenuItem( "Group", ( "group" ), KeyEvent.VK_G, COMMAND ) ) );
        menu .add( enableIf( isEditor, createMenuItem( "Ungroup", ( "ungroup" ), KeyEvent.VK_G, COMMAND_OPTION ) ) );

        menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        menu .add( enableIf( isEditor, createMenuItem( "Hide", ( "hideball" ), KeyEvent.VK_H, CONTROL ) ) );
        menu .add( enableIf( isEditor, createMenuItem( "Show All Hidden", ( "showHidden" ), KeyEvent.VK_H, CONTROL_OPTION ) ) );

        menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        setColorMenuItem = enableIf( isEditor, createMenuItem( "Set Color...", "setItemColor" ) );
        menu .add( setColorMenuItem );

        this .add( menu );

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Construct menu
        menu = new JMenu( "Construct" );

        menu.add( enableIf( isEditor, createMenuItem( "Loop Balls", "joinballs", KeyEvent.VK_J, COMMAND ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Chain Balls", "chainBalls", KeyEvent.VK_J, COMMAND_OPTION ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Join Balls to Last", "joinBallsAllToLast" ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Make All Possible Struts", "joinBallsAllPossible" ) ) );

        menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        menu .add( enableIf( isEditor, createMenuItem( "Panel", ( "panel" ), KeyEvent.VK_P, COMMAND ) ) );
        menu .add( enableIf( isEditor, createMenuItem( "Panel/Strut Vertices", ( "showVertices" ) ) ) );

        menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        menu.add( enableIf( isEditor, createMenuItem( "Centroid", ( "centroid" ) ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Strut Midpoint", ( "midpoint" ) ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Line-Line Intersection", ( "lineLineIntersect" ) ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Line-Plane Intersection", ( "linePlaneIntersect" ) ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Cross Product", ( "crossProduct" ) ) ) );

        menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        menu.add( enableIf( isEditor, createMenuItem( "Ball At Origin", ( "ballAtOrigin" ) ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Ball At Symmetry Center", ( "ballAtSymmCenter" ) ) ) );

        menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//        menu.add( enableIf( isEditor, createMenuItem( "Affine Transform All", getExclusiveAction( "affineTransformAll" ) ) );
//        menuItem = enableIf( isEditor, createMenuItem( "Conjugate", getExclusiveAction( "conjugate" ) );
        if ( metaModels ) {
        	menu .add(  createMenuItem( "Meta-model", ( "realizeMetaParts" ) ) );
        }
        if ( isGolden ) {
            menu.add( enableIf( isEditor, createMenuItem( "\u03C4 Divide", ( "tauDivide" ) ) ) );
            menu.add( enableIf( isEditor, createMenuItem( "Affine Pentagon", ( "affinePentagon" ) ) ) );
        } else if ( isHeptagon )
            menu.add( enableIf( isEditor, createMenuItem( "1/\u03C3/\u03C1 Subdivisions", ( "heptagonDivide" ) ) ) );

        if ( developerExtras ) {
            menu .addSeparator(); // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

            menu.add( enableIf( isEditor, createMenuItem( "Assert Selection", ( "assertSelection" ) ) ) );

//            menu.add( enableIf( isEditor, createMenuItem( "6-Lattice", getExclusiveAction( "sixLattice" ) ) );
        }

        // TODO restore this somehow
//        try {
//            Properties softMenu = Preferences .getUserPropertiesFile(
//            "vZomeEditMenu.properties" );
//            if ( softMenu != null ) {
//                menu.addSeparator();
//                for ( Iterator keys = softMenu .keySet() .iterator(); keys
//                .hasNext(); ) {
//                    String actionName = (String) keys .next();
//                    String menuString = softMenu .getProperty( actionName );
//                    menu.add( createMenuItem( menuString, getExclusiveAction( actionName
//                    ) ) );
//                }
//            }
//        } catch ( Throwable t ) {
//            // !!! don't want to advertise this ability yet
//        }

        this .add( menu );


        // ----------------------------------------- Symmetry menu

        menu = new JMenu( "Tools" );
        
        menu.add( enableIf( isEditor, createMenuItem( "Set Center", "setSymmetryCenter" ) ) ); 
        menu.add( enableIf( isEditor, createMenuItem( "Set Axis", "setSymmetryAxis" ) ) ); 
        menu.addSeparator(); 
        
        showToolsMenuItem = enableIf( isEditor, createMenuItem( "Show Tools Panel", "showToolsPanel" ) );
        showToolsMenuItem .setEnabled( fullPower );
        menu .add( showToolsMenuItem );
        menu .addSeparator();

        if ( isGolden ) {
            menu.add( enableIf( isEditor, createMenuItem( "Icosahedral Symmetry", "icosasymm-golden", KeyEvent.VK_I, COMMAND ) ) );
        } else if ( isSnubDodec )
            menu.add( enableIf( isEditor, createMenuItem( "Icosahedral Symmetry", "icosasymm-snubDodec", KeyEvent.VK_I, COMMAND ) ) );

        if ( developerExtras && isRootThree ) {
            menu.add( enableIf( isEditor, createMenuItem( "Dodecagonal Symmetry", "dodecagonsymm", KeyEvent.VK_D, COMMAND ) ) );
        }
        menu.add( enableIf( isEditor, createMenuItem( "Cubic / Octahedral Symmetry", "octasymm", KeyEvent.VK_C, COMMAND_OPTION ) ) );
        menu .add( enableIf( isEditor, createMenuItem( "Tetrahedral Symmetry", "tetrasymm", KeyEvent.VK_T, COMMAND_OPTION ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Axial Symmetry", "axialsymm", KeyEvent.VK_R, COMMAND ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Point Reflection", "pointsymm" ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Mirror Reflection", "mirrorsymm" , KeyEvent.VK_M, COMMAND ) ) );
        menu.add( enableIf( isEditor, createMenuItem( "Translate", "translate", KeyEvent.VK_T, COMMAND ) ) );
        
        menu .addSeparator();
        menu .add( enableIf( isEditor, createMenuItem( "Generate Polytope...", "showPolytopesDialog", KeyEvent.VK_P, COMMAND_OPTION ) ) );
        if ( enable4d ) {
            menu.add( enableIf( isEditor, createMenuItem( "H_4 Symmetry", "h4symmetry" ) ) );
            menu.add( enableIf( isEditor, createMenuItem( "H_4 Rotations", "h4rotations" ) ) );
            menu.add( enableIf( isEditor, createMenuItem( "I,T Symmetry", "IxTsymmetry" ) ) );
            menu.add( enableIf( isEditor, createMenuItem( "T,T Symmetry", "TxTsymmetry" ) ) );
        }

        this .add( menu );

        // ----------------------------------------- Polytopes menu
        menu = new JMenu( "Polytopes" );
        menu .setEnabled( fullPower );
//        this .add( menu );

        String zeros = "0000";

        if ( isGolden ) {
            submenu = new JMenu( "H_4 Uniform" );
            for ( int p = 0x1; p <= 0xF; p++ ) {
                String dynkin = Integer.toString( p, 2 );
                dynkin = zeros.substring( dynkin.length() ) + dynkin;
                submenu.add( enableIf( isEditor, createMenuItem( dynkin + "   " + Integer.toString( p, 16 ), "polytope_H4" + dynkin ) ) );
            }
            menu.add( submenu );
            submenu .setEnabled( fullPower );

            submenu = new JMenu( "A_4 Uniform" );
            for ( int p = 0x1; p <= 0xF; p++ ) {
                String dynkin = Integer.toString( p, 2 );
                dynkin = zeros.substring( dynkin.length() ) + dynkin;
                submenu.add( enableIf( isEditor, createMenuItem( dynkin + "   " + Integer.toString( p, 16 ), "polytope_A4" + dynkin ) ) );
            }
            menu.add( submenu );
            submenu .setEnabled( fullPower );
        }

        submenu = new JMenu( isGolden ? "B_4 Symmetric" : "B_4 Uniform" );
        for ( int p = 0x1; p <= 0xF; p++ ) {
            String dynkin = Integer.toString( p, 2 );
            dynkin = zeros.substring( dynkin.length() ) + dynkin;
            submenu
                    .add( enableIf( isEditor, createMenuItem( dynkin + "   " + Integer.toString( p, 16 ), "polytope_B4" + dynkin ) ) );
        }
        menu.add( submenu );
        submenu .setEnabled( fullPower );

        submenu = new JMenu( isGolden ? "F_4 Symmetric" : "F_4 Uniform" );
        for ( int p = 0x1; p <= 0xF; p++ ) {
            String dynkin = Integer.toString( p, 2 );
            dynkin = zeros.substring( dynkin.length() ) + dynkin;
            submenu .add( enableIf( isEditor, createMenuItem( dynkin + "   " + Integer.toString( p, 16 ), "polytope_F4" + dynkin ) ) );
        }
        menu.add( submenu );
        submenu .setEnabled( fullPower );

        if ( developerExtras && isRootThree )
            menu.add( enableIf( isEditor, createMenuItem( "Ghost Symmetric 24-cell", "ghostsymm24cell" ) ) );
        if ( developerExtras && isGolden )
            menu.add( enableIf( isEditor, createMenuItem( "van Oss 600-cell", "vanOss600cell" ) ) );

        // ----------------------------------------- System menu

        menu = new JMenu( "System" );
        ButtonGroup group = new ButtonGroup();
        JMenuItem rbMenuItem;
        if ( isGolden || isSnubDodec ) {
            rbMenuItem = actions .setMenuAction( "setSymmetry.icosahedral", new JRadioButtonMenuItem( "Icosahedral System" ) );
            rbMenuItem .setSelected( "icosahedral".equals( initSystem ) );
            rbMenuItem .setEnabled( fullPower );
            group.add( rbMenuItem );
            menu.add( rbMenuItem );
        }

        rbMenuItem = actions .setMenuAction( "setSymmetry.octahedral", new JRadioButtonMenuItem( "Octahedral System" ) );
        rbMenuItem .setSelected( "octahedral".equals( initSystem ) );
        rbMenuItem .setEnabled( fullPower );
        group.add( rbMenuItem );
        menu.add( rbMenuItem );

        if ( isRootThree )
        {
            rbMenuItem = actions .setMenuAction( "setSymmetry.dodecagonal", new JRadioButtonMenuItem( "Dodecagon System" ) );
            rbMenuItem .setSelected( "dodecagonal".equals( initSystem ) );
            rbMenuItem .setEnabled( fullPower );
            group.add( rbMenuItem );
            menu.add( rbMenuItem );
        }
        else if ( isRootTwo )
        {
            rbMenuItem = actions .setMenuAction( "setSymmetry.synestructics", new JRadioButtonMenuItem( "Synestructics System" ) );
            rbMenuItem .setSelected( "synestructics".equals( initSystem ) );
            rbMenuItem .setEnabled( fullPower );
            group.add( rbMenuItem );
            menu.add( rbMenuItem );
        }

        menu.addSeparator();
        
        if ( developerExtras )
        {
            JMenuItem wfMenuItem = actions .setMenuAction( "toggleWireframe", new JCheckBoxMenuItem( "Wireframe" ) );
            boolean isWireframe = "true".equals( controller .getProperty( "wireframe" ) );
            wfMenuItem .setSelected( isWireframe );
            menu.add( wfMenuItem );
        }

        menu.add( createMenuItem( "Shapes...", "configureShapes" ) );
        menu.add( createMenuItem( "Directions...", "configureDirections" ) );

        JMenuItem cbMenuItem = actions .setMenuAction( "toggleOrbitViews", new JCheckBoxMenuItem( "Show Directions Graphically" ) );
        boolean setting = "true".equals( controller .getProperty( "useGraphicalViews" ) );
        cbMenuItem .setSelected( setting );
        menu.add( cbMenuItem );
        cbMenuItem .setEnabled( fullPower );

        final JMenuItem showStrutScalesItem = actions .setMenuAction( "toggleStrutScales", new JCheckBoxMenuItem( "Show Strut Scales" ) );
        setting = "true" .equals( controller .getProperty( "showStrutScales" ) );
        showStrutScalesItem .setSelected( setting );
        showStrutScalesItem .setEnabled( fullPower );
        controller .addPropertyListener( new PropertyChangeListener(){
			@Override
            public void propertyChange( PropertyChangeEvent chg )
            {
                if ( "showStrutScales" .equals( chg .getPropertyName() ) )
                    showStrutScalesItem .setSelected(((Boolean) chg .getNewValue()));
            }} );
        menu .add( showStrutScalesItem );

//        cbMenuItem = enabler .enableMenuAction( "toggleOneSidedPanels", new JCheckBoxMenuItem( "Show Panels One-sided" ) );
//        setting = "true".equals( controller.getProperty( "oneSidedPanels" ) );
//        cbMenuItem.setSelected( setting );
//        menu.add( cbMenuItem );

        cbMenuItem = actions .setMenuAction( "toggleFrameLabels", new JCheckBoxMenuItem( "Show Frame Labels" ) );
        setting = "true".equals( controller .getProperty( "showFrameLabels" ) );
        cbMenuItem .setSelected( setting );
        menu.add( cbMenuItem );

        cbMenuItem = actions .setMenuAction( "toggleOutlines", new JCheckBoxMenuItem( "Render Outlines" ) );
        setting = "true".equals( controller.getProperty( "drawOutlines" ) );
        cbMenuItem .setSelected( setting );
        menu.add( cbMenuItem );

        this.add( menu );

        // ----------------------------------------- Scripting menu

        menu = new JMenu( "Scripting" );
        menu .setEnabled( fullPower );
        pythonMenuItem = createMenuItem( "Python...", "showPythonWindow" );
        pythonMenuItem .setEnabled( fullPower );
        if ( developerExtras )
            menu .add( pythonMenuItem );
        zomicMenuItem = createMenuItem( "Zomic...", "showZomicWindow" );
        zomicMenuItem .setEnabled( fullPower );
        menu .add( zomicMenuItem );
        this .add( menu );

        menu = new JMenu( "Help" );
        if ( "G4G10" .equals( controller .getProperty( "licensed.user" ) ) )
            menu .add( createMenuItem( "Welcome G4G10 Participant...", "openResource-org/vorthmann/zome/content/welcomeG4G10.vZome" ) );            
        menu .add( createMenuItem( "Quick Start...", "openResource-org/vorthmann/zome/content/welcomeDodec.vZome" ) );
        menu .addSeparator(); 
        menu .add( createMenuItem( "About vZome...", "showAbout" ) );
        this .add( menu );
	}

	@Override
	public void propertyChange( PropertyChangeEvent e )
	{
        if ( "editor.mode" .equals( e .getPropertyName() ) )
        {
            String mode = (String) e .getNewValue();
            if ( "article" .equals( mode ) )
            {
                setColorMenuItem .setEnabled( false );
                showToolsMenuItem .setEnabled( false );
                pythonMenuItem .setEnabled( false );
                zomicMenuItem .setEnabled( false );
                import3dSubmenu .setEnabled( false );
            }
            else
            {                   
                setColorMenuItem .setEnabled( true );
                showToolsMenuItem .setEnabled( fullPower );
                pythonMenuItem .setEnabled( fullPower );
                zomicMenuItem .setEnabled( fullPower );
                import3dSubmenu .setEnabled( fullPower );
            }
        }
	}

	private JMenuItem enableIf( boolean enable, JMenuItem control )
	{
    	control .setEnabled( enable );
    	return control;
	}

	private JMenuItem withAction( String action, JMenuItem menuItem )
	{
		return withAction( null, action, menuItem );
	}

	private JMenuItem withAction( String controllerName, String action, JMenuItem menuItem )
	{
		menuItem .setActionCommand( action );
		Controller subc = this .controller;
		if ( controllerName != null )
			subc = subc .getSubController( controllerName );
		if ( subc != null ) {
			menuItem .setEnabled( true );
			menuItem .addActionListener( subc );
		}
		else
			menuItem .setEnabled( false );
    	return menuItem;
	}

	private JMenuItem withAccelerator( int key, int modifiers, JMenuItem menuItem )
	{
        menuItem .setAccelerator( KeyStroke.getKeyStroke( key, modifiers ) );
    	return menuItem;
	}
	
    private JMenuItem createMenuItem( String label, String command )
    {
    	return createMenuItem( label, command, KeyEvent .CHAR_UNDEFINED, 0 );
	}

    private JMenuItem createMenuItem( String label, String command, int key, int modifiers )
    {
    	JMenuItem menuItem = actions .setMenuAction( command, new JMenuItem( label ) );
    	menuItem .setEnabled( true );
        if ( key != KeyEvent .CHAR_UNDEFINED )
            menuItem .setAccelerator( KeyStroke.getKeyStroke( key, modifiers ) );
		return menuItem;
	}
}
