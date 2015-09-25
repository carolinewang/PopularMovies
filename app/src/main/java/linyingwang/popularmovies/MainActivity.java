package linyingwang.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.Result;

public class MainActivity extends ActionBarActivity {
	public final static String ID = "id";
	public final static String DISCOVER ="discover/movie";
	public final static String TOP_RATED ="movie/top_rated";
	public final static String SORT_POPULARITY ="popularity.desc";

//	public final static String SORT_RATING ="vote_average.desc";

	protected boolean isOnline;
	//	public ArrayList<String> urls = new ArrayList<>();
	public String[] posterPaths;
	public ImageAdapter imageAdapter;
	private GridView gridview;
	private int page = 1;
	private int sortCriteria;
	public ArrayList<Movie> movies = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Main Activity", "OnCreate");
		setContentView(R.layout.activity_main);
		gridview = (GridView) findViewById(R.id.gridview);
		imageAdapter = new ImageAdapter(movies);
		gridview.setAdapter(imageAdapter);
		if (isOnline()) {
			loadSpinner(page);
			gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
				                        int position, long id) {
					long movieID = movies.get(position).id;
					Intent intent = new Intent(MainActivity.this, MovieDetail.class);
					intent.putExtra(ID, movieID);
					startActivity(intent);
				}
			});
		} else {
			Toast.makeText(MainActivity.this, R.string.toast_no_internet, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
//		MenuItem item = menu.findItem(R.id.spinner);
//		Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
////		spinner.setAdapter(adapter); // set the adapter to provide layout of rows and content
////		s.setOnItemSelectedListener(onItemSelectedListener);
//		loadSpinner(page, spinner);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_load_more) {
			loadMoreMovies();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("Main Activity", "OnResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("Main Activity", "OnPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("Main Activity", "OnStop");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d("Main Activity", "OnRestart");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("Main Activity", "OnDestroy");
	}

	public boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		isOnline = networkInfo != null && networkInfo.isConnected();
		return isOnline;
	}

	public void loadSpinner(final int p) {
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.sort_criteria, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						sortCriteria = 0;
						page = 1;
						movies.clear();
						FetchMovieTask fetchMovieTask = new FetchMovieTask();
						fetchMovieTask.execute(DISCOVER,SORT_POPULARITY,String.valueOf(p));
						break;
					case 1:
						sortCriteria = 1;
						page = 1;
						movies.clear();
						FetchMovieTask fetchMovie = new FetchMovieTask();
						fetchMovie.execute(TOP_RATED,String.valueOf(p));
						break;
					default:
						sortCriteria =1;
						FetchMovieTask fetch = new FetchMovieTask();
						fetch.execute(DISCOVER,SORT_POPULARITY,String.valueOf(page));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	public void loadMoreMovies(){
		switch (sortCriteria){
			case 0:
				page++;
				FetchMovieTask fetchMovieTask = new FetchMovieTask();
				fetchMovieTask.execute(DISCOVER,SORT_POPULARITY,String.valueOf(page));
				break;
			case 1:
				page++;
				FetchMovieTask fetchMovie = new FetchMovieTask();
				fetchMovie.execute(TOP_RATED,String.valueOf(page));
				break;
		}

	}

	public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

		private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

		@Override
		protected ArrayList<Movie> doInBackground(String... params) {

			// These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
			if (params.length == 0) {
				return null;
			}
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;

// Will contain the raw JSON response as a string.
			String popMovieJsonStr = null;
//			String sortBy = "popularity.desc";

			try {
				final String BASE_URL = "http://api.themoviedb.org/3/";
				final String SORT_BY = "sort_by";
				final String PAGE = "page";
				final String API = "api_key";
				Uri builtUri;
				if(params.length > 2){
					builtUri = Uri.parse(BASE_URL).buildUpon()
							.appendEncodedPath(params[0])
							.appendQueryParameter(SORT_BY, params[1])
							.appendQueryParameter(PAGE, params[2])
							.appendQueryParameter(API, getString(R.string.api_key))
							.build();
				}else{
					builtUri = Uri.parse(BASE_URL).buildUpon()
							.appendEncodedPath(params[0])
							.appendQueryParameter(PAGE, params[1])
							.appendQueryParameter(API, getString(R.string.api_key))
							.build();
				}

				URL url = new URL(builtUri.toString());
				Log.v(LOG_TAG, "Built URI " + builtUri.toString());

				// Create the request to OpenWeatherMap, and open the connection
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				// Read the input stream into a String
				InputStream inputStream = urlConnection.getInputStream();
				StringBuffer buffer = new StringBuffer();
				if (inputStream == null) {
					// Nothing to do.
					popMovieJsonStr = null;
				}
				reader = new BufferedReader(new InputStreamReader(inputStream));

				String line;
				while ((line = reader.readLine()) != null) {
					// Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
					// But it does make debugging a *lot* easier if you print out the completed
					// buffer for debugging.
					buffer.append(line + "\n");
				}

				if (buffer.length() == 0) {
					// Stream was empty.  No point in parsing.
					popMovieJsonStr = null;
				}
				popMovieJsonStr = buffer.toString();
				Log.v(LOG_TAG, "Pop Movie Json String" + popMovieJsonStr);

			} catch (IOException e) {
				Log.e("PlaceholderFragment", "Error ", e);
				// If the code didn't successfully get the weather data, there's no point in attempting
				// to parse it.
				popMovieJsonStr = null;
			} finally {
				if (urlConnection != null) {
					urlConnection.disconnect();
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (final IOException e) {
						Log.e("PlaceholderFragment", "Error closing stream", e);
					}
				}
			}
			try {
				return getMovieDataFromJson(popMovieJsonStr, 20);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		public ArrayList<Movie> getMovieDataFromJson(String popMovieJsonStr, int numMovie)
				throws JSONException {
			final String RESULTS = "results";
			final String ID = "id";
			final String POSTER_PATH = "poster_path";
			JSONObject movieJson = new JSONObject(popMovieJsonStr);
			JSONArray movieArray = movieJson.getJSONArray(RESULTS);
			posterPaths = new String[numMovie];
			for (int i = 0; i < movieArray.length(); i++) {
				JSONObject movieData = movieArray.getJSONObject(i);
				Long id = movieData.getLong(ID);
				posterPaths[i] = "http://image.tmdb.org/t/p/w185/" + movieData.getString(POSTER_PATH);
				Movie movie = new Movie(id, posterPaths[i]);
				movies.add(movie);
				Log.v(LOG_TAG, "Poster Paths fetched: " + movie.posterPath);
				Log.v(LOG_TAG, "IDs fetched: " + movie.id);
			}
			return movies;
		}

		@Override
		protected void onPostExecute(ArrayList<Movie> movies) {
			super.onPostExecute(movies);
			imageAdapter.notifyDataSetChanged();
		}
	}
		public class ImageAdapter extends ArrayAdapter<Movie> {
			public ImageAdapter(List<Movie> moviez) {
				super(MainActivity.this, android.R.layout.simple_list_item_1, moviez);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ImageView view = (ImageView) convertView;
				if (view == null) {
					view = new ImageView(MainActivity.this);
					view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				}

				Movie movie = getItem(position);
				Picasso.with(MainActivity.this) //
						.load(movie.posterPath) //
						.placeholder(R.drawable.poster_placeholder) //
						.error(R.drawable.movie_placeholder_text) //
						.fit() //
//						.tag(mContext) //
						.into(view);
				return view;
			}
		}
	}


