package dolziplib;

import java.util.LinkedList;

public class ScoreBoard {
	
	public class Score
	{
		public String name;
		public double point;
	}
	
	LinkedList<Score> scores = new LinkedList<>();
	
	public synchronized void mark(String name, double point)
	{
		for(Score score:scores)
		{
			if(score.name.equals(name))
			{
				score.point = point;
				return;
			}
		}
		
		Score s = new Score();
		s.name = name;
		s.point = point;
		
		this.scores.add(s);
	}
	
	public synchronized Score[] getScores()
	{
		Score[] scores = this.scores.toArray(new Score[0]);
		return scores;
	}
}
