package nl.fourtress.flickrclient.flickr;

import nl.fourtress.flickrclient.flickr.model.ImageSizesResponseModel;
import nl.fourtress.flickrclient.flickr.model.SearchResponseModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Rick Slinkman
 */

public interface FlickrAPI
{

    // &api_key={apiKey}&photo_id={item}
    @GET("?method=flickr.photos.getSizes&format=json&nojsoncallback=1")
    Call<ImageSizesResponseModel> getItemSizes(@Query("api_key") String apiKey, @Query("photo_id") String itemID);

    @GET("?method=flickr.photos.getRecent&format=json&nojsoncallback=1")
    Call<SearchResponseModel> searchImages(@Query("api_key") String apiKey, @Query("per_page") int photosPerPage, @Query("searchTags") String searchTags);

    @GET("?method=flickr.photos.getRecent&format=json&nojsoncallback=1")
    Call<SearchResponseModel> searchImages(@Query("api_key") String apiKey, @Query("per_page") int photosPerPage, @Query("searchTags") String searchTags, @Query("pageNumber") int pageNumber);
}
