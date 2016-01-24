/*
 * AlternativePromiseImp.java
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;
import com.logicaalternativa.futures.OnFailure;
import com.logicaalternativa.futures.OnSuccesful;

public class AlternativePromiseImp<T> implements AlternativePromise<T>{
	
	private  Logger logger = LoggerFactory.getLogger(getClass());
	
	private T value;
	
	private Throwable error;
	
	private AlternativeFutureImp future;
	
	public AlternativePromiseImp() {
		
		future = new AlternativeFutureImp();
		
	}

	@Override
	public synchronized void reject(Throwable err) {
		
		validatePromiseIsResovedYet();
		
		error = err;
		
		execOnFailure( future.getOnFailure(), err );
		
	}

	private void validatePromiseIsResovedYet() {
		
		if ( error != null
				|| value != null ) {
			
			throw new IllegalStateException("The promise is already resolved");
			
		}
		
	}

	@Override
	public synchronized void resolve(T val) {
		
		validatePromiseIsResovedYet();
		
		value = val;
				
		execOnSuccesful( future.getOnSuccesful(), future.getOnFailure(), value );
		
	}

	@Override
	public AlternativeFuture<T> future() {
		
		return future;
		
	}	
	
	private void execOnFailure( OnFailure onFailure, Throwable error ) {
		
		if ( error != null
				&& onFailure != null ) {
			
			onFailure.apply(error);
			
		}		
		
	}
	
	private void execOnSuccesful( final OnSuccesful<T> onSuccesful, final OnFailure onFailure, final T value ) {
		
		if ( value != null
				&& onSuccesful != null ) {
			
			try {
				
				onSuccesful.apply(value);
				
			} catch (Exception e) {
				
				logger.error("Error to execute OnSuccessful", e);
				
			}
			
		}
		
		
	}
	
	
	public class AlternativeFutureImp implements AlternativeFuture<T> {
		
		private OnSuccesful<T> onSuccesful;
	
		private OnFailure onFailure;

		@Override
		public void onSuccesful(OnSuccesful<T> function) {
			
			onSuccesful = function;
			
			execOnSuccesful( onSuccesful, onFailure, value );
			
		}

		@Override
		public void onFailure( OnFailure function ) {
			
			onFailure = function;
			
			execOnFailure( onFailure, error );
			
		}
		
		protected OnSuccesful<T> getOnSuccesful() {
			
			return onSuccesful;
		
		}

		protected OnFailure getOnFailure() {
			
			return onFailure;
		
		}
		
	}

}
