/*
 * ManageQueue.java
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logicaalternativa.futures.pojo.FunctionExecutorPojo;
import com.logicaalternativa.futures.util.IManageQueue;

public class ManageQueue implements IManageQueue {
	
	private static IManageQueue manageQueueInstance;
	
	private static Logger logger = LoggerFactory.getLogger(ManageQueue.class);
	
	private ManageQueue(){
		
	}
	
	
	public static IManageQueue getInstance(){
		
		if ( manageQueueInstance == null ) {
			
			manageQueueInstance = new ManageQueue();
			
		}
		
		return manageQueueInstance;
		
	}
	
	@Override
	public <E> void putOnQueue( BlockingQueue<FunctionExecutorPojo<E>> queue, E element, final Executor executor) {
		
		try {
			
			final FunctionExecutorPojo<E> functionExecutorPojo = new FunctionExecutorPojoImp<E>(element, executor);
			
			queue.put(functionExecutorPojo);
			
		} catch (InterruptedException e) {
			
			logger.error("Error to put the value in the queue");
		}	
		
	}
	
	@Override
	public <E> E takeOfQueue( BlockingQueue<E> queue ) {
		
		E take = null;
		
		try {
			
			take = queue.take();
			
		} catch (InterruptedException e) {
			
			logger.error("Error to take of queue", e);
		}
		
		return take;
		
	}
	
	private class FunctionExecutorPojoImp<E> implements FunctionExecutorPojo<E> {

		private E function;
		
		private Executor executor;
		
		@Override
		public E getFunction() {
			return function;
		}

		@Override
		public Executor getExecutor() {
			return executor;
		}

		public FunctionExecutorPojoImp(E function,
				Executor executor) {
			super();
			this.function = function;
			this.executor = executor;
		}
		
	}
	
	
	

}
