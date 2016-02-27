package com.logicaalternativa.futures.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import com.logicaalternativa.futures.pojo.FunctionExecutorPojo;

public interface IManageQueue {

	<E> void putOnQueue(BlockingQueue<FunctionExecutorPojo<E>> queue, E element,
			ExecutorService executorService);

	<E> E takeOfQueue(BlockingQueue<E> queue);

}
