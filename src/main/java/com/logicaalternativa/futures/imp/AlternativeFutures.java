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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.logicaalternativa.futures.AlternativeFuture;

public class AlternativeFutures {
	

	public static <T> AlternativeFuture<List<T>> sequence(
			final List<AlternativeFuture<T>> listFutures) {
		
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		
		return sequenceRecursive( listFutures, listFutures.size() -1 , executorService);
	}
	
	private static  <T> AlternativeFuture<List<T>> sequenceRecursive(  final List<AlternativeFuture<T>> listFutures, final Integer index, final ExecutorService executorService ) {
		
		final AlternativeFuture<T> next = listFutures.get( index );
		
		if ( index == 0 ) {
			
			return next.map(s -> { 
						final List<T> list = new ArrayList<>();
						list.add( s );
						return list;
					}
			, executorService);
			
		}
		
		return next.zip( sequenceRecursive(listFutures, index -1, executorService ) )
			.map( s -> {					
					final List<T> list = new ArrayList<>( s.getB() );
					list.add( s.getA() );
					return list;
				}
			, executorService);		
		
	}

}
