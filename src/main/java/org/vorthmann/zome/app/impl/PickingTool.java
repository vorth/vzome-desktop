package org.vorthmann.zome.app.impl;

import com.vzome.core.model.Manifestation;

public interface PickingTool
{
	boolean doManifestationAction( Manifestation pickedManifestation, String action );

	boolean[] enableCommands( Manifestation pickedManifestation, String[] menu, boolean[] result );
}
