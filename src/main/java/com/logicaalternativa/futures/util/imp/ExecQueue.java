/*
 * ExecQueue.java
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
package com.logicaalternativa.futures.util.imp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaalternativa.futures.pojo.FunctionExecutorPojo;
import com.logicaalternativa.futures.util.IExecQueue;
import com.logicaalternativa.futures.util.IManageQueue;

public class ExecQueue implements IExecQueue {
	
	private static Logger logger = LoggerFactory.getLogger( ExecQueue.class );
	
	private static IExecQueue iExecQueue;
	
	private ExecQueue(){
		
		
	}
	
	public static IExecQueue getInstance() {
		
		if ( iExecQueue == null ) {
			
			iExecQueue = new ExecQueue();
			
		}
		
		return iExecQueue;
		
	}
	
	@Override
	public <E>  void executeQueue(
			final BlockingQueue<FunctionExecutorPojo<Consumer<E>>> onSuccesfulQueue, final E value, final IManageQueue iManageQueue) {
		
		while ( onSuccesfulQueue != null
				&& ! onSuccesfulQueue.isEmpty() ) {
			
			FunctionExecutorPojo<Consumer<E>> fuctionPojo = iManageQueue.takeOfQueue( onSuccesfulQueue );
				
			if ( fuctionPojo != null ) {
				
				executeFucntionApply( fuctionPojo.getFunction(), value, fuctionPojo.getExecutor() );
				
			}
			
		}
		
	}
	
	private <E> void executeFucntionApply (
			final Consumer<E> functionApply, final E value, Executor executor) {
		
		
			executor.execute( () -> {
		
				try {
					
					functionApply.accept(value);
					
				} catch (Exception e) {
					
					logger.error("Error to execute FunctionApply", e);
					
				}
				
			});
			
	}

}
