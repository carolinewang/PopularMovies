package linyingwang.popularmovies;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Activity;
//import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
import java.util.List;

//import linyingwang.popularmovies.dummy.DummyContent;

public class MovieGridFragment extends Fragment {
	public final static String ID = "id";
	public final static String DISCOVER ="discover/movie";
	public final static String TOP_RATED ="movie/top_rated";
	public final static String SORT_POPULARITY ="popularity.desc";
	private static final String SELECTED_KEY = "selected_position";
	private static final String PARCELABLE_KEY_POPULARITY = "Most Popular";
	private static final String PARCELABLE_KEY_TOPRATED = "Highest Rated";
//	public final static String SORT_RATING ="vote_average.desc";

	protected boolean isOnline;
	//	public ArrayList<String> urls = new ArrayList<>();
	public String[] posterPaths;
	public ImageAdapter imageAdapter;
	private GridView gridview;
	private int page = 1;
	private int sortCriteria;
	private boolean loadMore = false;
	public ArrayList<Movie> movies;
	private ProgressBar progressBar;
	private Spinner spinner;
	private Callbacks mCallbacks = sDummyCallbacks;
	private int mPosition = ListView.INVALID_POSITION;
	private Bundle bundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState!=null){
			bundle = savedInstanceState;
			if(savedInstanceState.containsKey(PARCELABLE_KEY_POPULARITY)){
				movies = savedInstanceState.getParcelableArrayList(PARCELABLE_KEY_POPULARITY);
			}else if(savedInstanceState.containsKey(PARCELABLE_KEY_TOPRATED)){
				movies = savedInstanceState.getParcelableArrayList(PARCELABLE_KEY_TOPRATED);
			}
		}else{
			movies = new ArrayList<Movie>();
		}
		// Add this line in order for this fragment to handle menu events.
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);
		spinner = (Spinner) rootView.findViewById(R.id.spinner);

		progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
		gridview = (GridView) rootView.findViewById(R.id.gridview);

		if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
			mPosition = savedInstanceState.getInt(SELECTED_KEY);
			gridview.smoothScrollToPosition(mPosition);

		}
//		else if (savedInstanceState == null){

//		}
		imageAdapter = new ImageAdapter(getActivity(), movies);
		gridview.setAdapter(imageAdapter);
//		if (isOnline()) {
			loadSpinner();
//		} else {
//			Toast.makeText(getActivity(), R.string.toast_no_internet, Toast.LENGTH_LONG).show();
//		}
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
			                        int position, long id) {
				long movieID = movies.get(position).id;
				((Callbacks) getActivity())
						.onItemSelected(movieID);
				mPosition = position;
			}
		});

		return rootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
			mPosition = savedInstanceState.getInt(SELECTED_KEY);
			// If we don't need to restart the loader, and there's a desired position to restore
			// to, do so now.
			gridview.setSelection(mPosition);
//			gridview.smoothScrollToPosition(mPosition);
		}
	}



	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// When tablets rotate, the currently selected list item needs to be saved.
		// When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
		// so check for that before storing.
		if (mPosition != ListView.INVALID_POSITION) {
			outState.putInt(SELECTED_KEY, mPosition);
		}
		switch (sortCriteria){
			case 0:
				outState.putParcelableArrayList(PARCELABLE_KEY_POPULARITY,movies);
				break;
			case 1:
				outState.putParcelableArrayList(PARCELABLE_KEY_TOPRATED,movies);
				break;
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_movie_grid, menu);
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



	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(long id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(long id) {
		}
	};


	public boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager)
				getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		isOnline = networkInfo != null && networkInfo.isConnected();
		return isOnline;
	}

	public void showDialogWhenOffline(final Spinner spinner){
		new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert).setTitle("No Internet Connection")
				.setMessage("Would you like to view \"My Favorites\" when offline?")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						spinner.setSelection(2);
					}
				}).setNegativeButton("No", null).show();
	}

	public void loadSpinner() {

// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.sort_criteria, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
//			savedInstanceState = bundle;
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				loadMore = false;
				page = 1;
				switch (position) {
					case 0:
						if(isOnline()){
						sortCriteria = 0;
						if(bundle == null || !bundle.containsKey(PARCELABLE_KEY_POPULARITY)){
							FetchMovieTask fetchMovieTask = new FetchMovieTask();
							fetchMovieTask.execute(DISCOVER,SORT_POPULARITY,String.valueOf(page));
						}
						}else {
							showDialogWhenOffline(spinner);
						}
						break;
					case 1:
						if(isOnline()){
							sortCriteria = 1;
							if(bundle == null || !bundle.containsKey(PARCELABLE_KEY_TOPRATED)){
								FetchMovieTask fetchMovie = new FetchMovieTask();
								fetchMovie.execute(TOP_RATED,String.valueOf(page));
							}
						}else {
							showDialogWhenOffline(spinner);
						}
						break;
					case 2:
						sortCriteria = 2;
						displayFavorites();
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}



	public void loadMoreMovies(){
		loadMore = true;
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

	public void displayFavorites(){

		ParseQuery <ParseObject> queryFavMovies = ParseQuery.getQuery("FavMovie");
		queryFavMovies.fromLocalDatastore();
		queryFavMovies.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> favMovies, ParseException e) {
				if(e == null){
					if(favMovies.size()!=0){
						movies.clear();
						for(int i= 0; i<favMovies.size();i++){
							Long movieID = favMovies.get(i).getLong("movieID");
							String posterPath = favMovies.get(i).getString("posterPath");
							Movie movie = new Movie(movieID,posterPath);
							movies.add(movie);
							Log.d("fav movie", String.valueOf(movieID));
						}
//						imageAdapter.notifyDataSetChanged();
						imageAdapter = new ImageAdapter(getActivity(),movies);
						gridview.setAdapter(imageAdapter);
					}else{
						Toast.makeText(getActivity(),R.string.toast_no_favorite,Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(getActivity(),"Error: " + e.toString(),Toast.LENGTH_SHORT).show();
				}
			}
		});
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
			if(!loadMore){
				movies.clear();

			}
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
		protected void onPreExecute() {
			super.onPreExecute();
//			if(!loadMore){
			progressBar.setVisibility(View.VISIBLE);
//			} progressBar2.setVisibility(View.VISIBLE);

		}

		@Override
		protected void onPostExecute(ArrayList<Movie> movies) {
			super.onPostExecute(movies);
			progressBar.setVisibility(View.GONE);

			if(loadMore){
				imageAdapter.notifyDataSetChanged();
//				gridview.smoothScrollToPosition(mPosition);
			}else{
				imageAdapter = new ImageAdapter(getActivity(), movies);
				gridview.setAdapter(imageAdapter);
//				gridview.smoothScrollToPosition(mPosition);
			}

		}
	}
		public class ImageAdapter extends ArrayAdapter<Movie> {

			public ImageAdapter(Activity context, List<Movie> moviez) {
				super(context, android.R.layout.simple_list_item_1, moviez);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ImageView view = (ImageView) convertView;
				if (view == null) {
					view = new ImageView(getContext());
					view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				}

				Movie movie = getItem(position);
				Picasso.with(getContext()) //
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


