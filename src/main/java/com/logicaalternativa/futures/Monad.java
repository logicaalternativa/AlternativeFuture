package com.logicaalternativa.futures;


public interface Monad<T> {
	
	abstract <U> Monad<U> map( final FunctionMapper<T, U> mapper );

	abstract <U> Monad<U> flatMap( final FunctionMapper<T, Monad<U>> mapper );
	
	abstract <U> Monad<U> pure( U value );

}
