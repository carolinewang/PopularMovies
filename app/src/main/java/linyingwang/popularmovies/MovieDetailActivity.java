package linyingwang.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class MovieDetailActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_detail);

		if (savedInstanceState == null) {

			Bundle arguments = new Bundle();
			arguments.putLong(MovieDetailFragment.ARG_MOVIE_ID,
					getIntent().getLongExtra(MovieDetailFragment.ARG_MOVIE_ID,168259));
			MovieDetailFragment fragment = new MovieDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.movie_detail_container, fragment)
					.commit();
		}
}

}

