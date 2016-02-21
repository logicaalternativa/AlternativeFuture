/*
 * AlternativeFutures.java
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
package com.logicaalternativa.futures.imp;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;

public class AlternativeFutures {
	
	public static <T> AlternativePromise<T> createPromise() {
		
		return new AlternativePromiseImp<T>();
		
	}
	
	public static <T> AlternativeFuture<T> successful( T value ) {
		
		AlternativePromise<T> promise = createPromise();
		
		promise.resolve( value );
		
		return promise.future();
		
	}
	
	public static <T> AlternativeFuture<T> failed( Throwable error ) {
		
		AlternativePromise<T> promise = createPromise();
		
		promise.reject( error );
		
		return promise.future();
		
	}

}
