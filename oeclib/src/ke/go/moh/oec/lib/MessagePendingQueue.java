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
 *
 * @author Jim Grace
 */
final class MessagePendingQueue implements Runnable {

    private final int MESSAGE_TIMEOUT_SECONDS = 20;

    class Entry {

        private long timeout;
        private Message request;
        private Message response;
    }
    private List<Entry> queue = new ArrayList<Entry>();
    private Thread timeoutThread = new Thread(this);
    private long nextTimeout = 0;

    public void run() {
        nextTimeout = getNextTimeout();
        while (nextTimeout > 0) {
            long now = System.currentTimeMillis();
            try {
                Thread.sleep(nextTimeout - now);
            } catch (InterruptedException ex) {
            }
            timeOutEntries();
            nextTimeout = getNextTimeout();
        }
    }

    private void notify(Entry e) {
        synchronized (e) {
            e.notify();
        }
    }

    private synchronized long getNextTimeout() {
        if (queue.isEmpty()) {
            return 0;
        } else {
            return queue.get(0).timeout;
        }
    }

    private synchronized void timeOutEntries() {
        long now = System.currentTimeMillis();
        for (Entry e : queue) {
            if (e.timeout <= now) {
                notify(e);
            }
        }
    }

    protected synchronized Entry enqueue(Message request) {
        Entry e = new Entry();
        e.request = request;
        e.response = null;
        e.timeout = System.currentTimeMillis() + MESSAGE_TIMEOUT_SECONDS * 1000;
        queue.add(e);
        return e;
    }

    protected synchronized void dequeue(Entry e) {
        queue.remove(e);
    }

    protected Message waitForResponse(Entry e) {
        synchronized (e) {
            if (e.response == null) {
                if (!timeoutThread.isAlive()) {
                    timeoutThread.start();
                }
                try {
                    e.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        dequeue(e);
        return e.response;
    }

    protected synchronized boolean findRequest(Message response) {

        for (Entry e : queue) {
            if (e.request.getMessageId().equals(response.getMessageId())) {
                e.response = response;
                notify(e);
                return true;
            }
        }
        return false;
    }
}
