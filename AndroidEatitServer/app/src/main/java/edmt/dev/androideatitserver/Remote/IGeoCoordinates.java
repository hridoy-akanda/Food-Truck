package edmt.dev.androideatitserver.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Minhaz on 3/1/2018.
 */

public interface IGeoCoordinates {
    @GET("maps/api/geocode/json")
    Call<String> getGeoCode(@Query("address")String address);
    @GET("maps/api/directions/json")
    Call<String>getDirections(@Query("origin")String origin, @Query("destination") String destination);

}
