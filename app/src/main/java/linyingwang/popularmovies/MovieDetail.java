package linyingwang.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieDetail extends ActionBarActivity {
	private ImageView poster;
	private TextView title;
	private TextView releaseDate;
	private TextView plot;
	private TextView rating;
	private RatingBar ratingBar;
	private String movieInfoJsonStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_detail);
		poster = (ImageView)findViewById(R.id.imageView);
		title = (TextView)findViewById(R.id.title);
		releaseDate = (TextView)findViewById(R.id.release_date);
		plot = (TextView)findViewById(R.id.plot);
		rating = (TextView)findViewById(R.id.rating);
		ratingBar = (RatingBar)findViewById(R.id.ratingBar);

		Intent intent = getIntent();
		long imgID = intent.getLongExtra(MainActivity.ID, 0);
		GetMovieTask getMovieTask = new GetMovieTask();
		getMovieTask.execute(imgID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_movie_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
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
//			String movieInfoJsonStr = null;
//			String sortBy = "popularity.desc";
			final String API_KEY = "81cdc16066eb01e599562bc01d19e5a4";

			try {

				final String BASE_URL = "http://api.themoviedb.org/3/movie/";
				final String API = "api_key";
				Uri builtUri = Uri.parse(BASE_URL).buildUpon()
						.appendPath(String.valueOf(params[0]))
						.appendQueryParameter(API, API_KEY)
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
				String movieTitle = movieJson.getString(TITLE);
				title.setText(movieTitle);
				ActionBar actionBar = getSupportActionBar();
				actionBar.setTitle(movieTitle);

				releaseDate.setText(movieJson.getString(DATE));
				Double ratingValue = movieJson.getDouble(RATING);
				float vote = ratingValue.floatValue();
				rating.setText("" + ratingValue + "/10");
				ratingBar.setRating(vote/2);
				plot.setText(movieJson.getString(OVERVIEW));
				String posterPath = "http://image.tmdb.org/t/p/w185/" + movieJson.getString(POSTER_PATH);
				Picasso.with(MovieDetail.this)
						.load(posterPath)
						.placeholder(R.drawable.movie_placeholder_text)
						.into(poster);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	}

	}
}

