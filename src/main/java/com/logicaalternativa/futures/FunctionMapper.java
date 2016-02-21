package com.logicaalternativa.futures;

public interface FunctionMapper<T,U> {

	abstract U map( T result );	
	
}
