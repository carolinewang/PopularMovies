package linyingwang.popularmovies;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieDetailFragment extends Fragment {
	public static final String ARG_MOVIE_ID = "movie_id";
	private ImageView poster;
	private TextView title;
	private TextView releaseDate;
	private TextView plot;
	private TextView rating;
	private RatingBar ratingBar;
	private boolean favorite = false;
	private MenuItem favoriteButton;
	private long movieID;

	private String movieTitle;
	private String date;
	private String plotSynopsis;
	private float vote;
	private String posterPath;

	private ParseObject favMovie;
	private ParseObject favMoviePin;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_movie_detail,container,false);
		poster = (ImageView)rootView.findViewById(R.id.imageView);
		title = (TextView)rootView.findViewById(R.id.title);
		releaseDate = (TextView)rootView.findViewById(R.id.release_date);
		plot = (TextView)rootView.findViewById(R.id.plot);
		rating = (TextView)rootView.findViewById(R.id.rating);
		ratingBar = (RatingBar)rootView.findViewById(R.id.ratingBar);

//		Intent intent = getActivity().getIntent();
//		if(intent !=null) {
//			movieID = intent.getLongExtra(ARG_MOVIE_ID, 0);
//		}
		if(savedInstanceState!=null){
			movieTitle = savedInstanceState.getString("title");
			plotSynopsis = savedInstanceState.getString("plot");
			date = savedInstanceState.getString("date");
			posterPath = savedInstanceState.getString("posterPath");
			vote = savedInstanceState.getFloat("rating");
			loadIntoView();
		}else{
			GetMovieTask getMovieTask = new GetMovieTask();
			getMovieTask.execute(movieID);
		}
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		Bundle arguments = getArguments();
		if (arguments!=null) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			movieID = arguments.getLong(ARG_MOVIE_ID);
		}
	}

	@Override
	public void onResume() {
		super.onResume();


	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("title",movieTitle);
		outState.putString("plot",plotSynopsis);
		outState.putString("date",date);
		outState.putString("posterPath", posterPath);
		outState.putFloat("rating",vote);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.menu_movie_detail, menu);
		favoriteButton = menu.findItem(R.id.action_favorite);
		setFavoriteStatus();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_favorite) {
			addToFavorite();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public class GetMovieTask extends AsyncTask<Long, Void, String> {

		private final String LOG_TAG = GetMovieTask.class.getSimpleName();

		@Override
		protected String doInBackground(Long... params) {
			// These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
			if (params.length == 0) {
				return null;
			}
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;

// Will contain the raw JSON response as a string.
			String movieInfoJsonStr = null;
//			String sortBy = "popularity.desc";
			try {

				final String BASE_URL = "http://api.themoviedb.org/3/movie/";
				final String API = "api_key";
				Uri builtUri = Uri.parse(BASE_URL).buildUpon()
						.appendPath(String.valueOf(params[0]))
						.appendQueryParameter(API, getString(R.string.api_key))
						.build();
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
					movieInfoJsonStr = null;
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
					movieInfoJsonStr = null;
				}
				movieInfoJsonStr = buffer.toString();
				Log.v(LOG_TAG, "Movie Info Json String" + movieInfoJsonStr);

			} catch (IOException e) {
				Log.e("PlaceholderFragment", "Error ", e);
				// If the code didn't successfully get the weather data, there's no point in attempting
				// to parse it.
				movieInfoJsonStr = null;
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

			return movieInfoJsonStr;
		}

		@Override
		protected void onPostExecute(String movieInfoJsonStr) {
			super.onPostExecute(movieInfoJsonStr);
			final String DATE = "release_date";
			final String RATING = "vote_average";
			final String POSTER_PATH = "poster_path";
			final String OVERVIEW = "overview";
			final String TITLE = "original_title";

			try{
				JSONObject movieJson = new JSONObject(movieInfoJsonStr);
				movieTitle = movieJson.getString(TITLE);
				date = movieJson.getString(DATE);
				Double ratingValue = movieJson.getDouble(RATING);
				vote = ratingValue.floatValue();
				plotSynopsis = movieJson.getString(OVERVIEW);
				posterPath = "http://image.tmdb.org/t/p/w185/" + movieJson.getString(POSTER_PATH);
//				MovieDetail movieDetail = new MovieDetail(movieID,posterPath,movieTitle,date,plotSynopsis,vote);
				loadIntoView();
			} catch (JSONException e) {
				e.printStackTrace();
			}
	}

	}
	public void loadIntoView(){
		title.setText(movieTitle);
		((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(movieTitle);
		releaseDate.setText(date);
		rating.setText("" + vote + "/10");
		ratingBar.setRating(vote / 2);
		plot.setText(plotSynopsis);
		Picasso.with(getActivity())
				.load(posterPath)
				.placeholder(R.drawable.movie_placeholder_text)
				.into(poster);
	}

	public void setFavoriteStatus(){
		ParseQuery<ParseObject> queryFav = ParseQuery.getQuery("FavMovie");
		queryFav.fromLocalDatastore();
		queryFav.whereEqualTo("movieID", movieID);
		queryFav.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject parseObject, ParseException e) {
				if(e == null){
					favMovie = parseObject;
					favorite = true;
					favoriteButton.setIcon(R.drawable.like_white);
				}
			}
		});
		;
	}

	public void addToFavorite(){
		if(!favorite) {
			favoriteButton.setIcon(R.drawable.like_white);
			favorite = true;
			favMoviePin = new ParseObject("FavMovie");
			ParseUser currentUser = ParseUser.getCurrentUser();
			favMoviePin.put("movieID",movieID);
			favMoviePin.put("posterPath",posterPath);
			favMoviePin.put("user",currentUser);
			favMoviePin.put("plot",plotSynopsis);
			favMoviePin.put("releaseDate",date);
			favMoviePin.put("rating",vote);
			favMoviePin.pinInBackground(new SaveCallback() {
				@Override
				public void done(ParseException e) {
					if(e == null){
						Toast.makeText(getActivity(),R.string.toast_add_to_favorite,
								Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(getActivity(),
								"Error saving: " + e.getMessage(),
								Toast.LENGTH_LONG).show();
					}
				}
			});

		}else{
			favoriteButton.setIcon(R.drawable.like_outline_white);
			favorite = false;
			if(favMoviePin!= null){
					favMoviePin.unpinInBackground();
				}
			if(favMovie!= null){
				try {
					favMovie.delete();
					Toast.makeText(getActivity(),R.string.toast_remove_from_favorite,
							Toast.LENGTH_SHORT).show();
					favorite = false;
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}

		}
	}
}

