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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaalternativa.futures.AlternativeFuture;
import com.logicaalternativa.futures.AlternativePromise;
import com.logicaalternativa.futures.OnFailure;
import com.logicaalternativa.futures.OnSuccesful;

public class AlternativePromiseImp<T> implements AlternativePromise<T>{
	
	private static Logger logger = LoggerFactory.getLogger(AlternativePromiseImp.class);
	
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
				
		execOnSuccesful( future.getOnSuccesfulQueue(), value );
		
	}

	@Override
	public AlternativeFuture<T> future() {
		
		return future;
		
	}	
	
	private static void execOnFailure( BlockingQueue<OnFailure> onFailureQueue, Throwable error ) {
		
		if ( error == null ) {
			
			return;
			
		
		}
		
		while ( onFailureQueue != null
				&& ! onFailureQueue.isEmpty() ) {
			
			OnFailure onFailure = takeOfQueue( onFailureQueue );
				
			if ( onFailure != null ) {
				
				executeOnFailureApply( error, onFailure );
				
			}
					
			
		}		
		
	}

	private void execOnSuccesful( final BlockingQueue<OnSuccesful<T>> onSuccesfulQueue, final T value ) {
		
		if ( value == null ) {
			
			return;
		}
		
		executeQueueOnSuccesful( onSuccesfulQueue, value );
		
	}
	
	private void executeQueueOnSuccesful(
			final BlockingQueue<OnSuccesful<T>> onSuccesfulQueue, final T value) {
		
		while ( onSuccesfulQueue != null
				&& ! onSuccesfulQueue.isEmpty() ) {
			
			OnSuccesful<T> onSuccesful;
			
			onSuccesful = takeOfQueue( onSuccesfulQueue );
				
			if ( onSuccesful != null ) {
				
				executeOnsucessfulApply( onSuccesful, value);
				
			}
			
		}
	}
	
	private static void executeOnFailureApply(final Throwable error, final OnFailure onFailure) {
		try {
			
			onFailure.apply(  error );
			
		} catch (Exception e) {
			
			logger.error("Error to execute OnFailure", e);
			
		}
		
	}

	private void executeOnsucessfulApply (
			final OnSuccesful<T> onSuccesful, final T value) {
			
			try {
				
				onSuccesful.apply(value);
				
			} catch (Exception e) {
				
				logger.error("Error to execute OnSuccessful", e);
				
			}
			
	}
	
	private static <E> E takeOfQueue( BlockingQueue<E> queue ) {
		
		E take = null;
		
		try {
			
			take = queue.take();
			
		} catch (InterruptedException e) {
			
			logger.error("Error to take of queue", e);
		}
		
		return take;
		
	}
	
	private <E> void putOnQueue( BlockingQueue<E> queue, E element ) {
		
		try {
			
			queue.put(element);
			
		} catch (InterruptedException e) {
			
			logger.error("Error to put the value in the queue");
		}
		
		
	}
	
	
	private class AlternativeFutureImp implements AlternativeFuture<T> {
		
		private BlockingQueue<OnSuccesful<T>> onSuccesfulQueue;
	
		private BlockingQueue<OnFailure> onFailureQueue;

		public AlternativeFutureImp() {
			super();
			onSuccesfulQueue = new LinkedBlockingQueue<OnSuccesful<T>>();
			onFailureQueue = new LinkedBlockingQueue<OnFailure>();
		}

		@Override
		public void onSuccesful(OnSuccesful<T> function) {
			
			putOnQueue(onSuccesfulQueue, function );

			execOnSuccesful(onSuccesfulQueue, value);

			
		}
		@Override
		public void onFailure( OnFailure function ) {
			
			putOnQueue(onFailureQueue, function );
			
			execOnFailure( onFailureQueue, error );
			
		}
		
		protected BlockingQueue<OnSuccesful<T>> getOnSuccesfulQueue() {
			
			return onSuccesfulQueue;
		
		}

		protected BlockingQueue<OnFailure> getOnFailure() {
			
			return onFailureQueue;
		
		}
		
	}

}
