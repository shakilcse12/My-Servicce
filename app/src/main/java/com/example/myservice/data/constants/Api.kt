package com.example.myservice.data.constants

object Api {
    const val BASE_URL = "http://192.168.1.10:8000/"
    const val BASE_URL_AVD = "http://10.0.2.2:8000/"
    // Change this to your local server IP
    //const val LOGIN_ENDPOINT = "login"
    object Endpoints {
        const val LOGIN = "api/login"
        const val INVOICES = "api/sales"
        const val PRODUCTS = "api/products"
        const val PARTIES = "api/parties"
    }
}