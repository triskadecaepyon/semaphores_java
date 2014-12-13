/**
 * The strategy for this Semaphores problem that I implemented is based off of the book's concept of
 * Pushers.  3 Different pushers acquire a material, lock the mutex, and see what other materials are present
 * (based upon what agents have released what items).
 * If other materials are present to make the Cigarettes, then the correct smoker is called. Otherwise,
 * all materials are unlocked and another pusher can check it, or another agent can release it.
 *
 * Implementation wise, you need 3 different Pushers as threads, 3 Additional Semaphores, and a mutex.
 * Boolean flags keep count an indication of what is on the table.
 */

/**
 * Created by davidliu on 7/26/14.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CSP {
    // agent
    static Semaphore agentSem = new Semaphore(1);
    static Semaphore tobacco = new Semaphore(0);
    static Semaphore paper = new Semaphore(0);
    static Semaphore matches = new Semaphore(0);

    static class agentA implements Runnable {
        public void run() {
            while (true) {
                try {
                    agentSem.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("agentA about to release tobacco and paper");
                tobacco.release();
                paper.release();
            }
        }
    }

    static class agentB implements Runnable {
        public void run() {
            while (true) {
                try {
                    agentSem.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("agentB about to release tobacco and matches");
                tobacco.release();
                matches.release();
            }
        }
    }

    static class agentC implements Runnable {
        public void run() {
            while (true) {
                try {
                    agentSem.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("agentC about to release paper and matches");
                paper.release();
                matches.release();
            }
        }
    }

    /**
     * As per assignment Guidelines, the Agents(above) do not change,
     * however the push solution in the book requires modification to
     * the holders of the materials (the smokers).
     *
     * Each one instead has a semaphore to represent what they hold
     * instead of attempting to acquire the other two materials.
     */
    static class smokerMatches implements Runnable {
        public void run() {
            while (true) {
                try {
                    matchSem.acquire();
//                    tobacco.acquire();
//                    paper.acquire();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("smoker with matches ready");
                agentSem.release();
            }
        }
    }

    static class smokerTobacco implements Runnable {
        public void run() {
            while (true) {
                try {
                    tobaccoSem.acquire();
//                    matches.acquire();
//                    paper.acquire();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("smoker with tobacco ready");
                agentSem.release();
            }
        }
    }

    static class smokerPaper implements Runnable {
        public void run() {
            while (true) {
                try {
                    paperSem.acquire();
//                    tobacco.acquire();
//                    matches.acquire();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("smoker with paper ready");
                agentSem.release();
            }
        }
    }

    public static void main(String[] args) {

        /**
         * New Threads representing the pushers, off of the book's
         * recommendation and structure of pushers.
         *
         * Each of the mutex calls looks to see if all materials are there.
         * If not, it releases the materials back to the table if it cannot
         * assemble them.
         *
         * The solution uses boolean flags to represent if materials are there.
         */
        Thread PusherA = new Thread() {
            public void run() {
                while (true) {
                    try {
                        //Acquire the Tobacco material
                        tobacco.acquire();
                        mutex.lock();
                        try {
                            if (isPaper) {
                                isPaper = false;
                                matchSem.release();
                            } else if (isMatch) {
                                isMatch = false;
                                paperSem.release();
                            } else {
                                isTobacco = true;
                            }
                        } finally {
                            mutex.unlock();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread PusherB = new Thread() {
            public void run() {
                while (true) {
                    try {
                        //Acquire the Paper material
                        paper.acquire();
                        mutex.lock();
                        try {
                            if (isTobacco) {
                                isTobacco = false;
                                matchSem.release();
                            } else if (isMatch) {
                                isMatch = false;
                                paperSem.release();
                            } else {
                                isPaper = true;
                            }
                        } finally {
                            mutex.unlock();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread PusherC = new Thread() {
            public void run() {
                while (true) {
                    try {
                        //Acquire the Matches material
                        matches.acquire();
                        mutex.lock();
                        try {
                            if (isPaper) {
                                isPaper = false;
                                tobaccoSem.release();
                            } else if (isTobacco) {
                                isTobacco = false;
                                paperSem.release();
                            } else {
                                isMatch = true;
                            }
                        } finally {
                            mutex.unlock();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        List<Thread> tList = new ArrayList<Thread>();
        tList.add(new Thread( new CSP.agentA()));
        tList.add(new Thread( new CSP.agentB()));
        tList.add(new Thread( new CSP.agentC()));
        tList.add(PusherA);
        tList.add(PusherB);
        tList.add(PusherC);
        tList.add(new Thread( new CSP.smokerMatches()));
        tList.add(new Thread( new CSP.smokerTobacco()));
        tList.add(new Thread( new CSP.smokerPaper()));

        for( Thread t : tList ) {
            t.start();
        }
        try {
            for ( Thread t : tList ) {
                t.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //The static variables require to make this entire solution work.
    static boolean isTobacco = false;
    static boolean isPaper = false;
    static boolean isMatch = false;

    static Semaphore tobaccoSem = new Semaphore(0);
    static Semaphore paperSem = new Semaphore(0);
    static Semaphore matchSem = new Semaphore(0);

    public static Lock mutex = new ReentrantLock();
}

