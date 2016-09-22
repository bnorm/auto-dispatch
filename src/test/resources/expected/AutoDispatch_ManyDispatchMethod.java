package test;

import java.lang.String;
import java.util.Arrays;

final class AutoDispatch_ManyDispatchMethod {
  private AutoDispatch_ManyDispatchMethod() {
  }

  static String description(ManyDispatchMethod self, Person person) {
    Age value = self.descriptionDispatch(person);
    if (value == null) {
      throw new NullPointerException("dispatch value == null");
    } if (value == test.Age.Young) {
      return self.descriptionYoung(person);
    } if (value == test.Age.Old) {
      return self.descriptionOld(person);
    } if (Arrays.asList(test.Age.Young, test.Age.Old, test.Age.VeryOld).contains(value)) {
      return self.descriptionDefault(person);
    } else {
      throw new UnsupportedOperationException("no method for value == " + value);
    }
  }
}
