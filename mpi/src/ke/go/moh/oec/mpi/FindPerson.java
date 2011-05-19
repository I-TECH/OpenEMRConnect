/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.mpi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;

/**
 * Searches the database for one or more candidates persons matching
 * a given set of search terms.
 * 
 * @author Jim Grace
 */
public class FindPerson {

    private static final int MAX_THREAD_COUNT = 2;

    /**
     * Searches the database for one or more candidates persons matching
     * a given set of search terms.
     *
     * @param personMatchList List of persons in which to search.
     * @param req Request containing the search terms to look for.
     * @return The response data to the request.
     */
    public PersonResponse find(List<PersonMatch> personMatchList, PersonRequest req) {
        PersonResponse resp = new PersonResponse();
        Person p = req.getPerson();
        if (p == null) {
            Logger.getLogger(FindPerson.class.getName()).log(Level.SEVERE, "FIND PERSON called with no person data.");
            return resp;
        }
        PersonMatch searchTerms = new PersonMatch(p);
        CandidateSet candidateSet = new CandidateSet();
        DateMatch.setToday();

        int personMatchCount = personMatchList.size();
        int threadCount = MAX_THREAD_COUNT;
        if (threadCount > personMatchCount) {
            threadCount = personMatchCount;
        }
        int countPerThread = (personMatchCount + threadCount - 1) / threadCount;
        long startTime = System.currentTimeMillis();
        List<Thread> threadArray = new ArrayList<Thread>();
        for (int i = 0; i < threadCount; i++) {
            int startIndex = countPerThread * i;
            int endIndex = (countPerThread * (i + 1)) - 1;
            if (endIndex >= personMatchCount) {
                endIndex = personMatchCount - 1;
            }
            FindPersonThread fpt = new FindPersonThread(personMatchList, searchTerms, candidateSet, startIndex, endIndex);
            Thread t = new Thread(fpt);
            threadArray.add(t);
            t.start();
        }
        for (Thread t : threadArray) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(FindPerson.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        List<Person> candidateList = candidateSet.export();
        resp.setPersonList(candidateList);
        resp.setSuccessful(true);
        return resp;
    }
}
