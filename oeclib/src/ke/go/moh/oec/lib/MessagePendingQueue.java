/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is OpenEMRConnect.
 *
 * The Initial Developer of the Original Code is International Training &
 * Education Center for Health (I-TECH) <http://www.go2itech.org/>
 *
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */
package ke.go.moh.oec.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * Mechanism to wait for a response to a message.
 * When sending a message for which we expect a response, the message
 * is put in a pending queue. Then as messages are received they are
 * compared against the pending queue entries to see if they are a response.
 * <p>
 * The request thread will wait to see if a response comes. If it comes,
 * the request thread will be woken, and the response will be returned to
 * the caller. If no matching response comes, a timer will time out and
 * wake the request thread. It will then return with no matching response.
 *
 * @author Jim Grace
 */
final class MessagePendingQueue implements Runnable {

    private final int MESSAGE_TIMEOUT_SECONDS = 20;
    // TODO: Make this an optional property.

    class Entry {

        private long timeout;
        private Message request;
        private Message response;
    }
    private List<Entry> queue = new ArrayList<Entry>();
    private Thread timeoutThread = null;
    private long nextTimeout = 0;

    /**
     * Sleeps until the timeout arrives for the first pending message in the
     * queue. (All subsequent pending messages will have a later timeout.)
     * Then looks through the queue to see if there are any messages to time out.
     * (By now, hopefully they have been responded to.)
     * Finally, get the timeout again for the first pending message in the
     * queue (if any), and sleep again to wait for that one.
     */
    public void run() {
        nextTimeout = getNextTimeout();
        while (nextTimeout > 0) {
            long now = System.currentTimeMillis();
            if (nextTimeout > now) { // If we aren't there yet:
                try {
                    Thread.sleep(nextTimeout - now);
                } catch (InterruptedException ex) {
                }
            }
            timeOutEntries();
            nextTimeout = getNextTimeout();
        }
    }

    /**
     * Notify the thread that is waiting for this queue entry.
     * This is done either because a response has been found to the request,
     * or we have timed out and given up waiting.
     *
     * @param e
     */
    private void notify(Entry e) {
        synchronized (e) {
            e.notify();
        }
    }

    /**
     * Get the timeout time of the first entry in the pending queue,
     * or zero if the queue is empty. The first entry in the queue will
     * be the next one to time out, since it is the oldest.
     * <p>
     * This method is called by the timeout thread. If it returns 0
     * then the timeout thread will exit. So if it returns 0, it will
     * also set timeoutThread to null. This will signal that a new
     * timeoutThread must be allocated to run the timer again.
     *
     * @return the system time in milliseconds when the next timeout will occur,
     * or zero if there are no entries in the queue.
     */
    private synchronized long getNextTimeout() {
        if (queue.isEmpty()) {
            timeoutThread = null;
            return 0;
        } else {
            return queue.get(0).timeout;
        }
    }

    /**
     * Loops through all the entries in the pending queue, and time out
     * any that have now expired.
     */
    private synchronized void timeOutEntries() {
        long now = System.currentTimeMillis();
        for (Entry e : queue) {
            if (e.timeout <= now) {
                notify(e);
            }
        }
    }

    /**
     * Adds an entry to the pending queue.
     * <p>
     * The caller should add the message to the pending queue before sending
     * the message on the network. Then after sending the message, the caller
     * can come back and wait for the response. This is done to prevent a
     * race condition where the response may come back quickly, before the
     * caller has the chance to wait for it.
     * <p>
     * By queuing the message before sending it on the network, the message
     * will be matched with the response even if the response comes before
     * the caller has the chance to wait for it.
     *
     * @param request
     * @return the queue entry, for future reference.
     */
    synchronized Entry enqueue(Message request) {
        Entry e = new Entry();
        e.request = request;
        e.response = null;
        e.timeout = System.currentTimeMillis() + MESSAGE_TIMEOUT_SECONDS * 1000;
        queue.add(e);
        return e;
    }

    /**
     * Removes an entry from the pending queue.
     *
     * @param e the entry to remove.
     */
    synchronized void dequeue(Entry e) {
        queue.remove(e);
    }

    /**
     * Waits for a response to this queue entry. The thread will be
     * suspended until either a matching response is received, or
     * the message times out.
     *
     * @param e the queue entry for which to wait.
     * @return the response message if there was one, otherwise null
     */
    Message waitForResponse(Entry e) {
        //
        // Start a timeout thread. Synchronize on the current object so that
        // another process won't try to start it after we test it but before
        // we try to start it -- and also so that the thread won't finish
        // and go away before we look.
        //
        synchronized (this) {
            if (timeoutThread == null) {
                timeoutThread = new Thread(this);
                timeoutThread.start();
            }
        }
        //
        // Wait for the timeout. Also handle the extreme case where we got
        // a response before we even wait. In this case the response will
        // not be null, becuase it will already be posted. In this case
        // we don't have to wait.
        //
        synchronized (e) {
            if (e.response == null) {
                try {
                    e.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        dequeue(e);
        return e.response;
    }

    /**
     * Test a received message to see if is the response to a request.
     * If it is, wake the thread that is waiting for the response, and return true.
     * If it is not, return false.
     *
     * @param response the message that might be a response
     * @return true if the message was a response to something in the queue, otherwise false
     */
    synchronized boolean findRequest(Message response) {

        for (Entry e : queue) {
            if (e.request.getMessageId().equals(response.getMessageId())) {
                // To prevent a race condition, it is important that the following
                // two statements are done in the right order. First set the
                // response on the message. If the sending thread sees the
                // response posted, they will not wait. The worst that can
                // happen is that the notify() method does nothing.
                //
                // But if the notify method were first, then the sending thread
                // might come in and look for the response after we've notified.
                // Then it might sleep before we set the response. Then it would
                // sleep needlessly, delaying the response to the user.
                //
                // Alternatively, we could have put a "synchronized (e) {" block
                // around the next two statements, but Java might warn us that
                // we have nested synchronizations (which is a dangerous thing
                // in some situations even if it isn't here.) So to avoid the
                // warning, we just do the next two statements in the right
                // order and there is no problem.
                //
                e.response = response;
                notify(e);
                return true;
            }
        }
        return false;
    }
}
