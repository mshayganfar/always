package edu.wpi.disco.rt.realizer;

import edu.wpi.disco.rt.*;
import edu.wpi.disco.rt.behavior.*;
import edu.wpi.disco.rt.util.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Realizer implements IRealizer, BehaviorHistory {

   private final PrimitiveBehaviorControl primitiveControl;
   private final ExecutorService executor;
   private final Hashtable<Resource, CompoundRealizerHandle> behaviorHandles = new Hashtable<Resource, Realizer.CompoundRealizerHandle>();
   private final ReentrantLock mainLock = new ReentrantLock();
   private final List<TimeStampedValue<CompoundBehavior>> history = new ArrayList<TimeStampedValue<CompoundBehavior>>();
   /*
    * Let's talk about history locks a little: Anything that wants to change the
    * history needs to acquire historyChangeLock. So both adding and removing
    * from history will need this lock, but not merely reading. Anything that
    * wants to remove stuff from history should acquire
    * historyShrinkLock.writeLock(). Now, isDone() is implemented in a way that
    * it will not be disturbed by adding stuff to the end of the history, but if
    * something is removed, it will cause problems. That is why isDone() needs
    * to get historyShrinkLock.readLock()
    */
   private final Lock historyChangeLock = new ReentrantLock();
   private final ReadWriteLock historyShrinkLock = new ReentrantReadWriteLock();

   public Realizer (PrimitiveBehaviorControl primitiveControl) {
      this.primitiveControl = primitiveControl;
      executor = ThreadPools.newCachedThreadPool(true);
   }

   @Override
   public void realize (CompoundBehavior behavior) {
      Set<Resource> resources = behavior.getResources();
      CompoundRealizerHandle handle = new CompoundRealizerHandle(behavior);
      mainLock.lock();
      try {
         if ( !isAlreadyInRunning(behavior) ) {
            for (Resource r : resources) {
               freeUpResource(r);
               behaviorHandles.put(r, handle);
            }
            handle.start();
         }
      } finally {
         mainLock.unlock();
      }
   }

   private boolean isAlreadyInRunning (CompoundBehavior behavior) {
      boolean alreadyInEffect = false;
      for (CompoundRealizerHandle h : behaviorHandles.values()) {
         if ( h.behavior.equals(behavior) )
            alreadyInEffect = true;
      }
      return alreadyInEffect;
   }

   @Override
   public void freeUpResource (Resource r) {
      mainLock.lock();
      try {
         if ( behaviorHandles.containsKey(r) ) {
            CompoundRealizerHandle h = behaviorHandles.get(r);
            h.stop();
         }
      } finally {
         mainLock.unlock();
      }
   }

   private void stopPrimitiveOn (Resource r) {
      primitiveControl.stop(r);
      if ( DiscoRT.TRACE ) Utils.lnprint(System.out, "Stopping: "+r);
   }

   private void saveDoneBehaviorInHistory (CompoundBehavior behavior) {
      historyChangeLock.lock();
      try {
         history.add(new TimeStampedValue<CompoundBehavior>(behavior));
         shrinkHistoryIfNecessary();
      } finally {
         historyChangeLock.unlock();
      }
   }

   private void shrinkHistoryIfNecessary () {
      if ( history.size() > 110 ) {
         historyChangeLock.lock();
         try {
            historyShrinkLock.writeLock().lock();
            try {
               while (history.size() > 100) {
                  history.remove(0);
               }
            } finally {
               historyShrinkLock.writeLock().unlock();
            }
         } finally {
            historyChangeLock.unlock();
         }
      }
   }

   @Override
   public boolean isDone (CompoundBehavior behavior, long since) {
      historyShrinkLock.readLock().lock();
      try {
         for (int i = history.size() - 1; i >= 0; i--) {
            TimeStampedValue<CompoundBehavior> cur = history.get(i);
            if ( cur.getTimeStamp() < since )
               break;
            if ( cur.getValue().equals(behavior) ) {
               return true;
            }
         }
         return false;
      } finally {
         historyShrinkLock.readLock().unlock();
      }
   }

   private class CompoundRealizerHandle implements CompoundRealizerObserver {

      private final CompoundBehavior behavior;
      private PrimitiveBehaviorControlDisconnectableWrapper primitiveAccess;
      private Future<?> task;
      private CompoundRealizer compoundRealizer;

      public CompoundRealizerHandle (CompoundBehavior behavior) {
         this.behavior = behavior;
      }

      public void start () {
         if ( DiscoRT.TRACE ) Utils.lnprint(System.out, "Starting: "+behavior);
         primitiveAccess = new PrimitiveBehaviorControlDisconnectableWrapper(
               primitiveControl);
         compoundRealizer = behavior.createRealizer(primitiveAccess);
         compoundRealizer.addObserver(this);
         task = executor.submit(compoundRealizer);
      }

      public void stop () {
         mainLock.lock();
         try {
            cutPrimitiveBehaviorAccess();
            compoundRealizer.removeObserver(this);
            task.cancel(true);
            for (Resource r : behavior.getResources()) {
               stopPrimitiveOn(r);
               behaviorHandles.remove(r);
            }
         } finally {
            mainLock.unlock();
         }
      }

      private void cutPrimitiveBehaviorAccess () {
         primitiveAccess.disconnect();
      }

      @Override
      public void compoundRealizerDone (CompoundRealizer sender) {
         saveDoneBehaviorInHistory(behavior);
      }
   }
}
