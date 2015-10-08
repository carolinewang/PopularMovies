package linyingwang.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by haylin2002 on 10/5/15.
 */
public class TrailerAdapter extends ArrayAdapter<Trailer> {
	public TrailerAdapter(Activity context, List<Trailer> trailers) {
		super(context,0, trailers);
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		if (convertView == null) {
			convertView =
					LayoutInflater.from(getContext()).inflate(R.layout.custom_grid_item, null);
		}
		ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);

			Trailer trailer = getItem(position);
			Picasso.with(getContext()) //
					.load(trailer.thumbnail) //
					.placeholder(R.drawable.poster_placeholder) //
					.error(R.drawable.poster_placeholder) //
					.fit() //
//						.tag(mContext) //
					.into(thumbnail);
		TextView textView = (TextView)convertView.findViewById(R.id.textView);
		textView.setText(trailer.name);

			return convertView;
		}
	}

