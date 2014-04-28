package plugins;

import edu.wpi.always.Always;
import edu.wpi.always.client.*;
import edu.wpi.always.cm.perceptors.sensor.face.ShoreFacePerceptor;
import edu.wpi.always.user.people.PeopleManager;
import edu.wpi.always.user.places.PlaceManager;
import edu.wpi.disco.rt.ResourceMonitor;
import edu.wpi.disco.rt.behavior.*;
import edu.wpi.disco.rt.menu.MenuPerceptor;
import pluginCore.*;

public class StorytellingSchema extends ScriptbuilderSchema {

   private final ShoreFacePerceptor shore;
   
	public StorytellingSchema (BehaviorProposalReceiver behaviorReceiver,
			BehaviorHistory behaviorHistory, ResourceMonitor resourceMonitor,
			MenuPerceptor menuPerceptor, Keyboard keyboard,
			UIMessageDispatcher dispatcher, PlaceManager placeManager,
			PeopleManager peopleManager, Always always, ShoreFacePerceptor shore) {
		 super(new ScriptbuilderCoreScript(new RAGStateContext(
	               keyboard, dispatcher, placeManager, peopleManager, always, 
	               shore instanceof ShoreFacePerceptor.Reeti ? null : shore,
	               "Storytelling")),
		       behaviorReceiver, behaviorHistory, resourceMonitor, menuPerceptor, dispatcher);
		 this.shore = shore;
	}
	
	@Override
   public void dispose () { 
      super.dispose();
      // this is here so it is run even if schema throws an error
      if ( shore != null ) shore.start(); 
   }
 
}