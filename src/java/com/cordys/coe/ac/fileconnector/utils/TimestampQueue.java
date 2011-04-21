
/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys File Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.cordys.coe.ac.fileconnector.utils;

import java.util.PriorityQueue;

/**
 * Queue with elements sorted with a timestamp.
 *
 * @author  mpoyhone
 */
public class TimestampQueue<E>
{
    /**
     * Contains the queued elements..
     */
    private PriorityQueue<Entry<E>> queue;

    /**
     * Constructor for TimestampQueue.
     */
    public TimestampQueue()
    {
        super();
        this.queue = new PriorityQueue<Entry<E>>();
    }

    /**
     * Constructor for TimestampQueue.
     *
     * @param  initialCapacity  the initial capacity for the priority queue.
     */
    public TimestampQueue(int initialCapacity)
    {
        super();
        this.queue = new PriorityQueue<Entry<E>>(initialCapacity);
    }

    /**
     * Adds a new element to this queue.
     *
     * @param   entry      e Element to be added.
     * @param   timestamp  Entry timestamp.
     *
     * @return  <code>true</code>
     */
    public boolean add(E entry, long timestamp)
    {
        return queue.add(new Entry<E>(timestamp, entry));
    }

    /**
     * Returns the number of elements in the queue.
     *
     * @return  Number of elements.
     */
    public int size()
    {
        return queue.size();
    }

    /**
     * Returns the first expired entry.
     *
     * @param   currentTime  Current time.
     * @param   remove       If <code>true</code>, the entry is removed from the queue.
     *
     * @return  First expired entry or <code>null</code> if no entry is available.
     */
    public E getFirst(long currentTime, boolean remove)
    {
        Entry<E> tmp = queue.peek();

        if ((tmp == null) || (tmp.timestamp > currentTime))
        {
            return null;
        }

        if (remove)
        {
            tmp = queue.remove();
        }

        return tmp.data;
    }

    /**
     * Queue entry class.
     *
     * @author  mpoyhone
     */
    private static class Entry<T>
        implements Comparable<Entry<T>>
    {
        /**
         * Actual entry data.
         */
        private T data;
        /**
         * Entry timestamp.
         */
        private long timestamp;

        /**
         * Constructor for Entry.
         *
         * @param  timestamp
         * @param  data
         */
        public Entry(long timestamp, T data)
        {
            super();
            this.timestamp = timestamp;
            this.data = data;
        }

        /**
         * @see  java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Entry<T> other)
        {
            if (other == null)
            {
                return 1;
            }

            return (int) (timestamp - other.timestamp);
        }
    }
}
