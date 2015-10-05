package linyingwang.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by haylin2002 on 9/14/15.
 */
public class MovieDetail implements Parcelable{
	public long id;
	public String posterPath;
	public String title;
	public String releaseDate;
	public String plot;
	public float vote;

	public MovieDetail(long id, String posterPath, String title, String releaseDate,String plot, float vote){
		this.id = id;
		this.posterPath = posterPath;
		this.title = title;
		this.releaseDate = releaseDate;
		this.plot = plot;
		this.vote = vote;
	}

	private MovieDetail(Parcel in){
		id = in.readLong();
		posterPath = in.readString();
		title = in.readString();
		releaseDate = in.readString();
		plot = in.readString();
		vote = in.readFloat();
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeLong(id);
		parcel.writeString(posterPath);
		parcel.writeString(title);
		parcel.writeString(releaseDate);
		parcel.writeString(plot);
		parcel.writeFloat(vote);
	}

	public final Creator<MovieDetail> CREATOR = new Creator<MovieDetail>(){

		@Override
		public MovieDetail createFromParcel(Parcel source) {
			return new MovieDetail(source);
		}

		@Override
		public MovieDetail[] newArray(int size) {
			return new MovieDetail[size ];
		}
	};
}
