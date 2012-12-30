package edu.wpi.always.cm;

import edu.wpi.always.Bootstrapper;
import edu.wpi.always.client.ClientRegistry;
import edu.wpi.always.cm.perceptors.*;
import edu.wpi.always.cm.perceptors.sensor.SensorsRegistry;
import edu.wpi.always.rm.*;
import edu.wpi.always.user.UserModel;
import edu.wpi.always.user.owl.OntologyUserRegistry;
import edu.wpi.disco.rt.ComponentRegistry;
import org.picocontainer.*;
import java.awt.*;
import javax.swing.*;

public class EngagementProgram {

   private static FacePerception facePerception;

   public static void main (String[] args) {
      final Bootstrapper program = new Bootstrapper(false);
      program.addRegistry(new ComponentRegistry() {

         @Override
         public void register (MutablePicoContainer container) {
            container.as(Characteristics.CACHE).addComponent(
                  IRelationshipManager.class, DummyRelationshipManager.class);
            container.as(Characteristics.CACHE).addComponent(
                  ICollaborationManager.class,
                  edu.wpi.always.cm.CollaborationManager.class);
         }
      });
      program.addRegistry(new OntologyUserRegistry("Test User"));
      program.addCMRegistry(new SensorsRegistry());
      program.addCMRegistry(new ClientRegistry());
      program.start();
      program.getContainer().getComponent(UserModel.class).load();
      JFrame frame = new JFrame("Face Location");
      frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
      JLabel locationLabel = new JLabel();
      locationLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
      frame.add(locationLabel);
      JLabel sizeLabel = new JLabel();
      sizeLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
      frame.add(sizeLabel);
      JPanel panel = new JPanel() {

         /**
			 * 
			 */
         private static final long serialVersionUID = 6254014952377622108L;

         @Override
         public void paintComponent (Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 320, 240);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, 320, 240);
            if ( facePerception != null )
               g.fillRect(facePerception.getLeft() + 160,
                     facePerception.getTop() + 120, facePerception.getRight()
                        - facePerception.getLeft(), facePerception.getBottom()
                        - facePerception.getTop());
         }
      };
      frame.add(panel);
      JLabel stateLabel = new JLabel();
      stateLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
      frame.add(stateLabel);
      frame.pack();
      frame.setSize(500, 500);
      frame.setVisible(true);
      frame.setAlwaysOnTop(true);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      FacePerceptor facePerceptor = program.getContainer()
            .getComponent(CollaborationManager.class).getContainer()
            .getComponent(FacePerceptor.class);
      EngagementPerceptor engagementPerceptor = program.getContainer()
            .getComponent(CollaborationManager.class).getContainer()
            .getComponent(EngagementPerceptor.class);
      while (true) {
         try {
            Thread.sleep(200);
         } catch (InterruptedException e) {
         }
         facePerception = facePerceptor.getLatest();
         if ( facePerception != null ) {
            locationLabel.setText("Location: " + facePerception.getPoint());
            sizeLabel.setText("Size: "
               + (facePerception.getRight() - facePerception.getLeft()) + " x "
               + (facePerception.getBottom() - facePerception.getTop())
               + (facePerception.isNear() ? " is near" : " is far"));
         }
         EngagementPerception engagementPerception = engagementPerceptor
               .getLatest();
         if ( engagementPerception != null ) {
            stateLabel.setText("State: " + engagementPerception.getState());
         }
         panel.repaint();
      }
   }

}
