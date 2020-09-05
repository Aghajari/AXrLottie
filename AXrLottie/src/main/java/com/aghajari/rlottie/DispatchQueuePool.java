/*
 * Copyright (C) 2020 - Amir Hossein Aghajari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.aghajari.rlottie;

import android.os.SystemClock;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;

import androidx.annotation.UiThread;

class DispatchQueuePool {
    public static SecureRandom random = new SecureRandom();

    private LinkedList<DispatchQueue> queues = new LinkedList<>();
    private HashMap<DispatchQueue, Integer> busyQueuesMap = new HashMap<>();
    private LinkedList<DispatchQueue> busyQueues = new LinkedList<>();
    private int maxCount;
    private int createdCount;
    private int guid;
    private int totalTasksCount;
    private boolean cleanupScheduled;

    private Runnable cleanupRunnable = new Runnable() {
        @Override
        public void run() {
            if (!queues.isEmpty()) {
                long currentTime = SystemClock.elapsedRealtime();
                for (int a = 0, N = queues.size(); a < N; a++) {
                    DispatchQueue queue = queues.get(a);
                    if (queue.getLastTaskTime() < currentTime - 30000) {
                        queue.recycle();
                        queues.remove(a);
                        createdCount--;
                        a--;
                        N--;
                    }
                }
            }
            if (!queues.isEmpty() || !busyQueues.isEmpty()) {
                DispatchQueue.runOnUIThread(this, 30000);
                cleanupScheduled = true;
            } else {
                cleanupScheduled = false;
            }
        }
    };

    public DispatchQueuePool(int count) {
        maxCount = count;
        guid = random.nextInt();
    }

    @UiThread
    public void execute(final Runnable runnable) {
        final DispatchQueue queue;
        if (!busyQueues.isEmpty() && (totalTasksCount / 2 <= busyQueues.size() || queues.isEmpty() && createdCount >= maxCount)) {
            queue = busyQueues.remove(0);
        } else if (queues.isEmpty()) {
            queue = new DispatchQueue("DispatchQueuePool" + guid + "_" + random.nextInt());
            queue.setPriority(Thread.MAX_PRIORITY);
            createdCount++;
        } else {
            queue = queues.remove(0);
        }
        if (!cleanupScheduled) {
            DispatchQueue.runOnUIThread(cleanupRunnable, 30000);
            cleanupScheduled = true;
        }
        totalTasksCount++;
        busyQueues.add(queue);
        Integer count = busyQueuesMap.get(queue);
        if (count == null) {
            count = 0;
        }
        busyQueuesMap.put(queue, count + 1);
        queue.postRunnable(new Runnable() {
            @Override
            public void run() {
                runnable.run();
                DispatchQueue.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        totalTasksCount--;
                        int remainingTasksCount = busyQueuesMap.get(queue) - 1;
                        if (remainingTasksCount == 0) {
                            busyQueuesMap.remove(queue);
                            busyQueues.remove(queue);
                            queues.add(queue);
                        } else {
                            busyQueuesMap.put(queue, remainingTasksCount);
                        }
                    }
                });
            }
        });
    }
}
