/*
 * AlternativeFuture.java
 * 
 * Copyright 2016 Miguel Rafael Esteban Martin <miguel.esteban@logicaalternativa.com>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * 
 * 
 */
package com.logicaalternativa.futures;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import com.logicaalternativa.futures.pojo.AlternativeTuple;

public interface AlternativeFuture<T> extends Monad<T>{
	
	abstract void onSuccesful( final Consumer<T> function, final Executor executor );
	
	abstract void onFailure ( final Consumer<Throwable> function, final Executor executor );

	abstract <U> AlternativeFuture<U> map( final Function<T, U> mapper, final Executor executor );

	abstract <U> AlternativeFuture<U> flatMap( final Function<T, AlternativeFuture<U>> mapper, final Executor executor );

	abstract <U> AlternativeFuture<AlternativeTuple<T, U>> zip( final AlternativeFuture<U> otherfuture );
	
	abstract <U> AlternativeFuture<U> map( final Function<T, U> mapper );

	abstract <U> AlternativeFuture<U> flatMap( final Function<T, Monad<U>> mapper );
	
	abstract <U> AlternativeFuture<U> pure( U value );
	
	
	public static <T> AlternativeFuture<T> successful( T value ) {
		
		AlternativePromise<T> promise = AlternativePromise.createPromise();
		
		promise.resolve( value );
		
		return promise.future();
		
	}
	
	public static <T> AlternativeFuture<T> failed( Throwable error ) {
		
		AlternativePromise<T> promise = AlternativePromise.createPromise();
		
		promise.reject( error );
		
		return promise.future();
		
	}
}
