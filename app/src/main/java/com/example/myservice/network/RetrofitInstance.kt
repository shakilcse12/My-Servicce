import android.content.Context
import com.example.myservice.network.AuthService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.myservice.data.constants.Api
import com.example.myservice.network.InvoiceService
import com.example.myservice.network.PartyService
import com.example.myservice.network.ProductService
import com.example.myservice.network.TokenInterceptor
import com.example.myservice.util.user.TokenManager

// RetrofitInstance.kt
object RetrofitInstance {

    private lateinit var tokenManager: TokenManager
    private lateinit var client: OkHttpClient
    private lateinit var retrofit: Retrofit

    fun init(context: Context) {
        tokenManager = TokenManager(context)

        client = OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(tokenManager))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(Api.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    val invoiceService: InvoiceService by lazy { retrofit.create(InvoiceService::class.java) }
    val productService: ProductService by lazy { retrofit.create(ProductService::class.java) }
    val partyService: PartyService by lazy { retrofit.create(PartyService::class.java) }

    // Helper function to ensure init() was called
    private fun checkInitialized() {
        if (!::retrofit.isInitialized) {
            throw UninitializedPropertyAccessException("RetrofitInstance.init(context) must be called before using RetrofitInstance")
        }
    }

    // Optional helper method to save token
    fun saveToken(token: String) {
        tokenManager.saveToken(token)
    }

    fun getToken(): String? {
        return tokenManager.getToken()
    }

    fun clearToken() {
        tokenManager.clearToken()
    }
}