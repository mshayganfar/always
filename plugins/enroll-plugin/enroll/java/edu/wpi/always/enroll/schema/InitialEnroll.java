package edu.wpi.always.enroll.schema;

import edu.wpi.disco.rt.menu.*;
import edu.wpi.always.user.people.Person;
import edu.wpi.always.enroll.schema.EditPersonState.*;
import edu.wpi.always.enroll.schema.EnrollAdjacencyPairs.*;

public class InitialEnroll extends EnrollAdjacencyPairImpl {

   @Override
   public void enter () {
      EditPersonState.editingSelf = false;
      ErrorCheckState.resetErrorCheckingMainPrompt();
   }
   
   public InitialEnroll (final EnrollStateContext context) {
      
      super("<FACE EXPR=\"SMILE\"> I'm ready for you to tell me about your family and friends <FACE EXPR=\"WARM\">", context);
      
      choice("Sounds good", new DialogStateTransition(){ 

            @Override
            public AdjacencyPair run() {
               return new PersonNameAdjacencyPair(context);
            }
         });

      choice("Stop telling information", new DialogStateTransition() {

         @Override
         public AdjacencyPair run () {
            return new DialogEndEvent(context);
         }
      });
      choice("I want to edit my own information", new DialogStateTransition() {

         @Override
         public AdjacencyPair run () {
            Person person = getContext()
                  .getUserModel().getPeopleManager().getUser();
            if(person == null)
               return new ClarifyNoOwnProfile(getContext());
            EditPersonState.editingSelf = true;
            EditPersonState.prompt = "Ok, Here is what I know about you";
            return new EditPersonAdjacencyPair(getContext(), person);
         }
      });
      choice("I want to edit someone else's information", new DialogStateTransition() {

         @Override
         public AdjacencyPair run () {
            EditPersonState.editingSelf = false;
            if(getContext().getPeopleManager().getPeople(false).length > 0)
               return new PeopleSelectEvent(context);
            return new ClarifyNoFriendsToEdit(context);
         }
      });
   }
   
   public static class ClarifyNoOwnProfile extends EnrollAdjacencyPairImpl{

      public ClarifyNoOwnProfile(final EnrollStateContext context) {
         super("Oh! I forgot to ask you to enter your profile first"
            + " After you do, you can edit it.", context);
         choice("Oh, ok", new DialogStateTransition() {

            @Override
            public AdjacencyPair run() {
               return new InitialEnroll(context);
            }
         });
      }
   }
   
   public static class ClarifyNoFriendsToEdit extends EnrollAdjacencyPairImpl{

      public ClarifyNoFriendsToEdit(final EnrollStateContext context) {
         super("Well, I still do not know about any of your friends or family,"
            + " After you tell me about some people, you can edit their profile here", context);
         choice("Oh, ok", new DialogStateTransition() {

            @Override
            public AdjacencyPair run() {
               return new InitialEnroll(context);
            }
         });
      }
   }


   public static class DialogEndEvent extends EnrollAdjacencyPairImpl{

      public DialogEndEvent(final EnrollStateContext context) {
         super("<FACE EXPR=\"SMILE\"> Okay, we can do this again if you want <FACE EXPR=\"WARM\">", context);
         choice("Sure", new DialogStateTransition() {

            @Override
            public AdjacencyPair run() {
               context.getSchema().stop();
               return null;
            }
         });
      }
   }

   public static class PeopleSelectEvent extends EnrollAdjacencyPairImpl {

      public PeopleSelectEvent (final EnrollStateContext context) {
         super("Tell me whose profile do you want to edit", context);
         Person[] people = getContext().getPeopleManager().getPeople(false);
         for(final Person person : people){
            if(person == null) continue; //user himself returned as null (use of false above)
            choice(person.getName(), new DialogStateTransition() {
               @Override
               public AdjacencyPair run() {
                  EditPersonState.prompt = "Ok, here is what I know about " + person.getName();
                  return new EditPersonAdjacencyPair(getContext(), person);
               }
            });
         }
         choice("Finished editing", new DialogStateTransition() {
            @Override
            public AdjacencyPair run() {
               return new InitialEnroll(getContext());
            }
         });
      }
   }
}
