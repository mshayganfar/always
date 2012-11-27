package edu.wpi.always.cm.engagement;

import org.picocontainer.*;

import edu.wpi.always.PicoRegistry;
import edu.wpi.always.cm.*;


public class EngagementRegistry implements SchemaRegistry, PicoRegistry {

	@Override
	public void register(SchemaManager manager) {
		manager.registerSchema(EngagementSchema.class, true);
	}

	@Override
	public void register(MutablePicoContainer container) {
		container.as(Characteristics.CACHE).addComponent(GeneralEngagementPerceptorImpl.class);
	}

}