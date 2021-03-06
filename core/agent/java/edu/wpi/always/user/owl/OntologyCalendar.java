package edu.wpi.always.user.owl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import edu.wpi.always.Always;
import edu.wpi.always.user.UserModelBase;
import edu.wpi.always.user.calendar.AbstractCalendar;
import edu.wpi.always.user.calendar.CalendarEntry;
import edu.wpi.always.user.calendar.CalendarEntryImpl;
import edu.wpi.always.user.calendar.CalendarEntryType;
import edu.wpi.always.user.calendar.CalendarEntryTypeManager;
import edu.wpi.always.user.calendar.RepeatingCalendarEntry;
import edu.wpi.always.user.calendar.RepeatingCalendarEntry.Frequency;
import edu.wpi.always.user.calendar.RepeatingCalendarEntryImpl;
import edu.wpi.always.user.people.Person;

public class OntologyCalendar extends AbstractCalendar {

   public static final String EVENT_CLASS = "Event";
   public static final String START_PROPERTY = "EventStart";
   public static final String TYPE_PROPERTY = "EventType";
   public static final String PEOPLE_PROPERTY = "EventPeople";
   @SuppressWarnings("unused")
   private static final String DURATION_PROPERTY = "EventDuration";
   @SuppressWarnings("unused")
   private static final String LOCATION_PROPERTY = "EventLocation";
   public static final String UUID_PROPERTY = "EventUUID";
   public static final String REPEATING_EVENT_CLASS = "RepeatingEvent";
   public static final String REPEATING_UUID_PROPERTY = "EventRepeatingUUID";
   public static final String REPEATING_START_DATE_PROPERTY = "RepeatingStartDate";
   public static final String REPEATING_END_DATE_PROPERTY = "RepeatingEndDate";
   public static final String REPEATING_START_TIME_PROPERTY = "RepeatingStartTime";
   public static final String REPEATING_DURATION_PROPERTY = "RepeatingDuration";
   public static final String REPEATING_FREQUENCY_PROPERTY = "RepeatingFrequency";
   private final OntologyHelper helper;
   private final OntologyPlaceManager placeHelper;
   private final OntologyPeopleManager peopleHelper;

   public OntologyCalendar (Ontology ontology,
         OntologyPlaceManager placeHelper, OntologyPeopleManager peopleHelper) {
      this.helper = new OntologyHelper(ontology);
      this.placeHelper = placeHelper;
      this.peopleHelper = peopleHelper;
   }

   public Set<OWLNamedIndividual> getAllEventInstances () {
      return helper.getAllOfClass(EVENT_CLASS);
   }

   @Override
   public Iterator<CalendarEntry> iterator () {
      return new Iterator<CalendarEntry>() {

         Iterator<OWLNamedIndividual> owlEntries = getAllEventInstances()
               .iterator();
         OntologyIndividual owlEntry = null;

         @Override
         public boolean hasNext () {
            return owlEntries.hasNext();
         }

         @SuppressWarnings("unused")
         @Override
         public CalendarEntry next () {
            owlEntry = new OntologyIndividual(helper.getOntologyDataObject(),
                  owlEntries.next());
            boolean isRepeat = owlEntry.hasSuperclass(REPEATING_EVENT_CLASS);
            DateTime start = owlEntry.getDataPropertyValue(START_PROPERTY)
                  .asDateTime();
            // NB: Duration and location not used!!
            ReadablePeriod duration = null; // owlEntry.getDataPropertyValue(DURATION_PROPERTY).asDuration();
            OntologyIndividual owlPlace = null; // owlEntry.getObjectPropertyValue(LOCATION_PROPERTY);
            OntologyPlace place = null;
            if ( owlPlace != null ) place = placeHelper.getPlace(owlPlace);
            CalendarEntryType type = CalendarEntryTypeManager.forName(owlEntry
                  .getDataPropertyValue(TYPE_PROPERTY).asString());
            String uuid = owlEntry.getDataPropertyValue(UUID_PROPERTY).asString();
            Set<Person> people = new HashSet<Person>();
            for (OWLNamedIndividual owlPerson : owlEntry
                  .getObjectPropertyValues(PEOPLE_PROPERTY))
               people.add(peopleHelper.getPerson(new OntologyIndividual(helper
                     .getOntologyDataObject(), owlPerson)));
            if ( isRepeat ) {
               String repeatingUUID = owlEntry.getDataPropertyValue(
                     REPEATING_UUID_PROPERTY).asString();
               LocalDate repeatStartDate = owlEntry.getDataPropertyValue(
                     REPEATING_START_DATE_PROPERTY).asDate();
               LocalDate repeatEndDate = owlEntry.getDataPropertyValue(
                     REPEATING_END_DATE_PROPERTY).asDate();
               LocalTime repeatStartTime = owlEntry.getDataPropertyValue(
                     REPEATING_START_TIME_PROPERTY).asTime();
               Period repeatDuration = owlEntry.getDataPropertyValue(
                     REPEATING_DURATION_PROPERTY).asDuration();
               Frequency repeatFrequency = Frequency.valueOf(owlEntry
                     .getDataPropertyValue(REPEATING_FREQUENCY_PROPERTY)
                     .asString());
               return new RepeatingCalendarEntryImpl(UUID.fromString(uuid),
                     type, people, place, start, duration,
                     UUID.fromString(repeatingUUID), repeatStartDate,
                     repeatEndDate, repeatStartTime, repeatDuration,
                     repeatFrequency);
            } else
               return new CalendarEntryImpl(UUID.fromString(uuid), type,
                     people, place, start, duration);
         }

         @Override
         public void remove () {
            if ( owlEntry != null ) {
               owlEntry.delete();
            }
         }
      };
   }

   @Override
   public void addEntry (CalendarEntry entry) {
      String id = entry.getId().toString();
      OntologyIndividual owlEntry = helper.getNamedIndividual(id);
      if ( entry instanceof RepeatingCalendarEntry ) {
         RepeatingCalendarEntry repeatingEntry = (RepeatingCalendarEntry) entry;
         owlEntry.addSuperclass(REPEATING_EVENT_CLASS);
         owlEntry.setDataProperty(REPEATING_UUID_PROPERTY,
               helper.getLiteral(repeatingEntry.getRepeatId().toString()));
         owlEntry.setDataProperty(REPEATING_START_DATE_PROPERTY,
               helper.getLiteral(repeatingEntry.getRepeatStartDate()));
         owlEntry.setDataProperty(REPEATING_END_DATE_PROPERTY,
               helper.getLiteral(repeatingEntry.getRepeatEndDate()));
         owlEntry.setDataProperty(REPEATING_START_TIME_PROPERTY,
               helper.getLiteral(repeatingEntry.getRepeatStartTime()));
         owlEntry.setDataProperty(REPEATING_DURATION_PROPERTY,
               helper.getLiteral(repeatingEntry.getRepeatDuration()));
         owlEntry.setDataProperty(REPEATING_FREQUENCY_PROPERTY,
               helper.getLiteral(repeatingEntry.getRepeat().name()));
         if ( Always.TRACE ) System.out.println(repeatingEntry.getRepeatId() + " - "
            + repeatingEntry.getRepeatStartDate() + " - "
            + repeatingEntry.getRepeatEndDate() + " - "
            + repeatingEntry.getRepeatStartTime() + " - "
            + repeatingEntry.getRepeatDuration() + " - "
            + repeatingEntry.getRepeat());
      } else
         owlEntry.addSuperclass(EVENT_CLASS);
      owlEntry.setDataProperty(START_PROPERTY,
            helper.getLiteral(entry.getStart()));
      /*  Not using durations or locations
      owlEntry.setDataProperty(DURATION_PROPERTY, 
            entry.getDuration() == null ? null : helper.getLiteral(entry.getDuration()));
      owlEntry.setObjectProperty(LOCATION_PROPERTY,
            (entry.getPlace() == null || entry.getPlace().getZip() == null ) ? null :
               placeHelper.getPlace(entry.getPlace().getZip()).getIndividual());
      */      
      owlEntry.setObjectProperty(PEOPLE_PROPERTY, null);// Clear all people
      for (Person person : entry.getPeople()) {
         owlEntry.addObjectProperty(PEOPLE_PROPERTY,
               peopleHelper.getPerson(person.getName()).getIndividual());
      }
      owlEntry.setDataProperty(TYPE_PROPERTY,
            helper.getLiteral(entry.getType().getId()));
      owlEntry.setDataProperty(UUID_PROPERTY, helper.getLiteral(id));
      UserModelBase.saveIf();
   }
}
