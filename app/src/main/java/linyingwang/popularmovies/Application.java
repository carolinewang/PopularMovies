package linyingwang.popularmovies;


import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class Application extends android.app.Application {

	private static final String TAG = Application.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.enableLocalDatastore(getApplicationContext());
		ParseUser.enableAutomaticUser();
		Parse.initialize(this, "zIaDc7HGC9AdFyg85C7JbOxNwGI8OOXdFEPn1yJY",
				"wIPqOr9i3DcgKNrqL7h8S0UhrhnJOQh9PVDCJVkZ");
		ParseACL.setDefaultACL(new ParseACL(), true);

	}
}
