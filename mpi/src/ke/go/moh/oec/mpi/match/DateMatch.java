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
package ke.go.moh.oec.mpi.match;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.mpi.Scorecard;

/**
 * Represents a date value for matching.
 *
 * @author Jim Grace
 */
public class DateMatch {

    private static final SimpleDateFormat yearFormat = new SimpleDateFormat("y");
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("M");
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("d");
    private static final Queue<Calendar> calendarCache = new LinkedList<Calendar>();
    private static long baseDate;
    private static int today = 0;
    private Date date = null;
    private int year = 0;
    private int yearMonth = 0;
    private int yearMonthDay = 0;

    static { // Make sure that baseDate is always set to Jan 1, 1800.
        Calendar calendar = Calendar.getInstance();
        calendar.set(1800, 1, 1);
        baseDate = calendar.getTimeInMillis();
    }

    /**
     * Construct a DateMatch from a Date.
     * <p>
     * Information about the date is extracted and stored ahead of time for quick matching.
     * For dates coming from the database, this information is extracted when all the
     * database values are loaded into memory. Then a database value can be compared
     * more quickly with multiple searches. For dates coming from the search terms,
     * this information is extracted before comparing the search terms with all
     * the database values. Then a search term can be compared more quickly with
     * multiple database values.
     *
     * @param d The date that will be matched with.
     */
    public DateMatch(Date d) {
        date = d;
        if (d != null) {
            Calendar calendar;
            synchronized (calendarCache) {
                calendar = calendarCache.poll(); // Get a cached calendar object if there is one.
            }
            if (calendar == null) {
                calendar = Calendar.getInstance(); // If not, allocate a new calendar object.
            }
            calendar.setTime(d);
            year = calendar.get(Calendar.YEAR);
            yearMonth = year * 12 + calendar.get(Calendar.MONTH);
            long time = calendar.getTimeInMillis();
            yearMonthDay = (int) ((time - baseDate) / (24 * 60 * 60 * 1000));
            synchronized (calendarCache) {
                calendarCache.add(calendar);        // Add (or return) calendar to the cache.
            }
        }
    }

    /**
     * Sets the current day, to be used in computing ages from a date
     * until today. This method should be called at least once a day
     * before any search is done that day. For convenience, it may just be
     * called before any search.
     * <p>
     * The current day is set to the number of days since January 1, 1800.
     */
    synchronized public static void setToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        long todayDate = calendar.getTimeInMillis();
        today = (int) ((todayDate - baseDate) / (24 * 60 * 60 * 1000));
    }

    /**
     * Compares two dates and compute the score of their relative match.
     * This object is the search term, the other object is the database value.
     * <p>
     * An exact match scores 1.0. A match within the same month scores 0.9.
     * A match within the same year scores 0.8.
     * <p>
     * Dates that are not within the same year are scored as follows:
     * The "age" of a date is defined as the amount of time from the date
     * to today (just as the current age of a person is the amount of time
     * from their birthdate to today.) The relative difference in age is computed
     * as a fraction of the absolute difference in age divided by the
     * larger of the two ages. Then a score is computed in the scale of 0-0.7
     * according to this fraction.
     *
     * @param s Scorecard in which to add the result.
     * @param dm Contains the date to match against.
     */
    public void score(Scorecard s, DateMatch dm) {
        if (date == null) {
            s.addScore(Scorecard.SEARCH_TERM_MISSING_WEIGHT, 0, Scorecard.SearchTerm.MISSING);
        } else if (dm.date == null) {
            s.addScore(Scorecard.MPI_VALUE_MISSING_WEIGHT, 0);
        } else {
            double score = 1.0;     // Score if we have a perfect match.
            if (yearMonthDay != dm.yearMonthDay) { // Buf if we don't have a perfect match...
                if (yearMonth == dm.yearMonth) {
                    score = 0.9;
                } else if (year == dm.year) {
                    score = 0.8;
                } else {
                    int maxDate = dm.yearMonthDay;
                    int minDate = yearMonthDay;
                    if (yearMonthDay > maxDate) {
                        maxDate = yearMonthDay;
                        minDate = dm.yearMonthDay;
                    }
                    int diff = maxDate - minDate;
                    int age = today - minDate;
                    if (age >= 0 && diff < age) {
                        score = (0.7 * (age - diff)) / age;
                    } else {
                        score = 0.0;
                    }
                }
            }
            double weight = Scorecard.DATE_MATCH_WEIGHT;
            if (score == 0) {
                weight = Scorecard.DATE_MISS_WEIGHT;
            }
            s.addScore(weight, score);
            if (Mediator.testLoggerLevel(Level.FINEST)) {
                Mediator.getLogger(DateMatch.class.getName()).log(Level.FINEST,
                        "Score {0},{1} total {2},{3},{4} comparing {5} with {6}",
                        new Object[]{score, weight, s.getTotalScore(), s.getTotalWeight(), s.getSearchTermScore(), date, dm.date});
            }
        }
    }
}
