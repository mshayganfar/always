package plugins;

import edu.wpi.always.*;
import edu.wpi.always.client.*;
import edu.wpi.always.user.people.PeopleManager;
import edu.wpi.always.user.places.PlaceManager;
import edu.wpi.disco.rt.ResourceMonitor;
import edu.wpi.disco.rt.behavior.*;
import edu.wpi.disco.rt.menu.MenuPerceptor;
import pluginCore.*;
import plugins.ExerciseSchema.Topic;

public class NutritionSchema extends ScriptbuilderSchema {
   
   public final static Logger.Activity LOGGER_NAME = Logger.Activity.NUTRITION;
   
   public enum Topic { GOALS, SERVINGS};
  
   public static void log (Topic topic, String value) {
	      Logger.logActivity(LOGGER_NAME, topic, value);
   }
   
	public NutritionSchema (BehaviorProposalReceiver behaviorReceiver,
			BehaviorHistory behaviorHistory, ResourceMonitor resourceMonitor,
			MenuPerceptor menuPerceptor, Keyboard keyboard,
			UIMessageDispatcher dispatcher, PlaceManager placeManager,
			PeopleManager peopleManager, Always always) {
		 super(new ScriptbuilderCoreScript(new RAGStateContext(
	               keyboard, dispatcher, placeManager, peopleManager, always, "Nutrition")),
		       behaviorReceiver, behaviorHistory, resourceMonitor, menuPerceptor, dispatcher,
		       LOGGER_NAME);
		 always.getUserModel().setProperty(NutritionPlugin.PERFORMED, true);
	}

}