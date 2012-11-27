package edu.wpi.always.cm.realizer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import edu.wpi.always.cm.*;

public class PrimitiveBehaviorControlImpl implements PrimitiveBehaviorControl, PrimitiveRealizerObserver {

	public static final int REALIZERS_INTERVAL = 300; // in milliseconds
	public static final int NUM_THREADS = 2;

	private final PrimitiveRealizerFactory factory;
	private final Map<Resource, PrimitiveRealizer<?>> realizersInEffect;
	private final Map<PrimitiveRealizer<?>, ScheduledFuture<?>> runningTasks;
	private final ScheduledExecutorService executor;
	private final CopyOnWriteArrayList<PrimitiveBehaviorControlObserver> observers = new CopyOnWriteArrayList<PrimitiveBehaviorControlObserver>();

	ReentrantLock lock = new ReentrantLock();
	private final IdleBehaviors nullPrimitives;

	public PrimitiveBehaviorControlImpl(PrimitiveRealizerFactory factory, IdleBehaviors nullPrimitives) {
		this.nullPrimitives = nullPrimitives;
		realizersInEffect = new HashMap<Resource, PrimitiveRealizer<?>>();
		runningTasks = new HashMap<PrimitiveRealizer<?>, ScheduledFuture<?>>();
		this.factory = factory;

		executor = Executors.newScheduledThreadPool(NUM_THREADS);
	}

	@Override
	public PrimitiveRealizerHandle realize(PrimitiveBehavior behavior) {
		lock.lock();

		try {
			PrimitiveRealizer<?> currentRealizer = realizersInEffect.get(behavior.getResource());

			if (currentRealizer == null
					|| !currentRealizer.getParams().equals(behavior))
				currentRealizer = runPrimitive(behavior);

			if (currentRealizer != null)
				return new PrimitiveRealizerHandleImpl(behavior, currentRealizer, this);
			else
				return alreadyDoneRealizerHandle(behavior);
		} finally {
			lock.unlock();
		}
	}

	private PrimitiveRealizerHandle alreadyDoneRealizerHandle(final PrimitiveBehavior behavior) {
		return new PrimitiveRealizerHandle() {

			@Override
			public void waitUntilDoneOrCanceled(long timeout) throws InterruptedException, ExecutionException, TimeoutException {
			}

			@Override
			public void waitUntilDoneOrCanceled() throws InterruptedException, ExecutionException {
			}

			@Override
			public boolean isRunning() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public PrimitiveBehavior getBehavior() {
				return behavior;
			}
		};
	}

	private PrimitiveRealizer<?> runPrimitive(PrimitiveBehavior primitiveBehavior) {
		lock.lock();
		try {
			Resource resource = primitiveBehavior.getResource();
			stopRealizerOnResource(resource);

			if (primitiveBehavior.equals(PrimitiveBehavior.nullBehavior(resource)))
				return null;

			PrimitiveRealizer<?> pbRealizer = factory.create(primitiveBehavior);
			realizersInEffect.put(primitiveBehavior.getResource(), pbRealizer);

			pbRealizer.addObserver(this);

			schedule(pbRealizer);

			return pbRealizer;
		} finally {
			lock.unlock();
		}
	}

	private void stopRealizerOnResource(Resource resource) {
		lock.lock();
		PrimitiveRealizer<?> realizer;
		try {
			realizer = realizersInEffect.get(resource);
			if (realizer != null) {
				stop(realizer);

				realizersInEffect.remove(resource);
			}
		} finally {
			lock.unlock();
		}
	}

	private void stop(PrimitiveRealizer<?> realizer) {
		lock.lock();
		try {
			realizer.removeObserver(this);
			realizer.shutdown();

			runningTasks.get(realizer).cancel(true);

			runningTasks.remove(realizer);
		} finally {
			lock.unlock();
		}
	}

	private void schedule(PrimitiveRealizer<?> realizer) {
		lock.lock();

		try {
			ScheduledFuture<?> future = executor.scheduleWithFixedDelay(realizer, 0,
					REALIZERS_INTERVAL, TimeUnit.MILLISECONDS);

			runningTasks.put(realizer, future);
		} finally {
			lock.unlock();
		}
	}

	public PrimitiveRealizer<?> currentRealizerFor(Resource resource) {
		return realizersInEffect.get(resource);
	}

	public void addObserver(PrimitiveBehaviorControlObserver listener) {
		observers.add(listener);
	}

	@Override
	public void removeObserver(PrimitiveBehaviorControlObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void prmitiveRealizerDone(PrimitiveRealizer<?> realizer) {
		fireDone(realizer.getParams());
	}

	private void fireDone(PrimitiveBehavior pb) {
		for (PrimitiveBehaviorControlObserver o : observers) {
			o.primitiveDone(this, pb);
		}
	}

	@Override
	public void stop(final Resource resource) {
		lock.lock();
		try {
			if (!nullRealizerInEffect(resource)) {
				stopRealizerOnResource(resource);
				
				Runnable runNullIfStillNoRealizer = new Runnable() {
					public void run() {
						if (!realizersInEffect.containsKey(resource) && !nullRealizerInEffect(resource)) {
							lock.lock();
							try {
								// double check after lock acquired
								if (!realizersInEffect.containsKey(resource) && !nullRealizerInEffect(resource)) {
									runNullRealizerOnResource(resource);
								}
							} finally {
								lock.unlock();
							}
						}
					}
				};
				
				executor.schedule(runNullIfStillNoRealizer, 50, TimeUnit.MILLISECONDS);
			}
		} finally {
			lock.unlock();
		}
	}

	private boolean nullRealizerInEffect(Resource resource) {
		PrimitiveRealizer<?> realizer = realizersInEffect.get(resource);
		if (realizer != null) {
			if (realizer.getParams().equals(nullPrimitives.get(resource)))
				return true;
		}

		return false;
	}

	private void runNullRealizerOnResource(Resource resource) {
		PrimitiveBehavior p = nullPrimitives.get(resource);
		if (p != null)
			realize(p);
	}

}