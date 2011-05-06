package testfingerprint;

import com.griaule.grfingerjava.GrFingerJava;
import com.griaule.grfingerjava.GrFingerJavaException;
import com.griaule.grfingerjava.MatchingContext;
import com.griaule.grfingerjava.Template;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jim Grace
 */
public class Main {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		final int maxTrial = 10000;
		final int threadCount = 1;
		File directory = new File(".");
		String dirName = directory.getAbsolutePath();
		GrFingerJava.setNativeLibrariesDirectory(directory);
		long startTime, elapsedTime;
		try {
			GrFingerJava.setLicenseDirectory(directory);
		} catch (GrFingerJavaException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
		int maximumNumberOfContexts = MatchingContext.getMaximumNumberOfContexts();
		MatchingContext matchingContext = null;
		try {
			matchingContext = new MatchingContext();
		} catch (GrFingerJavaException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}

		startTime = System.currentTimeMillis();
		List<Template> templateList = Database.loadTemplates(maxTrial);
		elapsedTime = System.currentTimeMillis() - startTime;
		int templateCount = templateList.size();
		System.out.println("Loaded " + templateCount + " database templates in " + elapsedTime + " milliseconds.");
		Template testTemplate = templateList.get(1);
		int minMatchScore = 9999;
		int maxMatchScore = 0;
		int minUnmatchScore = 9999;
		int maxUnmatchScore = 0;
		int matchCount = 0;
		int unmatchCount = 0;
		boolean matched = false;
		int score = 0;
		List<Match> matchList = new ArrayList<Match>();
		int countPerThread = (templateCount + threadCount - 1) / threadCount;
		startTime = System.currentTimeMillis();
		for (int i = 0; i < threadCount; i++) {
			Match match = new Match();
			match.setTemplateList(templateList);
			match.setTemplate(testTemplate);
			match.setStartIndex(countPerThread * i);
			int endIndex = countPerThread * (i+1);
			if (endIndex > templateCount) {
				endIndex = templateCount;
			}
			match.setEndIndex(endIndex);
			Thread thread = new Thread(match);
			match.setThread(thread);
			matchList.add(match);
		}
		for (Match m : matchList) {
			Thread t = m.getThread();
			t.start();
		}
		for (Match m : matchList) {
			Thread t = m.getThread();
			try {
				t.join(); // Wait for this thread to complete.
			} catch (InterruptedException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		elapsedTime = System.currentTimeMillis() - startTime;
		for (Match m : matchList) {
			System.out.println("Range " + m.getStartIndex() + "-" + (m.getEndIndex() - 1) + ":" );
			System.out.println("      " + (m.getMatchCount() + m.getUnmatchCount()) + " comparisons in " + m.getElapsedTime() + " milliseconds.");
			System.out.println("      Matched " + m.getMatchCount() + ", scores " + m.getMinMatchScore() + "-" + m.getMaxMatchScore());
			System.out.println("      Ummatched " +  m.getUnmatchCount() + ", scores " + m.getMinUnmatchScore() + "-" + m.getMaxUnmatchScore());
		}
	}
}
