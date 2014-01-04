package edu.wpi.always.client;

import java.awt.Point;
import edu.wpi.always.cm.primitives.GazeBehavior;
import edu.wpi.disco.rt.realizer.SingleRunPrimitiveRealizer;

public class GazeRealizer extends SingleRunPrimitiveRealizer<GazeBehavior> {

   private final ClientProxy proxy;

   public GazeRealizer (GazeBehavior params, ClientProxy proxy) {
      super(params);
      this.proxy = proxy;
   }

   @Override
   protected void singleRun () {
      proxy.gaze(translateToAgentTurnHor(getParams().getPoint()),
            translateToAgentTurnVer(getParams().getPoint()));
      fireDoneMessage();
   }
  
   /**
    * Inverse of {@link #translateToAgentTurnHor} and 
    * {@link #translateToAgentTurnVer}.
    */
   public static Point translateAgentTurn (float hor, float ver) {
      return new Point(Math.round(160f - (160f * hor / 0.4f)),
            Math.round(120f - (120f * ver / 0.1f)));
   }

   /**
    * Convert camera image coordinates to horizontal
    * component of agent coordinates.
    * 
    * @return between (inclusive) -1 (agent's right) and +1 (agent's left)
    */
    public static float translateToAgentTurnHor (Point p) {
       // NB: Make sure to change translateAgentTurn if change this
       return (((160f - p.x) * 0.4f / 160f) < -0.175) ? (((160f - p.x) * 0.4f / 160f) + 0.165f) : 
              ((((160f - p.x) * 0.4f / 160f) < 0.15) ? (((160f - p.x) * 0.4f / 160f) - 0.005f)  : ((160f - p.x) * 0.4f / 160f) - 0.12f);
   }

   /**
    * Convert camera image coordinates to vertical component
    * of agent coordinates.
    * 
    * @return between (inclusive) -1 (down) and +1 (up)
    */
   public static float translateToAgentTurnVer (Point p) {
      // NB: Make sure to change translateAgentTurn if change this
      return (120f - p.y) * 0.1f / 120f;
   }
}