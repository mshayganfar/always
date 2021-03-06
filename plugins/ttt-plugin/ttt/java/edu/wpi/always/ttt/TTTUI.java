package edu.wpi.always.ttt;

import java.util.List;

public interface TTTUI {

   public void resetGame();
   public void prepareAgentMove();
   public void makeBoardPlayable();
   public void prepareAgentCommentUserResponseForAMoveBy(int player);
   public void makeBoardUnplayable();
   public String getCurrentAgentComment();
   public String getCurrentAgentResponse(String humanChoosenComment);
   public List<String> getCurrentHumanResponseOptions();
   public void triggerAgentPlayTimer();
   public void cancelHumanCommentingTimer();
   public void triggerHumanCommentingTimer();
   public void updatePlugin(TTTUIListener listener);
   public void playAgentMove (TTTUIListener listener);
   public List<String> getCurrentHumanCommentOptionsAgentResponseForAMoveBy(int player);
   public void startPluginForTheFirstTime(TTTUIListener listener);
   public void triggerNextStateTimer(TTTUIListener listener);
   

}
