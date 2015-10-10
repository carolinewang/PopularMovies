package linyingwang.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by haylin2002 on 10/5/15.
 */
public class Trailer implements Parcelable {
	public String link;
	public String name;
	public String thumbnail;

	public Trailer(String link,String name, String thumbnail){
		this.link = link;
		this.name = name;
		this.thumbnail = thumbnail;
	}

	public Trailer(Parcel in) {
		link = in.readString();
		name = in.readString();
		thumbnail = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(link);
		parcel.writeString(name);
		parcel.writeString(thumbnail);
	}
	public final Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>(){

		@Override
		public Trailer createFromParcel(Parcel source) {
			return new Trailer(source);
		}

		@Override
		public Trailer[] newArray(int size) {
			return new Trailer[size ];
		}
	};
}
