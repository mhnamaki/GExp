package dataset.graphmaker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class CreateIMDBCSVToImport {

	private static String moviePath = "/Users/zhangxin/Desktop/IMDB/movies.list";
	private static String ratingPath = "/Users/zhangxin/Desktop/IMDB/ratings.list";
	private static String directorPath = "/Users/zhangxin/Desktop/IMDB/directors.list";
	private static String genresPath = "/Users/zhangxin/Desktop/IMDB/genres.list";
	private static String languagePath = "/Users/zhangxin/Desktop/IMDB/language.list";
	private static String actorPath = "/Users/zhangxin/Desktop/IMDB/actors.list";
	private static String actressPath = "/Users/zhangxin/Desktop/IMDB/actresses.list";

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-moviePath")) {
				moviePath = args[++i];
			} else if (args[i].equals("-ratingPath")) {
				ratingPath = args[++i];
			} else if (args[i].equals("-directorPath")) {
				directorPath = args[++i];
			} else if (args[i].equals("-genresPath")) {
				genresPath = args[++i];
			} else if (args[i].equals("-languagePath")) {
				languagePath = args[++i];
			} else if (args[i].equals("-actorPath")) {
				actorPath = args[++i];
			} else if (args[i].equals("-actressPath")) {
				actressPath = args[++i];
			}

		}
		CreateIMDBCSVToImport imdb = new CreateIMDBCSVToImport();
		HashMap<String, IMDBMovie> IMDBMovieMap = imdb.getIMDBMovieSet(imdb);
		
	}

	public void writeIntoCSV(HashMap<String, IMDBMovie> IMDBMovieMap, HashSet<String> actorSet,
			HashSet<String> actressSet, HashSet<String> directorSet, HashSet<String> genreSet) throws Exception {
		System.out.println("writing");
		File movie = new File("Movie.csv");
		FileOutputStream fosMovie = new FileOutputStream(movie);
		BufferedWriter bwMovie = new BufferedWriter(new OutputStreamWriter(fosMovie));
		bwMovie.write("KEY:ID(Movie-ID),title,year,rating,language,:LABEL\n");

		File actorF = new File("Actor.csv");
		FileOutputStream fosActor = new FileOutputStream(actorF);
		BufferedWriter bwActor = new BufferedWriter(new OutputStreamWriter(fosActor));
		bwActor.write("NAME:ID(Actor-ID),:LABEL\n");

		File actressF = new File("Actress.csv");
		FileOutputStream fosActress = new FileOutputStream(actressF);
		BufferedWriter bwActress = new BufferedWriter(new OutputStreamWriter(fosActress));
		bwActress.write("NAME:ID(Actress-ID),:LABEL\n");

		File directorF = new File("Director.csv");
		FileOutputStream fosDirector = new FileOutputStream(directorF);
		BufferedWriter bwDirector = new BufferedWriter(new OutputStreamWriter(fosDirector));
		bwDirector.write("NAME:ID(Director-ID),:LABEL\n");

		File genreF = new File("Genre.csv");
		FileOutputStream fosGenreF = new FileOutputStream(genreF);
		BufferedWriter bwGenre = new BufferedWriter(new OutputStreamWriter(fosGenreF));
		bwGenre.write("NAME:ID(Genre-ID),:LABEL\n");

		File movieActor = new File("Movie_Actor.csv");
		FileOutputStream fosMOActor = new FileOutputStream(movieActor);
		BufferedWriter bwMoActor = new BufferedWriter(new OutputStreamWriter(fosMOActor));
		bwMoActor.write(":Start_ID(Movie-ID),:End_ID(Actor-ID),characterName,:TYPE\n");

		File movieActress = new File("Movie_Actress.csv");
		FileOutputStream fosMOActress = new FileOutputStream(movieActress);
		BufferedWriter bwMoActress = new BufferedWriter(new OutputStreamWriter(fosMOActress));
		bwMoActress.write(":Start_ID(Movie-ID),:End_ID(Actress-ID),characterName,:TYPE\n");

		File movieDirector = new File("Movie_Director.csv");
		FileOutputStream fosMODi = new FileOutputStream(movieDirector);
		BufferedWriter bwMoDi = new BufferedWriter(new OutputStreamWriter(fosMODi));
		bwMoDi.write(":Start_ID(Movie-ID),:End_ID(Director-ID),:TYPE\n");

		File movieGnere = new File("Movie_Genre.csv");
		FileOutputStream fosMOGenre = new FileOutputStream(movieGnere);
		BufferedWriter bwMoGenre = new BufferedWriter(new OutputStreamWriter(fosMOGenre));
		bwMoGenre.write(":Start_ID(Movie-ID),:End_ID(Genre-ID),:TYPE\n");

		// bwActor.write("NAME:ID,:LABEL\n");
		for (String name : actorSet) {
			String nameC = CleanStr(name);
			bwActor.write(nameC + "," + "actor\n");
		}
		bwActor.close();

		// bwActress.write("NAME:ID,:LABEL\n");
		for (String name : actressSet) {
			String nameC = CleanStr(name);
			bwActress.write(nameC + "," + "actress\n");
		}
		bwActress.close();

		// bwDirector.write("NAME:ID,:LABEL\n");
		for (String name : directorSet) {
			String nameC = CleanStr(name);
			bwDirector.write(nameC + "," + "director\n");
		}
		bwDirector.close();

		// bwGenre.write("NAME:ID,:LABEL\n");
		for (String name : genreSet) {
			String nameC = CleanStr(name);
			bwGenre.write(nameC + "," + "genre\n");
		}
		bwGenre.close();

		for (IMDBMovie imdbMoive : IMDBMovieMap.values()) {
			String keyC = CleanStr(imdbMoive.key);
			String titleC = CleanStr(imdbMoive.title);
			String yearC = CleanStr(imdbMoive.year);
			String rateC = "";
			if(imdbMoive.rate != null) {
				rateC = CleanStr(imdbMoive.rate);
			}
			String languageC = "";
			if (imdbMoive.language != null){
				languageC = CleanStr(imdbMoive.language);
			}
			// bwMovie.write("KEY:ID,title,year,rating,language,:LABEL\n");
			bwMovie.write(keyC + "," + titleC + "," + yearC + "," + rateC + ","
					+ languageC + "movie\n");

			HashMap<String, String> actor = imdbMoive.actor;// <actorName,
															// characterName>
			// bwMoActor.write(":Start_ID,:End_ID,characterName,:TYPE\n");
			if(actor != null){
				for (String actorName : actor.keySet()) {
					String actorNameC = CleanStr(actorName);
					String characterNameC = CleanStr(actor.get(actorName));
					bwMoActor.write(keyC + "," + actorNameC + "," + characterNameC + "," + "actor_of\n");
				}
			}

			// bwMoActress.write(":Start_ID,:End_ID,characterName,:TYPE\n");
			HashMap<String, String> actress = imdbMoive.actress;
			if(actress != null) {
				for (String actressName : actress.keySet()) {
					String actressNameC = CleanStr(actressName);
					String characterNameC = CleanStr(actress.get(actressName));
					bwMoActress.write(
							keyC + "," + actressNameC + "," + characterNameC + "," + "actress_of\n");
				}
			}
			
			//bwMoDi.write(":Start_ID,:End_ID,:TYPE\n");
			HashSet<String> director = imdbMoive.director;
			if(director != null) {
				for (String directorName : director){
					String directorNameC = CleanStr(directorName);
					bwMoDi.write(keyC + "," + directorNameC + "," + "director_of\n");
				}
			}
			
			//bwMoGenre.write(":Start_ID,:End_ID,:TYPE\n");
			HashSet<String> genre = imdbMoive.genre;
			if(genre !=null) {
				for (String genreType : genre){
					String genreC = CleanStr(genreType);
					bwMoGenre.write(keyC + "," + genreC + "," + "genre_of\n");
				}
			}
		}
		bwMovie.close();
		bwMoActor.close();
		bwMoActress.close();
		bwMoDi.close();
		bwMoGenre.close();
		System.out.println("finish writting");
	}

	public static HashMap<String, IMDBMovie> getIMDBMovieSet(CreateIMDBCSVToImport imdb) throws Exception {
		System.out.println("begin generate IMDB map");
		HashSet<String> actorSet = new HashSet<String>();
		HashSet<String> actressSet = new HashSet<String>();
		HashSet<String> directorSet = new HashSet<String>();
		HashSet<String> genreSet = new HashSet<String>();

		HashMap<String, IMDBMovie> IMDBMovieMap = imdb.readMovie();
		IMDBMovieMap = imdb.setRateNameYear(IMDBMovieMap);
		IMDBMovieMap = imdb.setGenres(IMDBMovieMap);
		IMDBMovieMap = imdb.setLanguage(IMDBMovieMap);
		IMDBMovieMap = imdb.setDirector(IMDBMovieMap);
		IMDBMovieMap = imdb.setActor(IMDBMovieMap);
		IMDBMovieMap = imdb.setActress(IMDBMovieMap);
		System.out.println(IMDBMovieMap.size());

		for (String key : IMDBMovieMap.keySet()) {
			IMDBMovie imdbMovie = IMDBMovieMap.get(key);

			HashMap<String, String> actor = imdbMovie.actor;// <actorName,
															// characterName>
			HashMap<String, String> actress = imdbMovie.actress;
			HashSet<String> director = imdbMovie.director;
			HashSet<String> genre = imdbMovie.genre;
			
			if(actor != null){
				actorSet.addAll(actor.keySet());
			}
			if(actress != null){
				actressSet.addAll(actress.keySet());
			}
			if(director != null) {
				directorSet.addAll(director);
			}
			if(genre !=null) {
				genreSet.addAll(genre);
			}

			
		}
		
		System.out.println("begin generate IMDB map and set");

		imdb.writeIntoCSV(IMDBMovieMap, actorSet, actressSet, directorSet, genreSet);

		return IMDBMovieMap;
	}

	public HashMap<String, IMDBMovie> readMovie() {
		HashMap<String, IMDBMovie> IMDBMovieMap = new HashMap<String, IMDBMovie>();
		System.out.println("Begin title and year");
		File file = new File(moviePath);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));

			BufferedReader br = new BufferedReader(reader);
			String temline = "";
			String line = "";
			while ((temline = br.readLine()) != null) {
				if (temline.contains("MOVIES LIST")) {
					line = br.readLine();// ====
					line = br.readLine();// empty
					line = br.readLine();
					temline = line;
				}
				if (!line.isEmpty()) {
					line = temline;
					int begin = line.indexOf("\"");
					int end = line.lastIndexOf("\"");
					if (!line.contains("{") && begin != end) {
						String title = line.substring(begin + 1, end);
						IMDBMovie imdbMovie = new IMDBMovie();
						imdbMovie.title = title;
						line = line.substring(end + 1);
						int beginYear = line.indexOf("(");
						int endYear = line.indexOf(")");
						String year = line.substring(beginYear + 1, endYear);
						if (year.contains("?")) {
							year = "";
						}
						imdbMovie.year = year;
						String key = title + "_" + year;
						imdbMovie.key = key;
						IMDBMovieMap.put(key, imdbMovie);
					}
				}
			}

			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finish title and year");
		return IMDBMovieMap;
	}

	public HashMap<String, IMDBMovie> setRateNameYear(HashMap<String, IMDBMovie> IMDBMovieMap) {
		System.out.println("begin setting rate");
		File file = new File(ratingPath);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader br = new BufferedReader(reader);
			String temline = "";
			String line = "";
			while ((temline = br.readLine()) != null) {
				if (temline.contains("MOVIE RATINGS REPORT")) {
					line = br.readLine();// empty
					line = br.readLine();// title line
					line = br.readLine();
					temline = line;
				}
				if (!line.isEmpty()) {
					line = temline;
					int begin = line.indexOf("\"");
					int end = line.lastIndexOf("\"");
					if (!line.contains("{") && begin != end) {
						String[] data = line.trim().split(" ");
						String newLine = "";
						for (int j = 0; j < data.length; j++) {
							if (!data[j].equals("")) {
								newLine = newLine + data[j] + " ";
							}
						}
						String[] newData = newLine.trim().split(" ");
						String rate = newData[2];
						int beginIndex = newLine.indexOf("\"");
						int endIndex = newLine.lastIndexOf("\"");
						String title = newLine.substring(beginIndex + 1, endIndex);
						newLine = newLine.substring(endIndex + 1);

						int beginIndexDate = newLine.indexOf("(");
						int endIndexDate = newLine.indexOf(")");
						String year = newLine.substring(beginIndexDate + 1, endIndexDate);
						if (year.contains("/")) {
							int index = year.indexOf("/");
							year = year.substring(0, index);
						}
						if (year.contains("?")) {
							year = "";
						}
						String key = title + "_" + year;
						if (IMDBMovieMap.containsKey(key)) {
							// IMDBMovieMap.get(title).year = year;
							IMDBMovieMap.get(key).rate = rate;
						}
					}
				}
			}

			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finish rate");
		return IMDBMovieMap;
	}

	public HashMap<String, IMDBMovie> setDirector(HashMap<String, IMDBMovie> IMDBMovieMap) {
		System.out.println("begin setting director");
		File file = new File(directorPath);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader br = new BufferedReader(reader);
			String temline = "";
			String line = "";
			// director
			String directorName = "";
			// movie list
			HashSet<String> movies = new HashSet<String>();
			while ((temline = br.readLine()) != null) {
				if (temline.equals("THE DIRECTORS LIST")) {
					line = br.readLine();// =====
					line = br.readLine();// empty
					line = br.readLine();// Name
					line = br.readLine();// ---
					line = br.readLine();// content
					temline = line;
				}
				if (!line.isEmpty()) {
					if (!temline.isEmpty()) {
						line = temline;
						String[] data = line.split("\t");
						if (!data[0].isEmpty()) {
							// change 'Abd Al-Hamid, Ja'far -> Jafar Abd
							// Al-Hamid
							String temDirector = data[0].trim();
							String[] temDiList = temDirector.replaceAll("'", "").trim().split(",");
							directorName = temDiList[temDiList.length - 1].trim() + temDiList[0].trim();
							// get first movie
							String[] temMovie = data[data.length - 1].trim().split("\\(");
							String movieTitle = temMovie[0].trim().replaceAll("\"", "");

							// set year
							String tem = data[data.length - 1];
							if (tem.contains("{")) {// remove {} part
								int indexRemove = tem.indexOf("{");
								if (indexRemove > tem.indexOf("(")) {
									tem = tem.substring(0, indexRemove - 1);
								}
							}
							// System.out.println("data[data.length - 1] ->" +
							// data[data.length - 1]);
							int beginYear = tem.indexOf("(");
							int endYear = tem.trim().lastIndexOf(")");
							int indexRemove = tem.indexOf("{");
							String year = "";
							if (beginYear < endYear) {
								year = tem.trim().substring(beginYear + 1, endYear);
								if (year.contains("(")) {
									endYear = year.lastIndexOf(")");
									if (!year.contains(")")) {
										beginYear = year.indexOf("(");
										year = year.substring(beginYear + 1);
									} else {
										year = year.substring(0, endYear);
									}
									if (year.contains("/")) {// address [2012)
																// (co-director)]
										int index = year.indexOf("/");
										year = year.substring(0, index);
									} else if (year.contains("(")) {// address
																	// [Festival
																	// Edition)
																	// (2005]
										beginYear = year.lastIndexOf("(");
										year = year.substring(beginYear + 1);
										if (year.contains("(") && year.contains(")")) {
											beginYear = year.lastIndexOf("(");
											endYear = year.lastIndexOf(")");
											year = year.substring(beginYear + 1, endYear);
										}

									}

								}
								if (year.contains("/")) {
									int index = year.indexOf("/");
									year = year.substring(0, index);
								} else if (year.contains("?")) {
									year = "";
								}
							}
							String key = movieTitle + "_" + year;

							if (!movieTitle.isEmpty()) {
								movies.add(key);
							}
						} else {
							String temMovieLine = "";
							for (int i = 0; i < data.length; i++) {
								if (!data[i].isEmpty()) {
									temMovieLine = data[i];
									break;
								}
							}
							// String temMovieLine = data[3];
							String[] temMovie = temMovieLine.trim().split("\\(");
							String movieTitle = temMovie[0].trim().replaceAll("\"", "");

							// set year
							String tem = data[data.length - 1];
							if (tem.contains("{")) {
								// TODO: remove {} part
								int indexRemove = tem.indexOf("{");
								tem = tem.substring(0, indexRemove - 1);
							}
							// System.out.println("empty start data[data.length
							// - 1] ->" + tem);
							int beginYear = tem.indexOf("(");
							int endYear = tem.trim().lastIndexOf(")");
							String year = "";
							if (beginYear < endYear) {
								year = tem.trim().substring(beginYear + 1, endYear);
								if (year.contains("(")) {
									endYear = year.lastIndexOf(")");
									if (!year.contains(")")) {
										beginYear = year.indexOf("(");
										year = year.substring(beginYear + 1);
									} else {
										year = year.substring(0, endYear);
									}
									if (year.contains("/")) {// address[ 2012)
																// (co-director)]
										int index = year.indexOf("/");
										year = year.substring(0, index);
									} else if (year.contains("(")) {// address[Festival
																	// Edition)
																	// (2005]
										beginYear = year.lastIndexOf("(");
										year = year.substring(beginYear + 1);
										if (year.contains("(") && year.contains(")")) {
											beginYear = year.lastIndexOf("(");
											endYear = year.lastIndexOf(")");
											year = year.substring(beginYear + 1, endYear);
											// System.out.println(year);
											// System.out.println();

										}
									}

								}
								if (year.contains("/")) {
									int index = year.indexOf("/");
									year = year.substring(0, index);
								} else if (year.contains("?")) {
									year = "";
								}
								String key = movieTitle + "_" + year;

								if (!movieTitle.isEmpty()) {
									movies.add(key);
								}
							}
						}
					} else {
						for (String key : movies) {
							if (IMDBMovieMap.containsKey(key)) {
								if (IMDBMovieMap.get(key).director == null) {
									HashSet<String> director = new HashSet<String>();
									director.add(directorName);
									IMDBMovieMap.get(key).director = director;
								} else {
									HashSet<String> director = IMDBMovieMap.get(key).director;
									director.add(directorName);
									IMDBMovieMap.get(key).director = director;
								}
							}
						}
						directorName = "";
						movies = new HashSet<String>();
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finish set director");
		return IMDBMovieMap;

	}

	public HashMap<String, IMDBMovie> setGenres(HashMap<String, IMDBMovie> IMDBMovieMap) {
		System.out.println("begin setting gneres");
		File file = new File(genresPath);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader br = new BufferedReader(reader);
			String temline = "";
			String line = "";
			while ((temline = br.readLine()) != null) {
				if (temline.contains("8: THE GENRES LIST")) {
					line = br.readLine();// ======
					line = br.readLine();// empty
					line = br.readLine();
					temline = line;
				}
				if (!line.isEmpty()) {
					line = temline;
					int indexTitleH = line.indexOf("\"");
					int indexTitleT = line.indexOf("\"", 1);
					if (!(indexTitleT == indexTitleH)) {
						String temTitle = line.substring(indexTitleH + 1, indexTitleT);

						line = line.substring(indexTitleT + 1);
						int beginYear = line.indexOf("(");
						int endYear = line.indexOf(")");
						String year = line.substring(beginYear + 1, endYear);
						if (year.contains("?")) {
							year = "";
						}
						String key = temTitle + "_" + year;

						String[] data = line.trim().split(" ");
						if (IMDBMovieMap.containsKey(key)) {
							String[] temData = data[data.length - 1].trim().split("\t");
							String genreTem = temData[temData.length - 1].trim();
							if (IMDBMovieMap.get(key).genre == null) {
								HashSet<String> genre = new HashSet<String>();
								genre.add(genreTem);
								IMDBMovieMap.get(key).genre = genre;
							} else {
								HashSet<String> genre = IMDBMovieMap.get(key).genre;
								genre.add(genreTem);
								IMDBMovieMap.get(key).genre = genre;
							}
						}
					}
				}

			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finish set genres.");
		return IMDBMovieMap;
	}

	public HashMap<String, IMDBMovie> setLanguage(HashMap<String, IMDBMovie> IMDBMovieMap) {
		System.out.println("begin setting language");
		File file = new File(languagePath);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader br = new BufferedReader(reader);
			String temline = "";
			String line = "";
			while ((temline = br.readLine()) != null) {
				if (temline.contains("LANGUAGE LIST")) {
					line = br.readLine();// ======
					line = br.readLine();
					temline = line;
				}
				if (!line.isEmpty()) {
					line = temline;
					int indexTitleH = line.indexOf("\"");
					int indexTitleT = line.indexOf("\"", 1);
					if (!(indexTitleT == indexTitleH)) {
						String temTitle = line.substring(indexTitleH + 1, indexTitleT);

						String tem = line.substring(indexTitleT + 1);
						int beginYear = tem.indexOf("(");
						int endYear = tem.indexOf(")");
						String year = tem.substring(beginYear + 1, endYear);
						if (year.contains("?")) {
							year = "";
						}
						String key = temTitle + "_" + year;

						String[] data = line.trim().split("\t");
						if (IMDBMovieMap.containsKey(key)) {
							String[] temData = data[data.length - 1].trim().split("\\)");
							String language = temData[temData.length - 1].trim();
							IMDBMovieMap.get(key).language = language;
						}
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finish set language.");
		return IMDBMovieMap;

	}

	public HashMap<String, IMDBMovie> setActor(HashMap<String, IMDBMovie> IMDBMovieMap) {
		System.out.println("begin setting actor");
		File file = new File(actorPath);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader br = new BufferedReader(reader);
			String temline = "";
			String line = "";
			// actor
			String actorName = "";
			// movie list
			// key: movie value: charactor name
			HashMap<String, Movie> movieMap = new HashMap<String, Movie>();
			while ((temline = br.readLine()) != null) {
				if (temline.contains("THE ACTORS LIST")) {
					line = br.readLine();// ======
					line = br.readLine();// empty
					line = br.readLine();// title line
					line = br.readLine();// -----
					line = br.readLine();
					temline = line;
				}

				if (!line.isEmpty()) {
					if (!temline.isEmpty()) {
						line = temline;
						String[] data = line.split("\t");
						if (!data[0].isEmpty()) {
							String temActorName = data[0].trim();
							String[] temAcList = temActorName.replaceAll("'", "").trim().split(",");
							actorName = temAcList[temAcList.length - 1].trim() + temAcList[0].trim();
							String movieNName = "";
							String characterName = "";
							// get first movie
							String[] temMovie = data[data.length - 1].trim().split("\\(");
							movieNName = temMovie[0].trim().replaceAll("\"", "");
							int beginIndex = data[data.length - 1].trim().indexOf("[");
							int endIndex = data[data.length - 1].trim().lastIndexOf("]");
							if (beginIndex == endIndex) {
								continue;
							} else {
								characterName = data[data.length - 1].trim().substring(beginIndex + 1, endIndex);

								// TODO: add year into movieName
								// set year
								String tem = data[data.length - 1];
								if (tem.contains("{")) {// remove {} part
									int indexRemove = tem.indexOf("{");
									if (indexRemove > tem.indexOf("(")) {
										tem = tem.substring(0, indexRemove - 1);
									}
								}
								// System.out.println("data[data.length - 1] ->"
								// +
								// data[data.length - 1]);
								int beginYear = tem.indexOf("(");
								int endYear = tem.trim().lastIndexOf(")");
								int indexRemove = tem.indexOf("{");
								String year = "";
								if (beginYear < endYear) {
									year = tem.trim().substring(beginYear + 1, endYear);
									if (year.contains("(")) {
										endYear = year.lastIndexOf(")");
										if (!year.contains(")")) {
											beginYear = year.indexOf("(");
											year = year.substring(beginYear + 1);
										} else {
											year = year.substring(0, endYear);
										}
										if (year.contains("/")) {// address
																	// [2012)
																	// (co-director)]
											int index = year.indexOf("/");
											year = year.substring(0, index);
										} else if (year.contains("(")) {// address
																		// [Festival
																		// Edition)
																		// (2005]
											beginYear = year.lastIndexOf("(");
											year = year.substring(beginYear + 1);
											if (year.contains("(") && year.contains(")")) {
												beginYear = year.lastIndexOf("(");
												endYear = year.lastIndexOf(")");
												year = year.substring(beginYear + 1, endYear);
											}

										}

									}
									if (year.contains("/")) {
										int index = year.indexOf("/");
										year = year.substring(0, index);
									} else if (year.contains("?")) {
										year = "";
									}
								}
								String key = movieNName + "_" + year;

								Movie movie = new Movie(key, characterName);
								movieMap.put(key, movie);
							}
						} else {
							String temMovieLine = "";
							for (int i = 0; i < data.length; i++) {
								if (!data[i].isEmpty()) {
									temMovieLine = data[i];
									break;
								}
							}
							String movieNName = "";
							String characterName = "";
							String[] temMovie = temMovieLine.trim().split("\\(");
							movieNName = temMovie[0].trim().replaceAll("\"", "");
							int beginIndex = data[data.length - 1].trim().indexOf("[");
							int endIndex = data[data.length - 1].trim().lastIndexOf("]");
							if (beginIndex == endIndex) {
								continue;
							} else {
								characterName = data[data.length - 1].trim().substring(beginIndex + 1, endIndex);

								// TODO: add year into movieName
								// set year
								String tem = data[data.length - 1];
								if (tem.contains("{")) {// remove {} part
									int indexRemove = tem.indexOf("{");
									if (indexRemove > tem.indexOf("(")) {
										tem = tem.substring(0, indexRemove - 1);
									}
								}
								// System.out.println("data[data.length - 1] ->"
								// +
								// data[data.length - 1]);
								int beginYear = tem.indexOf("(");
								int endYear = tem.trim().lastIndexOf(")");
								int indexRemove = tem.indexOf("{");
								String year = "";
								if (beginYear < endYear) {
									year = tem.trim().substring(beginYear + 1, endYear);
									if (year.contains("(")) {
										endYear = year.lastIndexOf(")");
										if (!year.contains(")")) {
											beginYear = year.indexOf("(");
											year = year.substring(beginYear + 1);
										} else {
											year = year.substring(0, endYear);
										}
										if (year.contains("/")) {// address
																	// [2012)
																	// (co-director)]
											int index = year.indexOf("/");
											year = year.substring(0, index);
										} else if (year.contains("(")) {// address
																		// [Festival
																		// Edition)
																		// (2005]
											beginYear = year.lastIndexOf("(");
											year = year.substring(beginYear + 1);
											if (year.contains("(") && year.contains(")")) {
												beginYear = year.lastIndexOf("(");
												endYear = year.lastIndexOf(")");
												year = year.substring(beginYear + 1, endYear);
											}

										}

									}
									if (year.contains("/")) {
										int index = year.indexOf("/");
										year = year.substring(0, index);
									} else if (year.contains("?")) {
										year = "";
									}
								}
								String key = movieNName + "_" + year;

								Movie movie = new Movie(key, characterName);
								movieMap.put(key, movie);
							}
						}
					} else {
						// set actor
						for (String movieName : movieMap.keySet()) {
							if (IMDBMovieMap.containsKey(movieName)) {
								if (IMDBMovieMap.get(movieName).actor == null) {
									HashMap<String, String> actor = new HashMap<String, String>();
									actor.put(actorName, movieMap.get(movieName).characterName);
									IMDBMovieMap.get(movieName).actor = actor;
								} else {
									HashMap<String, String> actor = IMDBMovieMap.get(movieName).actor;
									actor.put(actorName, movieMap.get(movieName).characterName);
									IMDBMovieMap.get(movieName).actor = actor;
								}
							}
						}
						actorName = "";
						movieMap = new HashMap<String, Movie>();
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finish set actor");
		return IMDBMovieMap;

	}

	public HashMap<String, IMDBMovie> setActress(HashMap<String, IMDBMovie> IMDBMovieMap) {
		System.out.println("begin setting actress");
		File file = new File(actressPath);
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			BufferedReader br = new BufferedReader(reader);
			String temline = "";
			String line = "";
			// actor
			String actressName = "";
			// movie list
			// key: movie value: charactor name
			HashMap<String, Movie> movieMap = new HashMap<String, Movie>();
			while ((temline = br.readLine()) != null) {
				if (temline.contains("THE ACTRESSES LIST")) {
					line = br.readLine();// ======
					line = br.readLine();// empty
					line = br.readLine();// title line
					line = br.readLine();// -----
					line = br.readLine();
					temline = line;
				}

				if (!line.isEmpty()) {
					if (!temline.isEmpty()) {
						line = temline;
						String[] data = line.split("\t");
						if (!data[0].isEmpty()) {
							String temActressName = data[0].trim();
							String[] temAcList = temActressName.replaceAll("'", "").trim().split(",");
							actressName = temAcList[temAcList.length - 1].trim() + temAcList[0].trim();
							String movieNName = "";
							String characterName = "";
							// get first movie
							String[] temMovie = data[data.length - 1].trim().split("\\(");
							movieNName = temMovie[0].trim().replaceAll("\"", "");
							int beginIndex = data[data.length - 1].trim().indexOf("[");
							int endIndex = data[data.length - 1].trim().lastIndexOf("]");
							if (beginIndex == endIndex) {
								continue;
							} else {
								characterName = data[data.length - 1].trim().substring(beginIndex + 1, endIndex);

								// TODO: add year into movieName
								// set year
								String tem = data[data.length - 1];
								if (tem.contains("{")) {// remove {} part
									int indexRemove = tem.indexOf("{");
									if (indexRemove > tem.indexOf("(")) {
										tem = tem.substring(0, indexRemove - 1);
									}
								}
								// System.out.println("data[data.length - 1] ->"
								// +
								// data[data.length - 1]);
								int beginYear = tem.indexOf("(");
								int endYear = tem.trim().lastIndexOf(")");
								int indexRemove = tem.indexOf("{");
								String year = "";
								if (beginYear < endYear) {
									year = tem.trim().substring(beginYear + 1, endYear);
									if (year.contains("(")) {
										endYear = year.lastIndexOf(")");
										if (!year.contains(")")) {
											beginYear = year.indexOf("(");
											year = year.substring(beginYear + 1);
										} else {
											year = year.substring(0, endYear);
										}
										if (year.contains("/")) {// address
																	// [2012)
																	// (co-director)]
											int index = year.indexOf("/");
											year = year.substring(0, index);
										} else if (year.contains("(")) {// address
																		// [Festival
																		// Edition)
																		// (2005]
											beginYear = year.lastIndexOf("(");
											year = year.substring(beginYear + 1);
											if (year.contains("(") && year.contains(")")) {
												beginYear = year.lastIndexOf("(");
												endYear = year.lastIndexOf(")");
												year = year.substring(beginYear + 1, endYear);
											}

										}

									}
									if (year.contains("/")) {
										int index = year.indexOf("/");
										year = year.substring(0, index);
									} else if (year.contains("?")) {
										year = "";
									}
								}
								String key = movieNName + "_" + year;

								Movie movie = new Movie(key, characterName);
								movieMap.put(key, movie);
							}
						} else {
							String temMovieLine = "";
							for (int i = 0; i < data.length; i++) {
								if (!data[i].isEmpty()) {
									temMovieLine = data[i];
									break;
								}
							}
							String movieNName = "";
							String characterName = "";
							String[] temMovie = temMovieLine.trim().split("\\(");
							movieNName = temMovie[0].trim().replaceAll("\"", "");
							int beginIndex = data[data.length - 1].trim().indexOf("[");
							int endIndex = data[data.length - 1].trim().lastIndexOf("]");
							if (beginIndex == endIndex) {
								continue;
							} else {
								characterName = data[data.length - 1].trim().substring(beginIndex + 1, endIndex);

								// TODO: add year into movieName
								// set year
								String tem = data[data.length - 1];
								if (tem.contains("{")) {// remove {} part
									int indexRemove = tem.indexOf("{");
									if (indexRemove > tem.indexOf("(")) {
										tem = tem.substring(0, indexRemove - 1);
									}
								}
								// System.out.println("data[data.length - 1] ->"
								// +
								// data[data.length - 1]);
								int beginYear = tem.indexOf("(");
								int endYear = tem.trim().lastIndexOf(")");
								int indexRemove = tem.indexOf("{");
								String year = "";
								if (beginYear < endYear) {
									year = tem.trim().substring(beginYear + 1, endYear);
									if (year.contains("(")) {
										endYear = year.lastIndexOf(")");
										if (!year.contains(")")) {
											beginYear = year.indexOf("(");
											year = year.substring(beginYear + 1);
										} else {
											year = year.substring(0, endYear);
										}
										if (year.contains("/")) {// address
																	// [2012)
																	// (co-director)]
											int index = year.indexOf("/");
											year = year.substring(0, index);
										} else if (year.contains("(")) {// address
																		// [Festival
																		// Edition)
																		// (2005]
											beginYear = year.lastIndexOf("(");
											year = year.substring(beginYear + 1);
											if (year.contains("(") && year.contains(")")) {
												beginYear = year.lastIndexOf("(");
												endYear = year.lastIndexOf(")");
												year = year.substring(beginYear + 1, endYear);
											}

										}

									}
									if (year.contains("/")) {
										int index = year.indexOf("/");
										year = year.substring(0, index);
									} else if (year.contains("?")) {
										year = "";
									}
								}
								String key = movieNName + "_" + year;

								Movie movie = new Movie(key, characterName);
								movieMap.put(key, movie);
							}
						}
					} else {
						// set actress
						for (String movieName : movieMap.keySet()) {
							if (IMDBMovieMap.containsKey(movieName)) {
								if (IMDBMovieMap.get(movieName).actress == null) {
									HashMap<String, String> actress = new HashMap<String, String>();
									actress.put(actressName, movieMap.get(movieName).characterName);
									IMDBMovieMap.get(movieName).actress = actress;
								} else {
									HashMap<String, String> actress = IMDBMovieMap.get(movieName).actress;
									actress.put(actressName, movieMap.get(movieName).characterName);
									IMDBMovieMap.get(movieName).actress = actress;
								}
							}
						}
						// System.out.println(actressName);
						// System.out.println(movieMap.keySet());
						actressName = "";
						movieMap = new HashMap<String, Movie>();
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finish set actress");
		return IMDBMovieMap;

	}
	
	public static String CleanStr(String s) {
		return s.replaceAll(",", " ").replaceAll("\"", "");
	}

	public static class Director {
		public String directors;
		public HashSet<String> movies;

		Director(String directors, HashSet<String> movies) {
			this.directors = directors;
			this.movies = movies;
		}
	}

	public static class Movie {
		public String title;
		public String characterName;
		// public HashSet<>

		Movie(String title, String characterName) {
			this.title = title;
			this.characterName = characterName;
		}
	}

}
