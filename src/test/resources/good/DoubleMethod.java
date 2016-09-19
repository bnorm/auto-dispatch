package test;

import com.bnorm.auto.dispatch.AutoDispatch;

public class DoubleMethod {

  @DescriptionMulti(Age.Young)
  public String descriptionYoung(Person person) {
    return "You're so young!";
  }

  @DescriptionMulti(Age.Old)
  public String descriptionOld(Person person) {
    return "You're quite old...";
  }

  @DescriptionMulti.Dispatcher
  public Age ageDispatch(Person person) {
    return person.years < 60 ? Age.Young : Age.Old;
  }

  @AutoDispatch(multi = DescriptionMulti.class, dispatcher = DescriptionMulti.Dispatcher.class)
  public String description(Person person) {
    return AutoDispatch_DoubleMethod.description(this, person);
  }

  @HeightMulti(Height.Short)
  public String heightShort(Person person) {
    return "You're quire short...";
  }

  @HeightMulti(Height.Tall)
  public String heightTall(Person person) {
    return "You're so tall!";
  }

  @HeightMulti.Dispatcher
  public Height heightDispatch(Person person) {
    return person.height < 60 ? Height.Short : Height.Tall;
  }

  @AutoDispatch(multi = HeightMulti.class, dispatcher = HeightMulti.Dispatcher.class)
  public String height(Person person) {
    return AutoDispatch_DoubleMethod.height(this, person);
  }
}
