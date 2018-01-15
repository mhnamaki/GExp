package dataset.graphmaker;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class IMDBMovie {

	public String title;
	public HashSet<String> genre;
	public String year;
	public String rate;
	public String language;
	public HashMap<String, String> actor;//<actorName, characterName>
	public HashMap<String, String> actress;
	public HashSet<String> director;
	public String key; //title+year
	
//	public void setTitle(IMDBMovie imdbMovie, String title) {
//		imdbMovie.title = title;
//	}
//	
//	public void setGenre(IMDBMovie imdbMovie, HashSet<String> genre) {
//		imdbMovie.genre = genre;
//	}
//	
//	public void setYear(IMDBMovie imdbMovie, String year) {
//		imdbMovie.year = year;
//	}
//	
//	public void setRate(IMDBMovie imdbMovie, String rate) {
//		imdbMovie.rate = rate;
//	}
//	
//	public void setLanguage(IMDBMovie imdbMovie, String language) {
//		imdbMovie.language = language;
//	}
//	
//	public void setActor(IMDBMovie imdbMovie, HashMap<String, String> actor) {
//		imdbMovie.actor = actor;
//	}
//	
//	public void setActress(IMDBMovie imdbMovie, HashMap<String, String> actress) {
//		imdbMovie.actress = actress;
//	}
//	
//	public void setDirector(IMDBMovie imdbMovie, HashSet<String> director) {
//		imdbMovie.director = director;
//	}

}
