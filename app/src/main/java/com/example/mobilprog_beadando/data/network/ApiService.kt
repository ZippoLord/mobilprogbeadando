import com.example.mobilprog_beadando.data.model.MoodEntry
import com.example.mobilprog_beadando.data.model.WeatherResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/weather/{city}") suspend fun getWeather( @Path("city") city: String ): WeatherResponse

    @POST("api/mood")
    suspend fun sendMood(@Body mood: MoodEntry)

    @GET("api/mood/getallmoods")
    suspend fun getAllMood(): List<MoodEntry>

}
