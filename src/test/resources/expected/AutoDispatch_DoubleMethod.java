package test;

import java.lang.String;

final class AutoDispatch_DoubleMethod {

  private AutoDispatch_DoubleMethod() {
  }

  static String description(DoubleMethod self, Person person) {
    Age value = self.ageDispatch(person);
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

  static String height(DoubleMethod self, Person person) {
    Height value = self.heightDispatch(person);
    if (value == null) {
      throw new NullPointerException("dispatch value == null");
    }
    if (value == test.Height.Short) {
      return self.heightShort(person);
    }
    if (value == test.Height.Tall) {
      return self.heightTall(person);
    } else {
      throw new UnsupportedOperationException("no method for value == " + value);
    }
  }
}
