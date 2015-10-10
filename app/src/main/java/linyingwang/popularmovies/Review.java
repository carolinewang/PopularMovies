package linyingwang.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by haylin2002 on 10/5/15.
 */
public class Review implements Parcelable{
	public String author;
	public String content;

	public Review(String author, String content){
		this.author = author;
		this.content = content;
	}
	public Review(Parcel in) {
		author = in.readString();
		content = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(author);
		parcel.writeString(content);
	}

	public final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>(){

		@Override
		public Review createFromParcel(Parcel source) {
			return new Review(source);
		}

		@Override
		public Review[] newArray(int size) {
			return new Review[size ];
		}
	};
}
