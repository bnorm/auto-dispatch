# auto-dispatch

AutoDispatch is a multimethod implementation for Java using an annotation processor.

The base annotations describe the layout of the methods or classes to which will be dispatched.

```java
@interface AutoDispatch {
    Class<? extends Annotation> multi();

    Class<? extends Annotation> dispatcher();
}
```
An example usage might look something like this.

```java
public class Person {
    int years;
}

enum Age {
    Young,
    Old
}

@interface DescriptionMulti {
    Age value();

    @interface Dispatcher {
    }
}

class MethodExample {
    @AutoDispatch(multi = DescriptionMulti.class, dispatcher = DescriptionMulti.Dispatcher.class)
    public String description(Person person) {
        return AutoDispatch_MethodExample.description(this, person);
    }

    @DescriptionMulti.Dispatcher
    public Age descriptionDispatch(Person person) {
        return person.years < 60 ? Age.Young : Age.Old;
    }

    @DescriptionMulti(Age.Young)
    public String descriptionYoung(Person person) {
        return "You're so young!";
    }

    @DescriptionMulti(Age.Old)
    public String descriptionOld(Person person) {
        return "You're quite old...";
    }
}
```
With a generated class that looks something like this.

```java
class AutoDispatch_MethodExample {
    static String description(MethodExample self, Person person) {
        Age age = self.descriptionDispatch(person);
        if (age == Age.Young) {
            return self.descriptionYoung(person);
        } else if (age == Age.Old) {
            return self.descriptionOld(person);
        } else {
            throw new UnsupportedOperationException(
                    "Unable to dispatch Example.description(person=" + person + ")");
        }
    }
}
```

An example of class dispatching might be as follows.

```java
@interface ExampleMulti {
    Age value();
}

@interface ExampleDispatcher {
}

@AutoDispatch(multi = ExampleMulti.class, dispatcher = ExampleDispatcher.class)
abstract class ClassExample implements Callable<String> {
    @ExampleDispatcher
    Age dispatch(Person person) {
        return person.years < 60 ? Age.Young : Age.Old;
    }

    public static ClassExample create(Person person) {
        return new AutoDispatch_ClassExample(person);
    }
}

class YoungPerson implements Callable<String> {
    @ExampleMulti(Age.Young)
    public YoungPerson(Person person) {
    }

    @Override
    public String call() throws Exception {
        return "You're so young!";
    }
}

class OldPerson implements Callable<String> {
    @ExampleMulti(Age.Old)
    public OldPerson(Person person) {
    }

    @Override
    public String call() throws Exception {
        return "You're quite old...";
    }
}
```

With a generated class looking like this:

```java
final class AutoDispatch_ClassExample extends ClassExample {
    private final Callable<String> stringCallable;

    AutoDispatch_ClassExample(Person person) {
        Age age = dispatch(person);
        if (age == Age.Young) {
            YoungPerson youngPerson = new YoungPerson(person);
            this.stringCallable = youngPerson;
        } else if (age == Age.Old) {
            OldPerson oldPerson = new OldPerson(person);
            this.stringCallable = oldPerson;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String call() throws Exception {
        return stringCallable.call();
    }
}
```

If you want to dispatch to the same methods or classes but use a different dispatch method, simply define a new dispatch annotation.

```java
@interface ExampleDispatcher2 {
}

@AutoDispatch(multi = ExampleMulti.class, dispatcher = ExampleDispatcher2.class)
abstract class ClassExample2 implements Callable<String> {
    @ExampleDispatcher2
    Age dispatch(Person person) {
        return person.years < 65 ? Age.Young : Age.Old;
    }

    public static ClassExample2 create(Person person) {
        return new AutoDispatch_ClassExample2(person);
    }
}

final class AutoDispatch_ClassExample2 extends ClassExample2 {
    private final Callable<String> stringCallable;

    public AutoDispatch_ClassExample2(Person person) {
        Age age = dispatch(person);
        if (age == Age.Young) {
            YoungPerson youngPerson = new YoungPerson(person);
            this.stringCallable = youngPerson;
        } else if (age == Age.Old) {
            OldPerson oldPerson = new OldPerson(person);
            this.stringCallable = oldPerson;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String call() throws Exception {
        return stringCallable.call();
    }
}
```

## License

    Copyright 2016 Brian Norman

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

