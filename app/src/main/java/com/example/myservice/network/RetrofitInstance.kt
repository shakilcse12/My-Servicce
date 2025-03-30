import com.example.myservice.network.AuthService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// RetrofitInstance.kt
object RetrofitInstance {
    private const val BASE_URL = "http://127.0.0.1:8000/"
    private const val BASE_URL2 = "http://10.0.2.2:8000/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL2)
            .client(client) // Add logging
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }
}