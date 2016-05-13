# Alternative Futures

This library implements Promise & Future Pattern. It's written in Java 8 and it is strongly is inspired by API Scala futures.

AlternativeFutures exports part of the functionality and some functions of the futures composition of Scala.

Alternative futures supports:
+ Callbacks
   - onSuccesful
   - onFailure
+ Composition futures: Monards
   - map
   - zip
   - flatMap
+ Auxiliary methods
   - sequence

You also can create completed futures with a right value
```
AlternativeFuture<T> future = AlternativeFutures.successful( T value );
```
or with a failure value:
```
AlternativeFuture<T> future = failed( Throwable error );
```


