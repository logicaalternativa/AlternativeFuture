package com.logicaalternativa.futures.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import com.logicaalternativa.futures.pojo.FunctionExecutorPojo;

public interface IManageQueue {

	<E> void putOnQueue(BlockingQueue<FunctionExecutorPojo<E>> queue, E element,
			Executor executor);

	<E> E takeOfQueue(BlockingQueue<E> queue);

}
