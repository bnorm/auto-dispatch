package test;

import java.lang.String;

final class AutoDispatch_BasicMethod {

  private AutoDispatch_BasicMethod() {
  }

  static String description(BasicMethod self, Person person) {
    Age value = self.descriptionDispatch(person);
    if (value == null) {
      throw new NullPointerException("dispatch value == null");
    }
    if (value == test.Age.Young) {
      return self.descriptionYoung(person);
    }
    if (value == test.Age.Old) {
      return self.descriptionOld(person);
    } else {
      throw new UnsupportedOperationException("no method for value == " + value);
    }
  }
}
