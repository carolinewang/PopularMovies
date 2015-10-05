package linyingwang.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by haylin2002 on 9/14/15.
 */
public class Movie implements Parcelable{
	public long id;
	public String posterPath;

	public Movie(long l,String s){
		id = l;
		posterPath = s;
	}

	private Movie(Parcel in){
		id = in.readLong();
		posterPath = in.readString();
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeLong(id);
		parcel.writeString(posterPath);
	}

	public final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>(){

		@Override
		public Movie createFromParcel(Parcel source) {
			return new Movie(source);
		}

		@Override
		public Movie[] newArray(int size) {
			return new Movie[size ];
		}
	};
}
