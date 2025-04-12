package com.example.myservice.data.constants

object Api {
    const val BASE_URL = "http://192.168.1.102:8000/"
    const val BASE_URL3 = "http://192.168.18.10:8000/"
    const val BASE_URL2 = "http://192.168.1.8:8000/"
    const val BASE_URL_AVD = "http://10.0.2.2:8000/"
    // Change this to your local server IP
    //const val LOGIN_ENDPOINT = "login"
    object Endpoints {
        const val LOGIN = "api/login"
        const val INVOICES = "api/sales"
        const val INVOICE_DETAILS = "api/sales/{invoiceId}"
        const val INVOICE_CREATE = "api/sales/create"
        const val PRODUCTS = "api/products"
        const val PARTIES = "api/parties"
        const val PARTIY_CREATE = "api/party/create"
    }
}