package com.logicaalternativa.futures;

import java.util.function.Function;


public interface Monad<T> {
	
	abstract <U> Monad<U> map( final Function<T, U> mapper );

	abstract <U> Monad<U> flatMap( final Function<T, Monad<U>> mapper );
	
	abstract <U> Monad<U> pure( U value );

}
