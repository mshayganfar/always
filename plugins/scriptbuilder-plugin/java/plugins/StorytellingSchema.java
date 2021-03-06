package plugins;

import edu.wpi.always.*;
import edu.wpi.always.client.*;
import edu.wpi.always.cm.perceptors.EngagementPerception;
import edu.wpi.always.cm.perceptors.sensor.face.ShoreFacePerceptor;
import edu.wpi.always.user.people.PeopleManager;
import edu.wpi.always.user.places.PlaceManager;
import edu.wpi.disco.rt.ResourceMonitor;
import edu.wpi.disco.rt.behavior.*;
import edu.wpi.disco.rt.menu.MenuPerceptor;
import pluginCore.*;

public class StorytellingSchema extends ScriptbuilderSchema {

   public final static Logger.Activity LOGGER_NAME = Logger.Activity.STORY;
   
   private static UIMessageDispatcher dispatcher;
   
	public enum Saved { SAVED, NOT_SAVED }
   
	public static void log (Saved saved, String storyType) {
	   Logger.logActivity(LOGGER_NAME, saved, storyType);
	   Message msg = Message.builder("story.saveRecording").add("saved",saved.name()).add("type",storyType).build();
	   dispatcher.send(msg);
	}
   
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
		       behaviorReceiver, behaviorHistory, resourceMonitor, menuPerceptor, dispatcher,
		       LOGGER_NAME);
		 this.dispatcher = dispatcher;
		 this.shore = shore instanceof ShoreFacePerceptor.Reeti ? null : shore;
		 always.getUserModel().setProperty(StorytellingPlugin.PERFORMED, true);
		 interruptible = false;
		 EngagementPerception.setRecoveringEnabled(false);
	}
	
	@Override
   public void dispose () { 
      super.dispose();
      // these are here so it is run even if schema throws an error
      if ( shore != null ) shore.start(); 
      EngagementPerception.setRecoveringEnabled(true);
   }
	
}
