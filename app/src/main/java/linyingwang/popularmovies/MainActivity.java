package linyingwang.popularmovies;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends ActionBarActivity implements MovieGridFragment.Callbacks{
	private boolean mTwoPane;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//
		if (findViewById(R.id.movie_detail_container) != null) {
			mTwoPane = true;
		}

	}


	@Override
	public void onItemSelected(long id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putLong(MovieDetailFragment.ARG_MOVIE_ID, id);
			MovieDetailFragment fragment = new MovieDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.movie_detail_container, fragment)
					.commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, MovieDetailActivity.class);
			detailIntent.putExtra(MovieDetailFragment.ARG_MOVIE_ID, id);
			startActivity(detailIntent);
		}
	}
}


