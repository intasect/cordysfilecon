/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
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
