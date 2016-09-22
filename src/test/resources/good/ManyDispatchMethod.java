package test;

import com.bnorm.auto.dispatch.AutoDispatch;

public class ManyDispatchMethod {

  @DescriptionMulti(Age.Young)
  public String descriptionYoung(Person person) {
    return "You're so young!";
  }

  @DescriptionMulti({Age.Old})
  public String descriptionOld(Person person) {
    return "You're quite old...";
  }

  @DescriptionMulti({Age.Young, Age.Old, Age.VeryOld})
  public String descriptionDefault(Person person) {
    return "I don't know what you are";
  }

  @DescriptionMulti.Dispatcher
  public Age descriptionDispatch(Person person) {
    return person.years < 60 ? Age.Young : Age.Old;
  }

  @AutoDispatch(multi = DescriptionMulti.class, dispatcher = DescriptionMulti.Dispatcher.class)
  public String description(Person person) {
    return AutoDispatch_ManyDispatchMethod.description(this, person);
  }
}
