package testfingerprint;

import com.griaule.grfingerjava.GrFingerJavaException;
import com.griaule.grfingerjava.MatchingContext;
import com.griaule.grfingerjava.Template;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jim Grace
 */
public class Match implements Runnable {

	private Template template = null;
	private List<Template> templateList = null;
	private int startIndex = 0;
	private int endIndex = 0;
	private int matchCount = 0;
	private int unmatchCount = 0;
	private int minMatchScore = 9999;
	private int maxMatchScore = 0;
	private int minUnmatchScore = 9999;
	private int maxUnmatchScore = 0;
	private long elapsedTime;
	private Thread thread;

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
	}

	public int getMaxMatchScore() {
		return maxMatchScore;
	}

	public void setMaxMatchScore(int maxMatchScore) {
		this.maxMatchScore = maxMatchScore;
	}

	public int getMaxUnmatchScore() {
		return maxUnmatchScore;
	}

	public void setMaxUnmatchScore(int maxUnmatchScore) {
		this.maxUnmatchScore = maxUnmatchScore;
	}

	public int getMinMatchScore() {
		return minMatchScore;
	}

	public void setMinMatchScore(int minMatchScore) {
		this.minMatchScore = minMatchScore;
	}

	public int getMinUnmatchScore() {
		return minUnmatchScore;
	}

	public void setMinUnmatchScore(int minUnmatchScore) {
		this.minUnmatchScore = minUnmatchScore;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public List<Template> getTemplateList() {
		return templateList;
	}

	public void setTemplateList(List<Template> templateList) {
		this.templateList = templateList;
	}

	public int getUnmatchCount() {
		return unmatchCount;
	}

	public void setUnmatchCount(int unmatchCount) {
		this.unmatchCount = unmatchCount;
	}

	public void run() {
		MatchingContext matchingContext = null;
		try {
			matchingContext = new MatchingContext();
		} catch (GrFingerJavaException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
		try {
			matchingContext.prepareForIdentification(template);
		} catch (GrFingerJavaException ex) {
			Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
		}
		long startTime = System.currentTimeMillis();
		boolean matched = false;
		int score = 0;
		for (int i = startIndex; i < endIndex; i++) {
			Template referenceTemplate = templateList.get(i);
			try {
				// matched = matchingContext.verify(template, referenceTemplate);
				matched = matchingContext.identify(referenceTemplate);
			} catch (GrFingerJavaException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IllegalArgumentException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			}
			try {
				score = matchingContext.getScore();
			} catch (GrFingerJavaException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			}
			if (matched) {
				matchCount++;
				if (score < minMatchScore) {
					minMatchScore = score;
				}
				if (score > maxMatchScore) {
					maxMatchScore = score;
				}
			} else {
				unmatchCount++;
				if (score < minUnmatchScore) {
					minUnmatchScore = score;
				}
				if (score > maxUnmatchScore) {
					maxUnmatchScore = score;
				}
			}
			// System.out.println("Matched " + matched + " score " + score);
		}
		elapsedTime = System.currentTimeMillis() - startTime;
	}
}
