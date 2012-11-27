package edu.wpi.always.cm.ragclient;

import edu.wpi.always.cm.*;

public class RummySchema extends SchemaImplBase {

	private final RummyPlugin plugin;
	private boolean firstRun = true;

	public RummySchema(BehaviorProposalReceiver behaviorReceiver,
			BehaviorHistory resourceMonitor, RummyPlugin plugin) {
		super(behaviorReceiver, resourceMonitor);
		this.plugin = plugin;
		
		setNeedsFocusResouce();
	}

	@Override
	public void run() {
		if (firstRun) {
			firstRun = false;
			plugin.initInteraction();
		}

		BehaviorBuilder r = plugin.updateInteraction(lastProposalIsDone());

		BehaviorMetadata m = r.getMetadata();
		Behavior b = r.build();
		
		if(b.equals(Behavior.NULL) && !plugin.gameOver()) {
			proposeIdle(0.7);
		} else {
			propose(b, m);
		}
	}
}