package edu.wpi.always.ttt;

public interface TTTUIListener {

   public void humanPlayed ();
   public void humanCommentTimeOut();
   public void agentPlayDelayOver();
   public void nextState();
   public void agentPlayingGazeDelayOver ();

}
