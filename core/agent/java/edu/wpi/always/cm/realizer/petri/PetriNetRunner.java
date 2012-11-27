package edu.wpi.always.cm.realizer.petri;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import edu.wpi.always.cm.realizer.petri.Place.State;

public class PetriNetRunner implements Runnable {
	private ReentrantLock placeQueueLock = new ReentrantLock();
	private final Transition start;
	private final ConcurrentHashMap<Place, Future<?>> placesEnqueued = new ConcurrentHashMap<Place, Future<?>>();
	private final ExecutorService executor;
	private final CopyOnWriteArraySet<Place> failedPlaces = new CopyOnWriteArraySet<Place>();

	public PetriNetRunner(Transition start) {
		this.start = start;
		executor = Executors.newCachedThreadPool();
	}

	@Override
	public void run() {
		subscribeToAllPlaces();

		boolean ran = start.fireIfYouCan();

		assert ran : "the staring transition should be ready to just fire!";

		boolean continueWait = true;
		while (continueWait) {
			
			continueWait = false;

			for (Place p : placesEnqueued.keySet()) {
				Future<?> future = placesEnqueued.get(p);

				if (future.isDone()) {
					if (p.getState() != Place.State.ExecutionSuccessful)
						failedPlaces.add(p);
					continue;
				}

				try {
					System.out.println("\nPetriNetRunner: waiting for Place <" + p + ">");
					future.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
					// the exception does not necessary mean failure, i.e. the
					// Place behavior may have set success and then an exception
					// happened
					if (p.getState() == Place.State.ExecutionSuccessful) {
						System.out.println();
						System.out
								.println("WARNING: Place executed successfuly, but an ExecutionException was thrown: ");
						e.printStackTrace();
						System.out.println();
					} else {
						failedPlaces.add(p);
					}
				}

				continueWait = true;
			}			
		}
	}

	private void subscribeToAllPlaces() {
		PlaceObserver observer = new PlaceObserver() {
			@Override
			public void stateChanged(Place sender, State state) {
				switch (state) {
				case Activated:
					placeActivated(sender);
					break;
				case ExecutionFailed:
					failedPlaces.add(sender);
					break;
				default:
				}
			}
		};

		Set<Place> places = findAllPlacesRecursively(start);

		for (Place p : places)
			p.addObserver(observer);
	}

	protected void placeActivated(Place place) {
		// the lock is just to make sure we don't ever run a place twice.
		// even if run() was called many times.
		if (!placesEnqueued.containsKey(place)) {
			placeQueueLock.lock();

			try {
				if (!placesEnqueued.containsKey(place)) { //double check after getting the lock
					Future<?> future = enqueue(place);
					placesEnqueued.put(place, future);
				}
			} finally {
				placeQueueLock.unlock();
			}
		}
	}

	private Future<?> enqueue(final Place place) {
		return executor.submit(place);
	}

	private Set<Place> findAllPlacesRecursively(Transition transition) {
		HashSet<Place> set = new HashSet<Place>();
		findAllPlacesRecursively(transition, set);

		return set;
	}

	private void findAllPlacesRecursively(Transition transition,
			HashSet<Place> foundPlaces) {
		for (Place p : transition.getOutputs()) {
			if (!foundPlaces.contains(p)) {
				foundPlaces.add(p);

				for (Transition t : p.getOutputs())
					findAllPlacesRecursively(t, foundPlaces);
			}
		}
	}

	public Set<Place> getFailedPlaces() {
		return Collections.unmodifiableSet(failedPlaces);
	}

}