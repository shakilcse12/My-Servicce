import com.example.myservice.network.AuthService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.myservice.data.constants.Api
import com.example.myservice.network.InvoiceService
import com.example.myservice.network.PartyService
import com.example.myservice.network.ProductService

// RetrofitInstance.kt
object RetrofitInstance {
    private const val BASE_URL = "http://127.0.0.1:8000/"
    private const val BASE_URL2 = "http://10.0.2.2:8000/"
    private const val TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiYzZmZWEyMjIxNGQzMjNhNjVmYmYzMzY5N2I2ODQxZmRhNTNkZDYyMDE1NWRkYTE1NTczODViNzhkODgwMjAxNTU5YmMzZTAwNzZhN2YxYjIiLCJpYXQiOjE3NDMwODUyNzguMTAxMDk1LCJuYmYiOjE3NDMwODUyNzguMTAxMTAyLCJleHAiOjE3NzQ2MjEyNzcuMzAxNTE3LCJzdWIiOiIxMyIsInNjb3BlcyI6W119.USByHAf6ebqktTt7qAbSAbu681vf8w_wNbTDON0C3QjMdVdNiyPE1I5T2FvspUo4t2Cy6A0_CjpbvqNunTLnWHE8ZRKD2qwVYBWam1rzzQsGSP3NVLjsY88w10OvQXPOK_1oQ16qysX0ZhdzYOSBNrw3Wb0OWzEUDrVFfB5oPmuVMQY_SalAmEHWjkzau0EjNPuEYtWgxQho63fBTMgUxoIlNC2Y6Da2N0hVxxe1AZERuQGSCAh1Wsb7LjrZ4p19YI9ccwBRQWO5M6-n--LXPNSLPTOqCoYgdfyaG6ydZomGExqQfDsWUrNd_mrI19F6QZtTCLfG3fkCDu9rE_Qv9zPwCmplMXWWv9USNL0TWxnFZXAcsc-MIXzVcPQdbu20PId9fEHYvCIwHzNr_BSgT6se5sgeUIEeh5-l_rB4DHgp2jjKPA82SHYRlQ93WHSMUIhRauVJkMunx7M3q-Y1XS6uGxgyS5qhtLqYkgMEUNScGrkFQbb1YqKT9MiFYBHJKsXtjffo4qf9gQ2QhZ91FcGGU3Rw3WQPMIrW8XUXCdJiRNe304sQ5YL5KwAvSM7bPyhpbbqvWIz5hh8y91b6R7elduRvOBRl26AXfRFhc7ZGQIvCo8lzPrpsVCuE5qnhBmm3qs01aIipxHZ4fYrJSAMc-zldiJn77sQeDVoD8uI"
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestWithToken = originalRequest.newBuilder()
                .header("Authorization", "Bearer $TOKEN")
                .build()
            chain.proceed(requestWithToken)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(Api.BASE_URL_AVD)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    val invoiceService: InvoiceService by lazy { retrofit.create(InvoiceService::class.java) }
    val productService: ProductService by lazy { retrofit.create(ProductService::class.java) }
    val partyService: PartyService by lazy { retrofit.create(PartyService::class.java) }
}